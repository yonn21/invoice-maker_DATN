package com.example.invoicemaker_1_0_231212

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.invoicemaker_1_0_231212.model.PaymentInstruction
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream

class CreatePaymentInstructionActivity : BaseActivity() {

    // Method list spinner
    private lateinit var methodListSpinner: Spinner
    private val newMethodPrompt = "Enter new custom payment method"

    private lateinit var saveButton: TextView
    private lateinit var backTextButton: TextView
    private lateinit var backButton: ImageButton
    private lateinit var payableTo: EditText
    private lateinit var paymentDetail: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_payment_instruction)

        saveButton = findViewById(R.id.Add_Payment_Instruction_Save)
        backTextButton = findViewById(R.id.Add_Payment_Instruction_Back)
        backButton = findViewById(R.id.Add_Payment_Instruction_Button_Back)
        payableTo = findViewById(R.id.Add_Payment_Instruction_Payable_To_Field)
        paymentDetail = findViewById(R.id.Add_Payment_Payment_Detail_Field)

        // Disable save button if payableTo is empty
        if (payableTo.text.isEmpty()) {
            saveButton.isEnabled = false
            saveButton.setTextColor(Color.parseColor("#B3B3B3"))
        }

        // Enable save button if payableTo is not empty
        payableTo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                if (payableTo.text.isEmpty()) {
                    saveButton.isEnabled = false
                    payableTo.error = "Required"
                    saveButton.setTextColor(Color.parseColor("#B3B3B3"))
                } else {
                    saveButton.isEnabled = true
                    payableTo.error = null
                    saveButton.setTextColor(Color.parseColor("#007AFF"))
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        // Spinner for payment method
        methodListSpinner = findViewById(R.id.Add_Payment_Instruction_Type_Spinner)
        loadPaymentMethods()

        methodListSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val selected = parent.getItemAtPosition(position).toString()
                if (selected == newMethodPrompt) {
                    // Show dialog or activity to enter a new custom method
                    promptForNewPaymentMethod()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Load item data if the activity is opened for editing
        val itemId = intent.getStringExtra("PAYMENT_INS_ID")
        if (itemId != null) {
            // Change the title to "Edit Item"
            val title = findViewById<TextView>(R.id.Add_Payment_Instruction_Title)
            title.text = "Edit Payment Instruction"
            loadItemData(itemId)
        }

        // Save
        saveButton.setOnClickListener {
            savePaymentInstructionData()
            // toast and make it in center of screen
            val toast = Toast.makeText(this, "Payment Instruction added", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
            navigate()
        }

        // Back
        backButton.setOnClickListener {
            navigate()
        }
        backTextButton.setOnClickListener {
            navigate()
        }
    }

    // Load item data
    private fun loadItemData(itemId: String) {
        val itemListJsonString = File(this.filesDir, "PaymentInstruction.json").readText()
        val itemListJsonArray = JSONArray(itemListJsonString)
        for (i in 0 until itemListJsonArray.length()) {
            val itemJsonObject = itemListJsonArray.getJSONObject(i)
            if (itemJsonObject.getString("id") == itemId) {
                payableTo.setText(itemJsonObject.getString("payableTo"))
                paymentDetail.setText(itemJsonObject.getString("paymentDetail"))
                // Set spinner selection based on method
                val method = itemJsonObject.getString("method")
                val methodListJsonString = File(this.filesDir, "PaymentMethodList.json").readText()
                val methodListJsonArray = JSONArray(methodListJsonString)
                for (j in 0 until methodListJsonArray.length()) {
                    val methodJsonObject = methodListJsonArray.getJSONObject(j)
                    if (methodJsonObject.getString("paymentMethodName") == method) {
                        methodListSpinner.setSelection(j)
                        break
                    }
                }
                break
            }
        }
    }

    private fun navigate() {
        if (intent.getStringExtra("PREVIOUS_SCREEN") == "CreateInvoicePaymentInstructionListActivity" || intent.getStringExtra("PARENT_SCREEN") == "CreateInvoice") {
            var scrollPosition = intent.getIntExtra("scrollPosition", 0)
            val invoiceID = intent.getStringExtra("INVOICE_ID")

            val intent = Intent(this, PaymentInstructionListActivity::class.java)
            intent.putExtra("scrollPosition", scrollPosition)
            intent.putExtra("INVOICE_ID", invoiceID)
            intent.putExtra("PREVIOUS_SCREEN", "CreateInvoicePaymentInstructionListActivity")

            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        } else {
            val parentScreen = intent.getStringExtra("PARENT_SCREEN")
            val intent = Intent(this, PaymentInstructionListActivity::class.java)
            intent.putExtra("PREVIOUS_SCREEN", "SettingPaymentInstructionListActivity")
            intent.putExtra("PARENT_SCREEN", parentScreen)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    override fun onBackPressed() {
        navigate()
    }

    private fun generateItemId(): String {
        return "${System.currentTimeMillis()}"
    }

    private fun savePaymentInstructionData() {
        // check edit or create
        val isEditing = intent.hasExtra("PAYMENT_INS_ID")
        if (isEditing) {
            // edit
            val itemId = intent.getStringExtra("PAYMENT_INS_ID") ?: return
            val itemListJsonString = File(this.filesDir, "PaymentInstruction.json").readText()
            val itemListJsonArray = JSONArray(itemListJsonString)
            for (i in 0 until itemListJsonArray.length()) {
                val itemJsonObject = itemListJsonArray.getJSONObject(i)
                if (itemJsonObject.getString("id") == itemId) {
                    itemJsonObject.put("payableTo", payableTo.text.toString())
                    itemJsonObject.put("paymentDetail", paymentDetail.text.toString().ifEmpty { "" })
                    itemJsonObject.put("method", methodListSpinner.selectedItem.toString())
                    break
                }
            }
            openFileOutput("PaymentInstruction.json", MODE_PRIVATE).use { output ->
                output.write(itemListJsonArray.toString().toByteArray())
            }
        } else {
            // create
            // Generate a unique ID for the new item
            val paymentInstructionID = generateItemId()

            // Prepare the item data as a JSON object
            val paymentInstructionObject = JSONObject().apply {
                put("id", paymentInstructionID)
                put("payableTo", payableTo.text.toString())
                put("paymentDetail", paymentDetail.text.toString().ifEmpty { "" })
                put("method", methodListSpinner.selectedItem.toString())
            }

            val paymentInstructionFile = File(filesDir, "PaymentInstruction.json")
            val paymentInstructionList = if (paymentInstructionFile.exists()) {
                // Read the existing JSON array
                JSONArray(paymentInstructionFile.readText())
            } else {
                // Create a new JSON array if the file doesn't exist
                JSONArray()
            }

            // Add the new paymentInstructionObject to the array
            paymentInstructionList.put(paymentInstructionObject)

            // Write the updated array back to file
            openFileOutput("PaymentInstruction.json", MODE_PRIVATE).use { output ->
                output.write(paymentInstructionList.toString().toByteArray())
            }
        }

        // Sort the payment instruction data by payableTo
        sortPaymentInstructionData()
    }

    // sort by payableTo
    private fun sortPaymentInstructionData() {
        val paymentInstructionFile = File(filesDir, "PaymentInstruction.json")
        val paymentInstructionList = if (paymentInstructionFile.exists()) {
            JSONArray(paymentInstructionFile.readText())
        } else {
            JSONArray()
        }

        val paymentInstructions = mutableListOf<PaymentInstruction>()
        for (i in 0 until paymentInstructionList.length()) {
            val itemJsonObject = paymentInstructionList.getJSONObject(i)
            val paymentInstruction = PaymentInstruction(
                id = itemJsonObject.getString("id"),
                payableTo = itemJsonObject.getString("payableTo").toUpperCase(),
                method = itemJsonObject.getString("method"),
                paymentDetail = itemJsonObject.getString("paymentDetail")
            )
            paymentInstructions.add(paymentInstruction)
        }

        paymentInstructions.sortWith(compareBy { it.payableTo })

        val sortedPaymentInstructionList = JSONArray()
        for (paymentInstruction in paymentInstructions) {
            val itemJsonObject = JSONObject().apply {
                put("id", paymentInstruction.id)
                put("payableTo", paymentInstruction.payableTo)
                put("method", paymentInstruction.method)
                put("paymentDetail", paymentInstruction.paymentDetail)
            }
            sortedPaymentInstructionList.put(itemJsonObject)
        }

        openFileOutput("PaymentInstruction.json", MODE_PRIVATE).use { output ->
            output.write(sortedPaymentInstructionList.toString().toByteArray())
        }
    }

    // Load methods
    private fun loadPaymentMethods() {
        val inputStream: InputStream = openFileInput("PaymentMethodList.json")
        val json = inputStream.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(json)
        val methods = ArrayList<String>()

        // Add methods to the list from JSON
        for (i in 0 until jsonArray.length()) {
            val method = jsonArray.getJSONObject(i).getString("paymentMethodName")
            methods.add(method)
        }

        // Add the "Enter new custom" option at the end
        methods.add(newMethodPrompt)

        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, methods) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view: View = super.getDropDownView(position, convertView, parent)
                // Change the color for the "Enter new custom" option
                if (position == methods.size - 1) {
                    (view as TextView).setTextColor(Color.parseColor("#B3B3B3"))
                } else {
                    (view as TextView).setTextColor(Color.BLACK) // For other items, use the default color
                }
                return view
            }

            override fun isEnabled(position: Int): Boolean {
                // All items are enabled
                return true
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        methodListSpinner.adapter = adapter

        methodListSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                if (selected == newMethodPrompt) {
                    promptForNewPaymentMethod()
                    // Reset the spinner to some default position
                    methodListSpinner.setSelection(0)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        })

        methodListSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                if (selected == newMethodPrompt) {
                    promptForNewPaymentMethod()
                }
                // Do not reset the spinner here as it will interfere with normal selection
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No implementation needed here
            }
        }
    }

    // Add new method
    private fun promptForNewPaymentMethod() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("New Custom Method")
            .setView(input)
            .setPositiveButton("Add", null) // Set to null. We will override the click listener later.
            .setNegativeButton("Cancel") { dialog, whichButton ->
                methodListSpinner.setSelection(0)
            }
            .create()

        dialog.setOnShowListener {
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.isEnabled = false // Initially disable the "Add" button

            input.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    addButton.isEnabled = s?.isNotEmpty() == true
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            addButton.setOnClickListener {
                val newMethod = input.text.toString()
                if (newMethod.isNotEmpty()) {
                    val position = addNewPaymentMethod(newMethod)
                    methodListSpinner.setSelection(position) // Select new added
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun addNewPaymentMethod(newPaymentMethod: String): Int {
        // Open the existing JSON file
        val inputStream: InputStream = openFileInput("PaymentMethodList.json")
        val json = inputStream.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(json)

        // Create a new JSON object for the new method
        val newMethodObject = JSONObject()
        newMethodObject.put("paymentMethodName", newPaymentMethod)

        // Add the new method JSON object to the existing array
        jsonArray.put(newMethodObject)

        // Save the updated JSON array back to the file
        openFileOutput("PaymentMethodList.json", MODE_PRIVATE).use { output ->
            output.write(jsonArray.toString().toByteArray())
        }

        val toast = Toast.makeText(this, "Added \"$newPaymentMethod\" method", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()

        // Refresh the spinner data to include the new method
        loadPaymentMethods()

        return jsonArray.length() - 1
    }

}