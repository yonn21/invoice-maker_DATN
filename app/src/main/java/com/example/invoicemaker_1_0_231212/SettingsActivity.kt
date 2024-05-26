package com.example.invoicemaker_1_0_231212

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.invoicemaker_1_0_231212.adapter.CurrencyAdapter
import com.example.invoicemaker_1_0_231212.model.Currency
import com.example.invoicemaker_1_0_231212.model.Settings
import com.example.invoicemaker_1_0_231212.utils.PreferencesManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class SettingsActivity : BaseActivity() {
    
    private lateinit var businessProfileLayout: androidx.constraintlayout.widget.ConstraintLayout
    private lateinit var paymentInstructionLayout: androidx.constraintlayout.widget.ConstraintLayout
    private lateinit var currencyLayout: androidx.constraintlayout.widget.ConstraintLayout
    private lateinit var editCurrencyFormatLayout: androidx.constraintlayout.widget.ConstraintLayout
    private lateinit var dateFormatLayout: androidx.constraintlayout.widget.ConstraintLayout

    private lateinit var rateAppLayout: androidx.constraintlayout.widget.ConstraintLayout
    private lateinit var feedbackLayout: androidx.constraintlayout.widget.ConstraintLayout
    private lateinit var privacyPolicyLayout: androidx.constraintlayout.widget.ConstraintLayout
    private lateinit var termsOfUseLayout: androidx.constraintlayout.widget.ConstraintLayout
    private lateinit var moreAppsLayout: androidx.constraintlayout.widget.ConstraintLayout
    
    private lateinit var currencyValue: TextView
    private lateinit var dateValue: TextView

    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val switchSub = findViewById<Switch>(R.id.switchSub)

        switchSub.isChecked = PreferencesManager.checkSUB() != null

        switchSub.setOnCheckedChangeListener { _, b ->
            if (b) PreferencesManager.purchaseAndRestoreSuccess() else PreferencesManager.purchaseFailed()
            checkSubToUpdateUI()
        }

        currencyValue = findViewById(R.id.Setting_Currency_Value)
        dateValue = findViewById(R.id.Setting_Date_Value)

        backButton = findViewById(R.id.Setting_Back_Button)

        businessProfileLayout = findViewById(R.id.Setting_Business_Profile_Layout)
        paymentInstructionLayout = findViewById(R.id.Setting_Payment_Instruction_Layout)
        currencyLayout = findViewById(R.id.Setting_Currency_Layout)
        editCurrencyFormatLayout = findViewById(R.id.Setting_Edit_Currency_Format_Layout)
        dateFormatLayout = findViewById(R.id.Setting_Date_Format_Layout)

        rateAppLayout = findViewById(R.id.Setting_Rate_App_Layout)
        feedbackLayout = findViewById(R.id.Setting_Feedback_Layout)
        privacyPolicyLayout = findViewById(R.id.Setting_Privacy_Policy_Layout)
        termsOfUseLayout = findViewById(R.id.Setting_Terms_Of_Use_Layout)
        moreAppsLayout = findViewById(R.id.Setting_More_Apps_Layout)

        feedbackLayout.visibility = android.view.View.GONE

        val parentScreen = intent.getStringExtra("PREVIOUS_SCREEN")
        // back
        backButton.setOnClickListener {
            NavigateToPreviousScreen()
        }

        // read Settings.json to set Setting_Currency_Value and Setting_Date_Value
        setTextViewValue()

        // business profile
        businessProfileLayout.setOnClickListener {
            val intent = Intent(this, BusinessDataActivity::class.java)
            intent.putExtra("PREVIOUS_SCREEN", "SettingsActivity")
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // payment instruction
        paymentInstructionLayout.setOnClickListener {
            val intent = Intent(this, PaymentInstructionListActivity::class.java)
            intent.putExtra("PREVIOUS_SCREEN", "SettingsActivity")
            intent.putExtra("PARENT_SCREEN", parentScreen)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // edit currency format click go to CurrencyFormatActivity
        editCurrencyFormatLayout.setOnClickListener {
            val intent = Intent(this, CurrencyFormatActivity::class.java)
            intent.putExtra("PREVIOUS_SCREEN", "SettingsActivity")
            intent.putExtra("PARENT_SCREEN", parentScreen)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // currency
        currencyLayout.setOnClickListener {
            showSelectCurrencyDialog()
        }

        // date format
        dateFormatLayout.setOnClickListener {
            showSelectDateFormatDialog()
        }

        // rate app

        // feedback

        // privacy policy
        privacyPolicyLayout.setOnClickListener {
            val intent = Intent(this, PrivacyPolicyActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // terms of use
        termsOfUseLayout.setOnClickListener {
            val intent = Intent(this, TermsOfUseActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun NavigateToPreviousScreen() {
        val previousScreen = intent.getStringExtra("PREVIOUS_SCREEN")
        val parentScreen = intent.getStringExtra("PARENT_SCREEN")
        println(parentScreen)
        println(previousScreen)
        when (previousScreen) {
            "ClientListActivity" -> {
                val intent = Intent(this, ClientListActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return
            }
            "ItemListActivity" -> {
                val intent = Intent(this, ItemListActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return
            }
            "MainActivity" -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return
            }
        }
        when (parentScreen) {
            "ClientListActivity" -> {
                val intent = Intent(this, ClientListActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return
            }
            "ItemListActivity" -> {
                val intent = Intent(this, ItemListActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return
            }
            "MainActivity" -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return
            }
        }
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onBackPressed() {
        NavigateToPreviousScreen()
    }

    // read Settings.json
    private fun readSettings(): Settings {
        val file = File(filesDir, "Settings.json")
        val jsonString = file.readText()
        return Gson().fromJson(jsonString, Settings::class.java)
    }

    // set Setting_Currency_Value and Setting_Date_Value
    private fun setTextViewValue() {
        // read Settings.json form internal storage
        val settings = readSettings()

        // set currency value
        currencyValue.text = settings.currency
        // set date value
        dateValue.text = settings.dateFormat
    }

    // show select currency dialog
    private fun showSelectCurrencyDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.select_currency_dialog, null)
        val closeTextView = dialogView.findViewById<TextView>(R.id.Select_Currency_Close_Text)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setView(dialogView, 30, 40, 30, 40)

        // setup currency_recycler_view
        val currencyRecyclerView = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.Select_Currency_Recycler_View)
        currencyRecyclerView.layoutManager = LinearLayoutManager(this)

        // read and paste JSON from file
        val jsonString = File(filesDir, "currency_list.json").readText()
        val type = object : TypeToken<List<Currency>>() {}.type
        val currencies = Gson().fromJson<List<Currency>>(jsonString, type)

        // Setup adapter
        val adapter = CurrencyAdapter(currencies) { currency ->
            // update settings.json when currency selected
            updateSettingsWithSelectedCurrency(currency)
            dialog.dismiss()
        }
        currencyRecyclerView.adapter = adapter

        // setup search view
        val searchView = dialogView.findViewById<SearchView>(R.id.Select_Currency_Search)
        searchView.setOnClickListener {
            searchView.onActionViewExpanded()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return false
            }
        })

        closeTextView.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateSettingsWithSelectedCurrency(selectedCurrency: Currency) {
        val settings = readSettings().apply {
            currency = selectedCurrency.code
            currencySymbol = selectedCurrency.symbol
        }

        val file = File(filesDir, "Settings.json")
        file.writeText(Gson().toJson(settings))

        // update currency value
        currencyValue.text = selectedCurrency.code
    }

    // show select date format dialog
    private fun showSelectDateFormatDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.select_date_format_dialog, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setView(dialogView, 160, 0, 160, 0)

        val formats = listOf("MM/dd/yyyy", "dd/MM/yyyy")
        val textViews = listOf(
            dialogView.findViewById<TextView>(R.id.Select_Date_Format_mmddyyyy),
            dialogView.findViewById<TextView>(R.id.Select_Date_Format_ddmmyyyy)
        )

        val currentFormat = readSettings().dateFormat
        // Highlight the current format and set click listeners
        textViews.forEachIndexed { index, textView ->
            textView.text = formats[index]
            if (formats[index] == currentFormat) {
                textView.setTypeface(ResourcesCompat.getFont(this, R.font.sf_pro_text_bold), Typeface.NORMAL)
            } else {
                textView.setTypeface(ResourcesCompat.getFont(this, R.font.sf_pro_text_regular), Typeface.NORMAL)
            }
            textView.setOnClickListener {
                // Update settings.json with the selected date format
                updateSettingsWithSelectedDateFormat(formats[index])
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun updateSettingsWithSelectedDateFormat(selectedFormat: String) {
        val settings = readSettings().apply {
            dateFormat = selectedFormat
        }

        val file = File(filesDir, "Settings.json")
        file.writeText(Gson().toJson(settings))

        // update date value
        dateValue.text = selectedFormat
    }
}