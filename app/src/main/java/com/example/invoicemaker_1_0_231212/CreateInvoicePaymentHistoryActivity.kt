package com.example.invoicemaker_1_0_231212

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.invoicemaker_1_0_231212.adapter.PaymentAdapter
import com.example.invoicemaker_1_0_231212.model.Payment
import org.json.JSONObject
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Locale

class CreateInvoicePaymentHistoryActivity : BaseActivity() {

    // recycler view
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PaymentAdapter
    private var paymentList: ArrayList<Payment> = arrayListOf()

    private lateinit var settings: JSONObject
    private lateinit var currencyPosition: String // before or after
    private lateinit var currencySymbol: String
    private var decimalPlaces = 2
    private var numberFormat = 2
    private lateinit var dateFormatSetting: String

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_create_invoice_payment_history)

        val buttonBack = findViewById<ImageButton>(R.id.Create_Invoice_Payment_History_Button_Back)
        val textBack = findViewById<TextView>(R.id.Create_Invoice_Payment_History_Back)
        val textAdd = findViewById<TextView>(R.id.Create_Invoice_Payment_History_Add)
        val textAmount = findViewById<TextView>(R.id.Create_Invoice_Payment_History_Total_Amount)

        settings = readSettings()
        currencyPosition = settings.getString("currencyPosition")
        currencySymbol = settings.getString("currencySymbol")
        decimalPlaces = settings.getInt("decimalPlaces")
        numberFormat = settings.getInt("numberFormat")
        dateFormatSetting = settings.getString("dateFormat")

        val scrollPosition = intent.getIntExtra("scrollPosition", 0)

        val invoiceID = intent.getStringExtra("INVOICE_ID") ?: ""

        // click button add to go to create payment activity
        textAdd.setOnClickListener {
            val intent = Intent(this, CreateInvoiceCreatePaymentActivity::class.java)
            intent.putExtra("PREVIOUS_SCREEN", "CreateInvoicePaymentHistoryActivity")
            intent.putExtra("INVOICE_ID", invoiceID)
            intent.putExtra("scrollPosition", scrollPosition)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // click button back and text back to go to create invoice activity
        buttonBack.setOnClickListener {
            val intent = Intent(this, CreateInvoiceActivity::class.java)
            intent.putExtra("PREVIOUS_SCREEN", "CreateInvoicePaymentHistoryActivity")
            intent.putExtra("INVOICE_ID", invoiceID)
            intent.putExtra("scrollPosition", scrollPosition)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        textBack.setOnClickListener {
            val intent = Intent(this, CreateInvoiceActivity::class.java)
            intent.putExtra("PREVIOUS_SCREEN", "CreateInvoicePaymentHistoryActivity")
            intent.putExtra("INVOICE_ID", invoiceID)
            intent.putExtra("scrollPosition", scrollPosition)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // display list of payments
        recyclerView = findViewById(R.id.Create_Invoice_Payment_History_Recycler_View)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load and sort the payment list
        loadPaymentList()

        val settingArray = arrayOf(currencyPosition, currencySymbol, decimalPlaces, numberFormat, dateFormatSetting)

        // Set adapter
        adapter = PaymentAdapter(this, paymentList, scrollPosition, invoiceID, settingArray) { position ->
            // onItemClick(position)
        }
        recyclerView.adapter = adapter

        var totalAmount = 0.0
        // Set total amount
        if (paymentList.isNotEmpty()) {
            for (payment in paymentList) {
                totalAmount += payment.amount
            }
            textAmount.text = setDouleValueBasedOnSettingsToString(totalAmount, true)
        } else {
            textAmount.text = ""
        }

        // balance due = total - total payment amount
        val tempInvoiceData = File(filesDir, "tempInvoiceData.json")
        val tempInvoiceDataJSON = JSONObject(tempInvoiceData.readText())
        val total = tempInvoiceDataJSON.getDouble("total")
        var balanceDue = total - totalAmount
        if (balanceDue < 0.0) balanceDue = 0.0

        // save balance due to tempInvoiceData.json
        tempInvoiceDataJSON.put("balanceDue", balanceDue)
        tempInvoiceData.writeText(tempInvoiceDataJSON.toString())

    }

    override fun onBackPressed() {
        super.onBackPressed()
        val scrollPosition = intent.getIntExtra("scrollPosition", 0)
        val intent = Intent(this, CreateInvoiceActivity::class.java)
        intent.putExtra("PREVIOUS_SCREEN", "CreateInvoicePaymentHistoryActivity")
        intent.putExtra("scrollPosition", scrollPosition)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    // load paymentList from tempInvoiceData.json
    private fun loadPaymentList() {
        val tempInvoiceData = File(filesDir, "tempInvoiceData.json")
        val tempInvoiceDataJSON = JSONObject(tempInvoiceData.readText())
        val tempInvoiceDataJSONPayments = tempInvoiceDataJSON.getJSONArray("paymentList")
        for (i in 0 until tempInvoiceDataJSONPayments.length()) {
            val payment = tempInvoiceDataJSONPayments.getJSONObject(i)
            paymentList.add(
                Payment(
                    payment.getString("id"),
                    payment.getDouble("amount"),
                    payment.getString("date"),
                    payment.getString("method"),
                    payment.getString("note")
                )
            )
        }
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