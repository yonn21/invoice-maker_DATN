package com.example.invoicemaker_1_0_231212

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class CreateClientActivity : BaseActivity() {

    // Request code for picking a contact
    private val PICK_CONTACT_REQUEST = 128
    private val PERMISSIONS_REQUEST_READ_CONTACTS = 921

    private lateinit var clientName: EditText
    private lateinit var clientPhone: EditText
    private lateinit var clientEmail: EditText
    private lateinit var clientAddress: EditText
    private lateinit var doneButton: TextView
    private lateinit var backTextButton: TextView
    private lateinit var backButton: ImageButton
    private lateinit var contactIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_client)

        clientName = findViewById(R.id.Create_Client_Name_Field)
        clientPhone = findViewById(R.id.Create_Client_Phone_Field)
        clientEmail = findViewById(R.id.Create_Client_Email_Field)
        clientAddress = findViewById(R.id.Create_Client_Address_Field)
        doneButton = findViewById(R.id.Create_Client_Done)
        backTextButton = findViewById(R.id.Create_Client_Back_Text)
        backButton = findViewById(R.id.Create_Client_Back_Button)
        contactIcon = findViewById(R.id.Create_Client_Contact_Icon)

        App.isSkipOpenAd = false

        // Disable Done Button if clientName or clientPhone is empty
        if (clientName.text.isNullOrEmpty() || clientPhone.text.isNullOrEmpty()) {
            doneButton.isEnabled = false
            doneButton.setTextColor(Color.parseColor("#B3B3B3"))
        }

        // Enable Done Button if clientName and clientPhone are not empty
        clientName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (clientName.text.isNullOrEmpty() || clientPhone.text.isNullOrEmpty()) {
                    doneButton.isEnabled = false
                    doneButton.setTextColor(Color.parseColor("#B3B3B3"))
                } else {
                    doneButton.isEnabled = true
                    doneButton.setTextColor(Color.parseColor("#007AFF"))
                }

                if (clientName.text.isNullOrEmpty()) {
                    clientName.error = "Client name is required"
                    doneButton.isEnabled = false
                }

                App.isSkipOpenAd = false
            }
            override  fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        clientPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (clientName.text.isNullOrEmpty() || clientPhone.text.isNullOrEmpty()) {
                    doneButton.isEnabled = false
                    doneButton.setTextColor(Color.parseColor("#B3B3B3"))
                } else {
                    doneButton.isEnabled = true
                    doneButton.setTextColor(Color.parseColor("#007AFF"))
                }

                if (clientPhone.text.isNullOrEmpty()) {
                    clientPhone.error = "Phone number is required"
                }

                App.isSkipOpenAd = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        // Load client data if the client is being edited
        val clientId = intent.getStringExtra("CLIENT_ID")
        if (clientId != null) {
            // Change the title to "Edit Client"
            val title = findViewById<TextView>(R.id.Create_Client_Title)
            title.text = "Edit Client"
            loadClientData(clientId)
        }

        // Pick contact
        contactIcon.setOnClickListener {
            App.isSkipOpenAd = true
            requestContactPermission()
        }

        // Save
        doneButton.setOnClickListener {
            App.isSkipOpenAd = false
            saveClientData()
            // toast and make it in center of screen
            if (clientId != null) {
                val toast = Toast.makeText(this, "Client saved", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            } else {
                val toast = Toast.makeText(this, "Client added", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
            val preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
            val editor = preferences.edit()
            val createClientCount = preferences.getInt("createClientCount", 0)
            // set createClientCount + 1
            editor.putInt("createClientCount", createClientCount + 1)
            editor.apply()
            navigate()
        }

        // Back
        backButton.setOnClickListener {
            App.isSkipOpenAd = false
            navigate()
        }
        backTextButton.setOnClickListener {
            App.isSkipOpenAd = false
            navigate()
        }
    }

    // navigate
    private fun navigate() {
        val previousScreen = intent.getStringExtra("PREVIOUS_SCREEN")

        var scrollPosition = intent.getIntExtra("scrollPosition", 0)
        val invoiceID = intent.getStringExtra("INVOICE_ID")

        when (previousScreen) {
            "CreateInvoiceActivity" -> {
                val intent = Intent(this, CreateInvoiceActivity::class.java)
                intent.putExtra("scrollPosition", scrollPosition)
                intent.putExtra("INVOICE_ID", invoiceID)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
            "ClientListActivity" -> {
                val intent = Intent(this, ClientListActivity::class.java)
                intent.putExtra("scrollPosition", scrollPosition)
                intent.putExtra("INVOICE_ID", invoiceID)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
            "CreateInvoiceClientSelectActivity" -> {
                val intent = Intent(this, CreateInvoiceClientSelectActivity::class.java)
                intent.putExtra("scrollPosition", scrollPosition)
                intent.putExtra("INVOICE_ID", invoiceID)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
            else -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("scrollPosition", scrollPosition)
                intent.putExtra("INVOICE_ID", invoiceID)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }

        finish()
    }

    private fun requestContactPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request permission
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CONTACTS), PERMISSIONS_REQUEST_READ_CONTACTS)
        } else {
            // Permission has already been granted
            pickContact()
        }
    }


    // Permission
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                pickContact()
            } else {
                // Permission is denied
                val toast = Toast.makeText(this, "Permission denied to read contacts", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }
    }

    // Pick contact
    private fun pickContact() {
        App.isSkipOpenAd = true
        val contactPickerIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(contactPickerIntent, PICK_CONTACT_REQUEST)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    // Get contact data
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        App.isSkipOpenAd = true
        try {
            if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
                data?.data?.let { contactUri ->
                    val cursor = contentResolver.query(contactUri, null, null, null, null)
                    if (cursor != null && cursor.moveToFirst()) {
                        val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                        val contactId = cursor.getString(idIndex)

                        // Get name
                        val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        val name = cursor.getString(nameIndex)
                        clientName.setText(name)

                        // Get phone number
                        val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                        if (cursor.getInt(hasPhoneIndex) > 0) {
                            val phones = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
                                null,
                                null
                            )
                            phones?.moveToFirst()
                            val phoneIndex = phones?.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            val phone = phones?.getString(phoneIndex!!)
                            clientPhone.setText(phone)
                        }
                        cursor.close()
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        App.isSkipOpenAd = true
    }

    // load client data if the client is being edited
    private fun loadClientData(clientId: String) {
        val clientListJsonString = File(this.filesDir, "ClientList.json").readText()
        val clientListJsonArray = JSONArray(clientListJsonString)
        for (i in 0 until clientListJsonArray.length()) {
            val clientJsonObject = clientListJsonArray.getJSONObject(i)
            if (clientJsonObject.getString("id") == clientId) {
                clientName.setText(clientJsonObject.getString("name"))
                clientPhone.setText(clientJsonObject.getString("phone"))
                clientEmail.setText(clientJsonObject.getString("email"))
                clientAddress.setText(clientJsonObject.getString("address"))
                break
            }
        }
    }

    // Generate client ID
    private fun generateClientId(): String {
        return "client_${System.currentTimeMillis()}"
    }

    // Save client data
    private fun saveClientData() {
        // check edit or create
        val isEditing = intent.hasExtra("CLIENT_ID")
        if (isEditing) {

            // Edit client
            val clientId = intent.getStringExtra("CLIENT_ID") ?: return
            val clientName = clientName.text.toString()
            val clientPhone = clientPhone.text.toString()
            val clientEmail = clientEmail.text.toString()
            val clientAddress = clientAddress.text.toString()

            val clientsArray = loadClientsArray()
            val invoicesArray = loadInvoicesArray()

            for (i in 0 until clientsArray.length()) {
                val clientObject = clientsArray.getJSONObject(i)
                if (clientObject.getString("id") == clientId) {
                    clientObject.put("name", clientName)
                    clientObject.put("phone", clientPhone)
                    clientObject.put("email", clientEmail)
                    clientObject.put("address", clientAddress)

                    // getJSONArray("invoiceList") if it's not null
                    val invoiceList = clientObject.optJSONArray("invoiceList")
                    if (invoiceList != null) {
                        for (j in 0 until invoiceList.length()) {
                            val invoiceId = invoiceList.getString(j)

                            for (k in 0 until invoicesArray.length()) {
                                val invoiceObject = invoicesArray.getJSONObject(k)
                                if (invoiceObject.getString("id") == invoiceId) {
                                    val clientSelectedData = invoiceObject.getJSONArray("clientSelectedData").getJSONObject(0)
                                    clientSelectedData.put("name", clientName)
                                    clientSelectedData.put("phone", clientPhone)
                                    clientSelectedData.put("email", clientEmail)
                                    clientSelectedData.put("address", clientAddress)
                                    break
                                }
                            }
                        }
                    }
                    saveInvoicesArray(invoicesArray)

                    saveClientsArray(clientsArray)
                    break
                }
            }

        } else {
            // Create client
            // Generate a unique ID for the new client
            val clientId = generateClientId()

            // Prepare the client data as a JSON object
            val clientObject = JSONObject().apply {
                put("id", clientId)
                put("name", clientName.text.toString())
                put("phone", clientPhone.text.toString())
                put("email", clientEmail.text.toString())
                put("address", clientAddress.text.toString())
            }

            // Load the existing clients array
            val clientsArray = loadClientsArray()

            // Add the new client to the array
            clientsArray.put(clientObject)

            // Save the updated client array to the internal storage
            saveClientsArray(clientsArray)
        }
    }

    private fun loadInvoicesArray(): JSONArray {
        val filename = "InvoiceList.json"
        val file = File(filesDir, filename)
        if (!file.exists()) return JSONArray() // Return an empty array if the file doesn't exist

        return JSONArray(file.readText())
    }

    private fun saveInvoicesArray(invoicesArray: JSONArray) {
        val filename = "InvoiceList.json"
        openFileOutput(filename, MODE_PRIVATE).use { output ->
            output.write(invoicesArray.toString().toByteArray())
        }
    }

    private fun loadClientsArray(): JSONArray {
        val filename = "ClientList.json"
        val file = File(filesDir, filename)
        if (!file.exists()) return JSONArray() // Return an empty array if the file doesn't exist

        return JSONArray(file.readText())
    }

    private fun saveClientsArray(clientsArray: JSONArray) {
        val filename = "ClientList.json"
        openFileOutput(filename, MODE_PRIVATE).use { output ->
            output.write(clientsArray.toString().toByteArray())
        }
    }
}