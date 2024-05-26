package com.example.invoicemaker_1_0_231212.adapter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SwipeLayout
import com.example.invoicemaker_1_0_231212.CreateInvoiceItemSelectActivity
import com.example.invoicemaker_1_0_231212.CreateItemActivity
import com.example.invoicemaker_1_0_231212.model.Item
import com.example.invoicemaker_1_0_231212.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Locale

// CreateInvoiceItemSelect
class ItemSelectListAdapter(
    private val context: Context,
    private val items: ArrayList<Item>,
    private val scrollPosition: Int,
    private val InvoiceID: String,
    private val settingArray: Array<out Any>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<ItemSelectListAdapter.ItemSelectViewHolder>() {

    class ItemSelectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val invoiceItemListImageView: ImageView = view.findViewById(R.id.Invoice_Item_List_Image)
        val invoiceItemListNameTextView: TextView = view.findViewById(R.id.Invoice_Item_List_Name)
        val invoiceItemListDescriptionTextView: TextView = view.findViewById(R.id.Invoice_Item_List_Description)
        val invoiceItemListStockTextView: TextView = view.findViewById(R.id.Invoice_Item_List_Stock)
        val invoiceItemListUnitCostTextView: TextView = view.findViewById(R.id.Invoice_Item_List_Cost)
        val invoiceItemListSelectIcon: ImageView = view.findViewById(R.id.Invoice_Item_List_Select_Icon)

        var isSwiping = false
        var lastSwipeTime: Long = 0
    }

    private var currencyPosition = settingArray[0].toString()
    private var currencySymbol = settingArray[1].toString()
    private var decimalPlaces = settingArray[2].toString().toInt()
    private var numberFormat = settingArray[3].toString().toInt()
    private var dateFormatSetting = settingArray[4].toString()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemSelectViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.invoice_item_list_recycler_view, parent, false)
        return ItemSelectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemSelectViewHolder, position: Int) {
        val item = items[position]

        if (item.isRemoved) {
            holder.itemView.visibility = View.GONE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
            return
        } else {
            holder.itemView.visibility = View.VISIBLE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        holder.invoiceItemListNameTextView.text = item.name
        holder.invoiceItemListDescriptionTextView.text = if (item.description == "") {
            "No description"
        } else {
            item.description
        }
        holder.invoiceItemListStockTextView.text = if (item.stock == "") {
            "Stock quantity has not been set"
        } else {
            "${item.stock} in stock"
        }
        holder.invoiceItemListUnitCostTextView.text = setDoubleValueBasedOnSettingsToString(item.unitCost, true)

        val imagePath = context.filesDir.resolve("ItemPhoto/${item.id}/photo_0.jpg")
        if (imagePath.exists()) {
            // Resize the ImageView to the desired dimensions
            val layoutParams = holder.invoiceItemListImageView.layoutParams
            layoutParams.width = 61 * context.resources.displayMetrics.density.toInt() // Convert dp to pixels
            layoutParams.height = 61 * context.resources.displayMetrics.density.toInt()
            holder.invoiceItemListImageView.layoutParams = layoutParams

            // Set the image to ImageView from the file path
            val bitmap = BitmapFactory.decodeFile(imagePath.absolutePath)
            holder.invoiceItemListImageView.setImageBitmap(bitmap)
        } else {
            // If the image file does not exist, load a default image
            holder.invoiceItemListImageView.setImageResource(R.drawable.item_default_photo)
        }

        if (item.stock == "0") {
            holder.invoiceItemListStockTextView.text = "Out of stock"
            // set color to red
            holder.invoiceItemListStockTextView.setTextColor(context.resources.getColor(R.color.red, null))

            val invoiceID = InvoiceID

            if (invoiceID == "" || invoiceID == null) {
                holder.invoiceItemListSelectIcon.visibility = View.INVISIBLE
                // uncheck
                item.isSelected = false

                (context as CreateInvoiceItemSelectActivity).updateAddButtonVisibility()
            } else {
                holder.invoiceItemListSelectIcon.visibility = View.VISIBLE
                holder.invoiceItemListSelectIcon.setOnClickListener {
                    item.isSelected = !item.isSelected
                    notifyItemChanged(position)

                    (context as CreateInvoiceItemSelectActivity).updateAddButtonVisibility()
                }
            }

        } else {

            holder.invoiceItemListStockTextView.text = if (item.stock == "") {
                "Stock quantity has not been set"
            } else {
                "${item.stock} in stock"
            }
            holder.invoiceItemListStockTextView.setTextColor(context.resources.getColor(R.color.textHint, null))
            holder.invoiceItemListSelectIcon.visibility = View.VISIBLE

            holder.invoiceItemListSelectIcon.setOnClickListener {
                item.isSelected = !item.isSelected
                notifyItemChanged(position)

                (context as CreateInvoiceItemSelectActivity).updateAddButtonVisibility()
            }

        }

        val swipeLayout = holder.itemView.findViewById<SwipeLayout>(R.id.Invoice_Item_List_Swipe_Layout)
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, null)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.itemView.findViewById(R.id.Invoice_Item_List_Layout_Remove))

        swipeLayout.addSwipeListener(object : SwipeLayout.SwipeListener {
            override fun onClose(layout: SwipeLayout) {
                // Handle close event
                holder.isSwiping = false
                holder.lastSwipeTime = System.currentTimeMillis()
            }

            override fun onUpdate(layout: SwipeLayout, leftOffset: Int, topOffset: Int) {
                // Handle while swiping
            }

            override fun onStartOpen(layout: SwipeLayout) {
                // Handle when start opening
                holder.isSwiping = true
                holder.lastSwipeTime = System.currentTimeMillis()
            }

            override fun onOpen(layout: SwipeLayout) {
                // Handle open event
            }

            override fun onStartClose(layout: SwipeLayout) {
                // Handle when start closing
            }

            override fun onHandRelease(layout: SwipeLayout, xvel: Float, yvel: Float) {
                // Handle when released
            }

        })

        // Set the initial icon
        if (item.isSelected) {
            holder.invoiceItemListSelectIcon.setImageResource(R.drawable.item_list_select)
            // padding to 3dp
            holder.invoiceItemListSelectIcon.setPadding(
                3 * context.resources.displayMetrics.density.toInt(),
                3 * context.resources.displayMetrics.density.toInt(),
                3 * context.resources.displayMetrics.density.toInt(),
                3 * context.resources.displayMetrics.density.toInt())
        } else {
            holder.invoiceItemListSelectIcon.setImageResource(R.drawable.item_list_unselect)
            // padding to 5dp
            holder.invoiceItemListSelectIcon.setPadding(
                5 * context.resources.displayMetrics.density.toInt(),
                5 * context.resources.displayMetrics.density.toInt(),
                5 * context.resources.displayMetrics.density.toInt(),
                5 * context.resources.displayMetrics.density.toInt())
        }

        // Add click listener to change the icon and update the item's selection state
        holder.invoiceItemListSelectIcon.setOnClickListener {
            item.isSelected = !item.isSelected
            notifyItemChanged(position)

            (context as CreateInvoiceItemSelectActivity).updateAddButtonVisibility()
        }

        // click item in recycler view to edit
        holder.itemView.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - holder.lastSwipeTime > 300 && !holder.isSwiping) {
                // open edit screen if the time from the last swipe is long enough
                val intent = Intent(context, CreateItemActivity::class.java)
                intent.putExtra("PREVIOUS_SCREEN", "CreateInvoiceItemSelectActivity")
                intent.putExtra("ITEM_ID", item.id)
                intent.putExtra("scrollPosition", scrollPosition)
                intent.putExtra("INVOICE_ID", InvoiceID)
                context.startActivity(intent)
            }
        }

        val removeButton = holder.itemView.findViewById<ImageButton>(R.id.Invoice_Item_List_Remove_Button)
        removeButton.setOnClickListener {
            // alert dialog
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle("Remove Item")
            alertDialog.setMessage("Are you sure you want to remove this item?")
            alertDialog.setPositiveButton("Remove", DialogInterface.OnClickListener { _, _ ->

                // val itemToRemove = items[position]

                items[position].isRemoved = true

                // change item isRemoved to true in ItemList.json
                val itemListFile = File(context.filesDir, "ItemList.json")
                val itemListJsonArray = JSONArray(itemListFile.readText())
                for (i in 0 until itemListJsonArray.length()) {
                    val itemJsonObject = itemListJsonArray.getJSONObject(i)
                    if (itemJsonObject.getString("id") == item.id) {
                        itemJsonObject.put("isRemoved", true)
                        break
                    }
                }

                itemListFile.writeText(itemListJsonArray.toString())

                items.removeAt(position)
                itemListFull.removeAt(position)

                notifyItemRemoved(position)

                /*// Save the item list to JSON file
                val itemListFile = File(context.filesDir, "ItemList.json")
                val itemListJsonArray = JSONArray()
                for (item in items) {
                    val itemJsonObject = JSONObject()
                    itemJsonObject.put("id", item.id)
                    itemJsonObject.put("image", item.image)
                    itemJsonObject.put("name", item.name)
                    itemJsonObject.put("description", item.description)
                    itemJsonObject.put("unitType", item.unitType)
                    itemJsonObject.put("stock", item.stock)
                    itemJsonObject.put("unitCost", item.unitCost)
                    itemListJsonArray.put(itemJsonObject)
                }
                itemListFile.writeText(itemListJsonArray.toString())

                // Remove the item photo directory
                val itemPhotoDir = context.filesDir.resolve("ItemPhoto/${itemToRemove.id}")
                if (itemPhotoDir.isDirectory) {
                    itemPhotoDir.deleteRecursively()
                }*/

                // after remove item, reload position of item
                notifyItemRangeChanged(position, items.size)
                notifyItemRangeChanged(position, itemListFull.size)
                notifyDataSetChanged()

                // toast and make it in center of screen
                val toast = android.widget.Toast.makeText(context, "Item removed", android.widget.Toast.LENGTH_SHORT)
                toast.setGravity(android.view.Gravity.CENTER, 0, 0)
                toast.show()
            })
            alertDialog.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
            })
            alertDialog.show()
        }

        // holder.itemView.setOnClickListener { onItemClick(position) }
    }

    override fun getItemCount() = items.size

    private var itemListFull: ArrayList<Item> = ArrayList(items)

    fun filter(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            itemListFull
        } else {
            itemListFull.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
        items.clear()
        items.addAll(filteredList)
        notifyDataSetChanged()
    }

    private fun setStringDateBasedOnSettings(date: String, isConvertTo_ddMMyyyy: Boolean): String {
        if (isConvertTo_ddMMyyyy) {
            // convert to dd/MM/yyyy
            val dateFormat = SimpleDateFormat(dateFormatSetting, Locale.getDefault())
            val dateObj = dateFormat.parse(date)
            return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateObj)
        } else {
            // convert base on date format setting
            val dateFormat = SimpleDateFormat(dateFormatSetting, Locale.getDefault())
            val dateObj = dateFormat.parse(date)
            return SimpleDateFormat(dateFormatSetting, Locale.getDefault()).format(dateObj)
        }
    }

    private fun setDoubleValueBasedOnSettingsToString(value: Double, isInculdeSymbol: Boolean): String {

        var formattedNumber = ""

        if (value.isNaN()) {
            formattedNumber = "0"
        }

        if (value != 0.0) {

            // set the locale based on the settings
            val decimalFormatSymbols = DecimalFormatSymbols().apply {
                decimalSeparator = if (numberFormat == 3 || numberFormat == 4) ',' else '.'
                groupingSeparator = when (numberFormat) {
                    1, 3 -> ' '  // 1, 3 use space
                    4 -> '.'      // 4 use dot
                    else -> ','  // default (2) use comma
                }
            }

            // format the number based on decimal places
            val numberFormatPattern = "###,###.${"0".repeat(decimalPlaces)}"
            val numberFormatter = DecimalFormat(numberFormatPattern, decimalFormatSymbols)
            val formatted = numberFormatter.format(value)

            if (isInculdeSymbol) {
                // format the number based on currency position
                formattedNumber = if (currencyPosition == "before") {
                    "$currencySymbol$formatted"
                } else {
                    "$formatted$currencySymbol"
                }
            } else {
                formattedNumber = formatted
            }

            // if decimalPlaces is 0, remove the decimal point
            if (decimalPlaces == 0) {
                if (numberFormat == 1 || numberFormat == 2) {
                    formattedNumber = formattedNumber.replace(".", "")
                } else {
                    formattedNumber = formattedNumber.replace(",", "")
                }
            }

        } else {
            if (isInculdeSymbol) {
                // format the number based on currency position
                formattedNumber = if (currencyPosition == "before") {
                    currencySymbol + "0"
                } else {
                    "0$currencySymbol"
                }
            } else {
                formattedNumber = "0"
            }
        }

        when (numberFormat) {
            1, 2 -> {
                // if first char is dot, add 0 before dot
                if (formattedNumber.startsWith(".")) {
                    formattedNumber = "0$formattedNumber"
                }
            }
            3, 4 -> {
                // if first char is comma, add 0 before comma
                if (formattedNumber.startsWith(",")) {
                    formattedNumber = "0$formattedNumber"
                }
            }
        }

        return formattedNumber
    }

    private fun convertStringTextViewToDouble(value: String): Double {

        if (value.isNullOrEmpty()) {
            return 0.0
        }

        var formattedValue: String

        // remove currency symbol, remove space
        formattedValue = value.replace(currencySymbol, "").replace(" ", "")

        when (numberFormat) {
            2 -> { // 10,000.00
                // replace comma with dot
                formattedValue = formattedValue.replace(",", "")
            }
            3 -> { // 10 000,00
                // replace space with nothing, then replace comma with dot
                formattedValue = formattedValue.replace(",", ".")
            }
            4 -> { // 10.000,00
                // replace dot with nothing, then replace comma with dot
                formattedValue = formattedValue.replace(".", "").replace(",", ".")
            }
        }

        return formattedValue.toDouble()
    }
}