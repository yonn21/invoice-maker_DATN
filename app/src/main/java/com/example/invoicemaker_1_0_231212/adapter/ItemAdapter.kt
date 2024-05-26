package com.example.invoicemaker_1_0_231212.adapter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SwipeLayout
import com.example.invoicemaker_1_0_231212.CreateItemActivity
import com.example.invoicemaker_1_0_231212.ItemListActivity
import com.example.invoicemaker_1_0_231212.R
import com.example.invoicemaker_1_0_231212.model.Item
import org.json.JSONArray
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Locale

// ItemList
class ItemAdapter(
    private val context: Context,
    private val items: ArrayList<Item>,
    private val settingArray: Array<out Any>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.Item_Image)
        val nameTextView: TextView = view.findViewById(R.id.Item_Name)
        val descriptionTextView: TextView = view.findViewById(R.id.Item_Description)
        val stockTextView: TextView = view.findViewById(R.id.Item_Stock)
        val unitCostTextView: TextView = view.findViewById(R.id.Item_Cost)

        val itemListLayout: ConstraintLayout = view.findViewById(R.id.Item_Recycler_Layout)

        var isSwiping = false
        var lastSwipeTime: Long = 0
    }

    private var currencyPosition = settingArray[0].toString()
    private var currencySymbol = settingArray[1].toString()
    private var decimalPlaces = settingArray[2].toString().toInt()
    private var numberFormat = settingArray[3].toString().toInt()
    private var dateFormatSetting = settingArray[4].toString()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_recycler_view, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]

        if (items.size == 1) {
            holder.itemListLayout.setBackgroundResource(R.drawable.corners_rounded_full)
        } else {
            when (position) {
                0 -> holder.itemListLayout.setBackgroundResource(R.drawable.corners_rounded_top)
                items.size - 1 -> holder.itemListLayout.setBackgroundResource(R.drawable.corners_rounded_bottom)
            }
        }

        holder.nameTextView.text = item.name
        holder.descriptionTextView.text = if (item.description == "") {
            "No description"
        } else {
            item.description
        }

        holder.stockTextView.text = if (item.stock == "") {
            "Stock quantity has not been set"
        } else {
            "${item.stock} in stock"
        }

        if (item.stock == "0") {
            holder.stockTextView.setTextColor(context.getColor(R.color.red))
            holder.stockTextView.text = "Out of stock"
        }

        holder.unitCostTextView.text = setDouleValueBasedOnSettingsToString(item.unitCost, true)

        val imagePath = context.filesDir.resolve("ItemPhoto/${item.id}/photo_0.jpg")
        if (imagePath.exists()) {
            // Resize the ImageView to the desired dimensions
            val layoutParams = holder.imageView.layoutParams
            layoutParams.width = 61 * context.resources.displayMetrics.density.toInt() // Convert dp to pixels
            layoutParams.height = 61 * context.resources.displayMetrics.density.toInt()
            holder.imageView.layoutParams = layoutParams

            // Set the image to ImageView from the file path
            val bitmap = BitmapFactory.decodeFile(imagePath.absolutePath)
            holder.imageView.setImageBitmap(bitmap)
        } else {
            // If the image file does not exist, load a default image
            holder.imageView.setImageResource(R.drawable.item_default_photo)
        }

        val swipeLayout = holder.itemView.findViewById<SwipeLayout>(R.id.Item_Swipe_Layout)
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, null)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.itemView.findViewById(R.id.Item_Layout_Remove))

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

        // click item in recycler view to edit
        holder.itemView.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - holder.lastSwipeTime > 300 && !holder.isSwiping) {
                // open edit screen if the time from the last swipe is long enough
                val intent = Intent(context, CreateItemActivity::class.java)
                intent.putExtra("PREVIOUS_SCREEN", "ItemListActivity")
                intent.putExtra("ITEM_ID", item.id)
                context.startActivity(intent)
            }
        }

        val removeButton = holder.itemView.findViewById<ImageButton>(R.id.Item_Remove_Button)
        removeButton.setOnClickListener {
            // alert dialog
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle("Remove Item")
            alertDialog.setMessage("Are you sure you want to remove this item?")
            alertDialog.setPositiveButton("Remove", DialogInterface.OnClickListener { _, _ ->

                // val itemToRemove = items[position]

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

                // if item list is empty, display button "New Item"
                if (items.isEmpty()) {
                    val buttonCreateNewItem = (context as ItemListActivity).findViewById<Button>(R.id.Item_List_Button_New_Item)
                    val imageEmptyItem = (context as ItemListActivity).findViewById<ImageView>(R.id.Item_List_Image_Empty)
                    val textEmptyItem = (context as ItemListActivity).findViewById<TextView>(R.id.Item_List_Empty_Text)
                    val cardCreateNewItem = (context as ItemListActivity).findViewById<CardView>(R.id.Item_List_Button_New_Item_Card)
                    val buttonAddItem = context.findViewById<ImageButton>(R.id.Item_List_Button_Add_Item)

                    // Hide search view
                    val searchView = context.findViewById<SearchView>(R.id.Item_List_Search)
                    val searchViewLayout = context.findViewById<CardView>(R.id.Item_List_Search_Layout)
                    searchView.visibility = SearchView.INVISIBLE
                    searchViewLayout.visibility = CardView.GONE
                    searchView.isClickable = false

                    // Display button "New Item"
                    buttonCreateNewItem.visibility = Button.VISIBLE
                    imageEmptyItem.visibility = ImageView.VISIBLE
                    textEmptyItem.visibility = TextView.VISIBLE
                    cardCreateNewItem.visibility = CardView.VISIBLE
                    buttonCreateNewItem.isClickable = true
                    // Hide button "Add Item"
                    buttonAddItem.visibility = Button.INVISIBLE
                    buttonAddItem.isClickable = false

                    // click button "New Item" go to CreateItemActivity
                    buttonCreateNewItem.setOnClickListener {
                        val intent = Intent(context, CreateItemActivity::class.java)
                        intent.putExtra("PREVIOUS_SCREEN", "ItemListActivity")
                        context.startActivity(intent)
                    }
                }

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
            // convert base on date format setting ("MM/dd/yyyy")
            val dateFormat = SimpleDateFormat(dateFormatSetting, Locale.getDefault())
            val dateObj = dateFormat.parse(date)
            return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateObj)
        }
    }

    private fun setDouleValueBasedOnSettingsToString(value: Double, isInculdeSymbol: Boolean): String {

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