package com.example.invoicemaker_1_0_231212

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.invoicemaker_1_0_231212.adapter.PaymentInstructionAdapter
import com.example.invoicemaker_1_0_231212.model.Item
import com.example.invoicemaker_1_0_231212.model.PaymentInstruction
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class PaymentInstructionListActivity : BaseActivity() {

    // recycler view
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PaymentInstructionAdapter
    private var paymentInstructionList: ArrayList<PaymentInstruction> = arrayListOf()

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_payment_instruction_list)

        val buttonBack = findViewById<ImageButton>(R.id.Payment_Instruction_List_Button_Back)
        val textBack = findViewById<TextView>(R.id.Payment_Instruction_List_Back)
        val textAdd = findViewById<TextView>(R.id.Payment_Instruction_List_Add)
        var buttonSelect = findViewById<Button>(R.id.Payment_Instruction_List_Button_Select)

        val paymentInstructionSelectAllIcon: ImageView = findViewById(R.id.Payment_Instruction_List_Select_All_Icon)
        val paymentInstructionSelectAllTitle: TextView = findViewById(R.id.Payment_Instruction_List_Select_All_Title)

        val scrollPosition = intent.getIntExtra("scrollPosition", 0)

        val invoiceID = intent.getStringExtra("INVOICE_ID") ?: ""

        if (intent.getStringExtra("PREVIOUS_SCREEN") == "SettingsActivity" || intent.getStringExtra("PREVIOUS_SCREEN") == "SettingPaymentInstructionListActivity") {

            // hide select, select all icon and title
            buttonSelect.visibility = Button.GONE
            paymentInstructionSelectAllIcon.visibility = ImageView.GONE
            paymentInstructionSelectAllTitle.visibility = TextView.GONE

            val parentScreen = intent.getStringExtra("PARENT_SCREEN")

            // click button back and text back to go to settings activity
            buttonBack.setOnClickListener {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.putExtra("PARENT_SCREEN", parentScreen)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
            textBack.setOnClickListener {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.putExtra("PARENT_SCREEN", parentScreen)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }

            // click button add to go to create payment instruction activity
            textAdd.setOnClickListener {
                val intent = Intent(this, CreatePaymentInstructionActivity::class.java)
                intent.putExtra("PREVIOUS_SCREEN", "SettingPaymentInstructionListActivity")
                intent.putExtra("PARENT_SCREEN", parentScreen)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }

        } else {

            // click button select to go to create invoice activity
            buttonSelect.setOnClickListener {
                saveTempInvoiceData()
                val intent = Intent(this, CreateInvoiceActivity::class.java)
                intent.putExtra("PREVIOUS_SCREEN", "PaymentInstructionListActivity")
                intent.putExtra("scrollPosition", scrollPosition)
                intent.putExtra("INVOICE_ID", invoiceID)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }

            // click button back and text back to go to create invoice activity
            buttonBack.setOnClickListener {
                val intent = Intent(this, CreateInvoiceActivity::class.java)
                intent.putExtra("PREVIOUS_SCREEN", "PaymentInstructionListActivity")
                intent.putExtra("scrollPosition", scrollPosition)
                intent.putExtra("INVOICE_ID", invoiceID)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
            textBack.setOnClickListener {
                val intent = Intent(this, CreateInvoiceActivity::class.java)
                intent.putExtra("PREVIOUS_SCREEN", "PaymentInstructionListActivity")
                intent.putExtra("scrollPosition", scrollPosition)
                intent.putExtra("INVOICE_ID", invoiceID)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }

            // click button add to go to create payment instruction activity
            textAdd.setOnClickListener {
                val intent = Intent(this, CreatePaymentInstructionActivity::class.java)
                intent.putExtra("PREVIOUS_SCREEN", "CreateInvoicePaymentInstructionListActivity")
                intent.putExtra("scrollPosition", scrollPosition)
                intent.putExtra("INVOICE_ID", invoiceID)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }

        }

        // display list of payments
        recyclerView = findViewById(R.id.Payment_Instruction_List_Recycler_View)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load and sort the payment list
        loadPaymentInstructionList()

        // if previous screen is settings
        if (intent.getStringExtra("PREVIOUS_SCREEN") == "SettingsActivity" || intent.getStringExtra("PREVIOUS_SCREEN") == "SettingPaymentInstructionListActivity") {
            val parentScreen = intent.getStringExtra("PARENT_SCREEN") ?: ""

            adapter = PaymentInstructionAdapter(this, paymentInstructionList, scrollPosition, invoiceID, true, parentScreen) { position ->
                // onItemClick(position)
            }
            recyclerView.adapter = adapter

            val layoutParams = recyclerView.layoutParams as ConstraintLayout.LayoutParams
            val marginInDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 30f, resources.displayMetrics
            ).toInt()
            layoutParams.setMargins(0, marginInDp, 0, 0) // set only top margin
            recyclerView.layoutParams = layoutParams
        }
        else {
            adapter = PaymentInstructionAdapter(this, paymentInstructionList, scrollPosition, invoiceID, false, "CreateInvoice") { position ->
                // onItemClick(position)
            }
            recyclerView.adapter = adapter
        }
    }

    override fun onBackPressed() {
        if (intent.getStringExtra("PREVIOUS_SCREEN") == "SettingsActivity" || intent.getStringExtra("PREVIOUS_SCREEN") == "SettingPaymentInstructionListActivity") {
            val parentScreen = intent.getStringExtra("PARENT_SCREEN")
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("PARENT_SCREEN", parentScreen)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        } else {
            val scrollPosition = intent.getIntExtra("scrollPosition", 0)
            val invoiceID = intent.getStringExtra("INVOICE_ID") ?: ""
            val intent = Intent(this, CreateInvoiceActivity::class.java)
            intent.putExtra("PREVIOUS_SCREEN", "PaymentInstructionListActivity")
            intent.putExtra("scrollPosition", scrollPosition)
            intent.putExtra("INVOICE_ID", invoiceID)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
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

    // load payment instruction from PaymentInstructionList.json
    private fun loadPaymentInstructionList() {
        val tempInvoiceData = readTempInvoiceData()

        val selectedItemsArray = tempInvoiceData?.optJSONArray("selectedPaymentInstructions")

        // if selectedPaymentInstructions is not null, get the selected items to check isSelected
        val selectedItems = if (selectedItemsArray != null) {
            val selectedItemsList = mutableListOf<String>()
            for (i in 0 until selectedItemsArray.length()) {
                selectedItemsList.add(selectedItemsArray.getString(i))
            }
            selectedItemsList
        } else {
            emptyList()
        }

        val itemListFile = File(this.filesDir, "PaymentInstruction.json")
        val itemListJsonString = itemListFile.readText()
        val itemListJsonArray = JSONArray(itemListJsonString)
        paymentInstructionList.clear()

        for (i in 0 until itemListJsonArray.length()) {
            val itemJsonObject = itemListJsonArray.getJSONObject(i)
            val itemId = itemJsonObject.optString("id")

            val item = PaymentInstruction(
                id = itemId,
                payableTo = itemJsonObject.optString("payableTo"),
                paymentDetail = itemJsonObject.optString("paymentDetail"),
                method = itemJsonObject.optString("method"),
                isSelected = selectedItems.contains(itemId)
            )

            paymentInstructionList.add(item)
        }
    }

    // Save the selected items to the temp invoice data file
    private fun saveTempInvoiceData() {
        val tempInvoiceData = readTempInvoiceData() ?: JSONObject()
        val selectedItemsArray = JSONArray()

        paymentInstructionList.filter { it.isSelected }.forEach { item ->
            selectedItemsArray.put(item.id)
        }

        tempInvoiceData.put("selectedPaymentInstructions", selectedItemsArray)
        val tempFile = File(filesDir, "tempInvoiceData.json")
        tempFile.writeText(tempInvoiceData.toString())
    }
}