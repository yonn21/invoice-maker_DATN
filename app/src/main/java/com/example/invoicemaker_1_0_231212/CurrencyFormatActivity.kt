package com.example.invoicemaker_1_0_231212

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import org.json.JSONObject
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

class CurrencyFormatActivity : BaseActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var backTextView: TextView
    private lateinit var saveTextView: TextView

    private lateinit var exampleTextView: TextView

    private lateinit var currencyPositionLayout: androidx.constraintlayout.widget.ConstraintLayout
    private lateinit var currencyStyleLayout: androidx.constraintlayout.widget.ConstraintLayout

    private lateinit var decimalIncreaseButton: ImageButton
    private lateinit var decimalDecreaseButton: ImageButton
    private lateinit var decimalValue: TextView

    private lateinit var settings: JSONObject
    private lateinit var currencyPosition: String // before or after
    private lateinit var currencySymbol: String
    private var decimalPlaces = 2
    private var numberFormat = 2
    // number format type
    // 1: 10_000.00
    // 2: 10,000.00
    // 3: 10_000,00
    // 4: 10.000,00

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_currency_format)

        backButton = findViewById(R.id.Currency_Format_Button_Back)
        backTextView = findViewById(R.id.Currency_Format_Back)
        saveTextView = findViewById(R.id.Currency_Format_Save)

        exampleTextView = findViewById(R.id.Currency_Format_Example_Value)

        currencyPositionLayout = findViewById(R.id.Currency_Format_Currency_Position_Layout)
        currencyStyleLayout = findViewById(R.id.Currency_Format_Currency_Style_Layout)

        decimalIncreaseButton = findViewById(R.id.Currency_Decimal_Increase)
        decimalDecreaseButton = findViewById(R.id.Currency_Decimal_Decrease)
        decimalValue = findViewById(R.id.Currency_Format_Decimal_Value)

        backButton.setOnClickListener {
            navigate()
        }

        backTextView.setOnClickListener {
            navigate()
        }

        saveTextView.setOnClickListener {
            navigate()
        }

        // hide save text view
        saveTextView.visibility = android.view.View.GONE

        // ================================================================================================

        // read settings from file
        settings = readSettings()

        // set example text view value based on settings
        updateExampleTextView()

        // set currency position layout click listener
        currencyPositionLayout.setOnClickListener {
            showSelectCurrencyPositionDialog()
        }

        // set currency style layout click listener
        currencyStyleLayout.setOnClickListener {
            showSelectCurrencyStyleDialog()
        }

        decimalValue.text = settings.getString("decimalPlaces")

        decimalIncreaseButton.setOnClickListener {
            val value = decimalValue.text.toString().toInt()
            if (value < 3) {
                val newValue = value + 1
                decimalValue.text = newValue.toString()
                settings.put("decimalPlaces", newValue)
                writeSettings(settings)
                updateExampleTextView()
            }
        }

        decimalDecreaseButton.setOnClickListener {
            val value = decimalValue.text.toString().toInt()
            if (value > 0) {
                val newValue = value - 1
                decimalValue.text = newValue.toString()
                settings.put("decimalPlaces", newValue)
                writeSettings(settings)
                updateExampleTextView()
            }
        }
    }

    private fun navigate() {
        val previousScreen = intent.getStringExtra("PREVIOUS_SCREEN")
        val parentScreen = intent.getStringExtra("PARENT_SCREEN")
        val intent = android.content.Intent(this, SettingsActivity::class.java)
        intent.putExtra("PREVIOUS_SCREEN", previousScreen)
        intent.putExtra("PARENT_SCREEN", parentScreen)
        startActivity(intent)
    }

    override fun onBackPressed() {
        navigate()
    }

    private fun readSettings(): JSONObject {
        val file = File(filesDir, "Settings.json")
        return if (file.exists()) {
            JSONObject(file.readText())
        } else {
            JSONObject()
        }
    }

    private fun writeSettings(settings: JSONObject) {
        val file = File(filesDir, "Settings.json")
        file.writeText(settings.toString())
    }

    // show select currency position dialog
    private fun showSelectCurrencyPositionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.select_currency_position_dialog, null)

        val selectBefore = dialogView.findViewById<TextView>(R.id.Select_Currency_Format_Before)
        val selectAfter = dialogView.findViewById<TextView>(R.id.Select_Currency_Format_After)

        val currentCurrencyPosition = settings.getString("currencyPosition")

        if (currentCurrencyPosition == "before") {
            selectBefore.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_bold)
            selectAfter.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_regular)
        } else {
            selectBefore.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_regular)
            selectAfter.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_bold)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setView(dialogView, 160, 0, 160, 0)

        selectBefore.setOnClickListener {
            settings.put("currencyPosition", "before")
            writeSettings(settings)
            updateExampleTextView()
            dialog.dismiss()
        }

        selectAfter.setOnClickListener {
            settings.put("currencyPosition", "after")
            writeSettings(settings)
            updateExampleTextView()
            dialog.dismiss()
        }

        dialog.show()
    }

    // show select currency style dialog
    private fun showSelectCurrencyStyleDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.select_currency_style_dialog, null)

        val style1 = dialogView.findViewById<TextView>(R.id.Select_Currency_Style_1)
        val style2 = dialogView.findViewById<TextView>(R.id.Select_Currency_Style_2)
        val style3 = dialogView.findViewById<TextView>(R.id.Select_Currency_Style_3)
        val style4 = dialogView.findViewById<TextView>(R.id.Select_Currency_Style_4)

        val currentCurrencyStyle = settings.getInt("numberFormat")

        when (currentCurrencyStyle) {
            1 -> {
                style1.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_bold)
                style2.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_regular)
                style3.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_regular)
                style4.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_regular)
            }
            2 -> {
                style1.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_regular)
                style2.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_bold)
                style3.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_regular)
                style4.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_regular)
            }
            3 -> {
                style1.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_regular)
                style2.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_regular)
                style3.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_bold)
                style4.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_regular)
            }
            4 -> {
                style1.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_regular)
                style2.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_regular)
                style3.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_regular)
                style4.typeface = ResourcesCompat.getFont(this, R.font.sf_pro_text_bold)
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setView(dialogView, 160, 0, 160, 0)

        style1.setOnClickListener {
            settings.put("numberFormat", 1)
            writeSettings(settings)
            updateExampleTextView()
            dialog.dismiss()
        }

        style2.setOnClickListener {
            settings.put("numberFormat", 2)
            writeSettings(settings)
            updateExampleTextView()
            dialog.dismiss()
        }

        style3.setOnClickListener {
            settings.put("numberFormat", 3)
            writeSettings(settings)
            updateExampleTextView()
            dialog.dismiss()
        }

        style4.setOnClickListener {
            settings.put("numberFormat", 4)
            writeSettings(settings)
            updateExampleTextView()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateExampleTextView() {
        currencyPosition = settings.getString("currencyPosition")
        currencySymbol = settings.getString("currencySymbol")
        decimalPlaces = settings.getInt("decimalPlaces")
        numberFormat = settings.getInt("numberFormat")

        val exampleNumber = 1234567.0
        var formattedNumber = ""

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
        val formattedExampleNumber = numberFormatter.format(exampleNumber)

        // format the number based on currency position
        formattedNumber = if (currencyPosition == "before") {
            "$currencySymbol$formattedExampleNumber"
        } else {
            "$formattedExampleNumber$currencySymbol"
        }

        // if decimalPlaces is 0, remove the decimal point
        if (decimalPlaces == 0) {
            if (numberFormat == 1 || numberFormat == 2) {
                formattedNumber = formattedNumber.replace(".", "")
            } else {
                formattedNumber = formattedNumber.replace(",", "")
            }
        }

        exampleTextView.text = formattedNumber
    }

}