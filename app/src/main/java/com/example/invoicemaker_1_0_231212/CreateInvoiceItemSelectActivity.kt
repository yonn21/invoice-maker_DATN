package com.example.invoicemaker_1_0_231212

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.invoicemaker_1_0_231212.adapter.ItemSelectListAdapter
import com.example.invoicemaker_1_0_231212.ads.AdmobRewardAd
import com.example.invoicemaker_1_0_231212.ads.TYPE_ADS
import com.example.invoicemaker_1_0_231212.model.Item
import com.example.invoicemaker_1_0_231212.utils.PreferencesManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Locale

class CreateInvoiceItemSelectActivity : BaseActivity() {

    private lateinit var selectItem_backButton: ImageButton
    private lateinit var selectItem_back: TextView
    private lateinit var selectItem_add: TextView
    private lateinit var selectItem_searchView: SearchView
    private lateinit var selectItem_createItem: Button
    private lateinit var selectItem_recyclerView: RecyclerView
    private lateinit var selectItem_adapter: ItemSelectListAdapter
    private var selectItem_itemList: ArrayList<Item> = arrayListOf()

    private lateinit var settings: JSONObject
    private lateinit var currencyPosition: String // before or after
    private lateinit var currencySymbol: String
    private var decimalPlaces = 2
    private var numberFormat = 2
    private lateinit var dateFormatSetting: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_invoice_item_select)

        val preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
        val editor = preferences.edit()
        val createItemCount = preferences.getInt("createItemCount", 0)
        val maxCreateItem = preferences.getInt("maxCreateItem", 0)

        settings = readSettings()
        currencyPosition = settings.getString("currencyPosition")
        currencySymbol = settings.getString("currencySymbol")
        decimalPlaces = settings.getInt("decimalPlaces")
        numberFormat = settings.getInt("numberFormat")
        dateFormatSetting = settings.getString("dateFormat")

        val settingArray = arrayOf(currencyPosition, currencySymbol, decimalPlaces, numberFormat, dateFormatSetting)

        val scrollPosition = intent.getIntExtra("scrollPosition", 0)

        // Search handle
        selectItem_searchView = findViewById(R.id.Create_Invoice_Select_Item_Search)
        setupSearchView()

        // RecyclerView handle
        selectItem_recyclerView = findViewById(R.id.Create_Invoice_Select_Item_Item_Recycler_View)
        selectItem_recyclerView.layoutManager = LinearLayoutManager(this)

        if (!isItemListEmpty(this) || hasItemListData(this)) {
            loadItemList()
        }

        val invoiceID = intent.getStringExtra("INVOICE_ID") ?: ""

        // Set adapter
        selectItem_adapter = ItemSelectListAdapter(this, selectItem_itemList, scrollPosition, invoiceID, settingArray){ position ->
            // Handle item click
        }
        selectItem_recyclerView.adapter = selectItem_adapter

        // Click button "Create New Item" go to CreateItemActivity
        selectItem_createItem = findViewById(R.id.Create_Invoice_Select_Item_Create_Item_Button)
        selectItem_createItem.setOnClickListener {
            if (PreferencesManager.checkSUB() != null) {

                val intent = Intent(this, CreateItemActivity::class.java)
                intent.putExtra("PREVIOUS_SCREEN", "CreateInvoiceItemSelectActivity")
                intent.putExtra("scrollPosition", scrollPosition)
                intent.putExtra("INVOICE_ID", invoiceID)
                startActivityForResult(intent, 14)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

            } else {
                if (createItemCount <= maxCreateItem) {
                    val intent = Intent(this, CreateItemActivity::class.java)
                    intent.putExtra("PREVIOUS_SCREEN", "CreateInvoiceItemSelectActivity")
                    intent.putExtra("scrollPosition", scrollPosition)
                    intent.putExtra("INVOICE_ID", invoiceID)
                    startActivityForResult(intent, 14)
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    // show subscription_dialog
                    val bottomSheetSubscriptionDialog =
                        BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)
                    val view =
                        LayoutInflater.from(this).inflate(R.layout.subscription_dialog, null)
                    bottomSheetSubscriptionDialog.setContentView(view)

                    view.findViewById<Button>(R.id.Subscription_Button).setOnClickListener {
                        // go to SubscriptionActivity
                        val intent = Intent(this, SubscriptionActivity::class.java)
                        (this as CreateInvoiceItemSelectActivity).startActivityForResult(intent, 1)
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        bottomSheetSubscriptionDialog.dismiss()
                    }

                    view.findViewById<Button>(R.id.Watch_ADS_Button).setOnClickListener {
                        App.getContext().isShowPopup = false

                        showPopupLoadAds(TYPE_ADS.RewardAd)

                        // reload activity after watch ads
                        AdmobRewardAd.Instance.setRewardAdListener { isRewarded ->
                            if (isRewarded) {
                                // set max create item + 3
                                editor.putInt("maxCreateItem", maxCreateItem + 3)
                                editor.apply()
                                val intent = Intent(
                                    this,
                                    CreateInvoiceItemSelectActivity::class.java
                                )
                                intent.putExtra("scrollPosition", scrollPosition)
                                intent.putExtra("INVOICE_ID", invoiceID)
                                startActivity(intent)
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                                finish()
                            }
                        }

                        bottomSheetSubscriptionDialog.dismiss()
                    }

                    bottomSheetSubscriptionDialog.show()
                }
            }
        }

        // Handle button "Add" and "Back"
        selectItem_add = findViewById(R.id.Create_Invoice_Select_Item_Add)
        updateAddButtonVisibility()

        selectItem_add.setOnClickListener {
            saveTempInvoiceData()
            val intent = Intent(this, CreateInvoiceActivity::class.java)
            intent.putExtra("scrollPosition", scrollPosition)
            intent.putExtra("INVOICE_ID", invoiceID)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        selectItem_backButton = findViewById(R.id.Create_Invoice_Select_Item_Button_Back)
        selectItem_backButton.setOnClickListener {
            val intent = Intent(this, CreateInvoiceActivity::class.java)
            intent.putExtra("scrollPosition", scrollPosition)
            intent.putExtra("INVOICE_ID", invoiceID)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        selectItem_back = findViewById(R.id.Create_Invoice_Select_Item_Back)
        selectItem_back.setOnClickListener {
            val intent = Intent(this, CreateInvoiceActivity::class.java)
            intent.putExtra("scrollPosition", scrollPosition)
            intent.putExtra("INVOICE_ID", invoiceID)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    /*override fun onResume() {
        super.onResume()
        // Reload the item list
        selectItem_itemList.clear()
        loadItemList()
        selectItem_adapter.notifyDataSetChanged()
    }*/

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, CreateInvoiceActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    // Check if the item list is empty
    private fun isItemListEmpty(context: Context): Boolean {
        val itemListFile = File(context.filesDir, "ItemList.json")
        return !itemListFile.exists()
    }

    // Check if the item list has data
    private fun hasItemListData(context: Context): Boolean {
        val itemListFile = File(context.filesDir, "ItemList.json")
        return itemListFile.length() > 2
    }

    // Load item list
    private fun loadItemList() {
        val tempInvoiceData = readTempInvoiceData()
        val selectedItems = tempInvoiceData?.optJSONArray("selectedItems")?.let { jsonArray ->
            (0 until jsonArray.length()).associate {
                val item = jsonArray.getJSONObject(it)
                item.getString("id") to item.getInt("quantity")
            }
        } ?: emptyMap()

        val itemListFile = File(this.filesDir, "ItemList.json")
        val itemListJsonString = itemListFile.readText()
        val itemListJsonArray = JSONArray(itemListJsonString)
        selectItem_itemList.clear()

        for (i in 0 until itemListJsonArray.length()) {
            val itemJsonObject = itemListJsonArray.getJSONObject(i)
            val itemId = itemJsonObject.optString("id")

            val item = Item(
                id = itemId,
                image = itemJsonObject.optString("image"),
                name = itemJsonObject.optString("name"),
                description = itemJsonObject.optString("description"),
                stock = itemJsonObject.optString("stock"),
                unitCost = itemJsonObject.optDouble("unitCost"),
                unitType = itemJsonObject.optString("unitType"),
                isSelected = itemId in selectedItems.keys,
                quantity = selectedItems[itemId] ?: 1, // Default quantity to 1 if not in tempInvoiceData
                isRemoved = itemJsonObject.optBoolean("isRemoved")
            )

            selectItem_itemList.add(item)
        }
        selectItem_itemList.sortBy { it.id }
    }


    // setup search
    private fun setupSearchView() {
        selectItem_searchView.setOnClickListener {
            selectItem_searchView.onActionViewExpanded()
        }

        selectItem_searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                selectItem_adapter.filter(newText ?: "")
                return false
            }
        })
    }

    // Handle add button visibility
    fun updateAddButtonVisibility() {
        val isAnyItemSelected = selectItem_itemList.any { it.isSelected }
        if (isAnyItemSelected) {
            selectItem_add.isEnabled = true
            selectItem_add.setTextColor(resources.getColor(R.color.colorPrimary))
        } else {
            selectItem_add.isEnabled = false
            selectItem_add.setTextColor(resources.getColor(R.color.textHint))
        }
    }

    // Read the temp invoice data from the file
    private fun readTempInvoiceData(): JSONObject? {
        val tempFile = File(filesDir, "tempInvoiceData.json")
        return if (tempFile.exists()) {
            val jsonData = tempFile.readText()
            JSONObject(jsonData)
        } else null
    }

    // Save the selected items to the temp invoice data file
    private fun saveTempInvoiceData() {
        val tempInvoiceData = readTempInvoiceData() ?: JSONObject()
        val selectedItemsArray = JSONArray()

        selectItem_itemList.filter { it.isSelected }.forEach { item ->
            val existingItem = (tempInvoiceData.optJSONArray("selectedItems") as? JSONArray)
                ?.let { array ->
                    (0 until array.length())
                        .asSequence()
                        .map { array.getJSONObject(it) }
                        .firstOrNull { it.getString("id") == item.id }
                }

            val itemJson = JSONObject().apply {
                put("id", item.id)
                put("quantity", existingItem?.optInt("quantity") ?: 1)
            }
            selectedItemsArray.put(itemJson)
        }

        tempInvoiceData.put("selectedItems", selectedItemsArray)
        val tempFile = File(filesDir, "tempInvoiceData.json")
        tempFile.writeText(tempInvoiceData.toString())
    }

    private fun readSettings(): JSONObject {
        val file = File(filesDir, "Settings.json")
        return if (file.exists()) {
            JSONObject(file.readText())
        } else {
            JSONObject()
        }
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