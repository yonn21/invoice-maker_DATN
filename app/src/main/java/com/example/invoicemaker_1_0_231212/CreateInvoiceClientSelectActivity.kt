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
import com.example.invoicemaker_1_0_231212.model.Client
import com.example.invoicemaker_1_0_231212.adapter.ClientSelectListAdapter
import com.example.invoicemaker_1_0_231212.ads.AdmobRewardAd
import com.example.invoicemaker_1_0_231212.ads.TYPE_ADS
import com.example.invoicemaker_1_0_231212.utils.PreferencesManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class CreateInvoiceClientSelectActivity : BaseActivity () {

    private lateinit var selectClient_backButton: ImageButton
    private lateinit var selectClient_back: TextView
    private lateinit var selectClient_add: TextView
    private lateinit var selectClient_searchView: SearchView
    private lateinit var selectClient_createClient: Button
    private lateinit var selectClient_recyclerView: RecyclerView
    private lateinit var selectClient_adapter: ClientSelectListAdapter
    private var selectClient_clientList: ArrayList<Client> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_invoice_client_select)

        val preferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
        val editor = preferences.edit()
        val createClientCount = preferences.getInt("createClientCount", 0)
        val maxCreateClient = preferences.getInt("maxCreateClient", 0)

        val scrollPosition = intent.getIntExtra("scrollPosition", 0)

        // Search handle
        selectClient_searchView = findViewById(R.id.Create_Invoice_Select_Client_Search)
        setupSearchView()

        // RecyclerView handle
        selectClient_recyclerView = findViewById(R.id.Create_Invoice_Select_Client_Client_Recycler_View)
        selectClient_recyclerView.layoutManager = LinearLayoutManager(this)

        if (!isClientListEmpty(this) || hasClientListData(this)) {
            loadClientList()
        }

        val invoiceID = intent.getStringExtra("INVOICE_ID") ?: ""

        // Set adapter
        selectClient_adapter = ClientSelectListAdapter(this, selectClient_clientList, scrollPosition, invoiceID){ position ->
            // Handle Client click
        }
        selectClient_recyclerView.adapter = selectClient_adapter

        // Click button "Create New Client" go to CreateClientActivity
        selectClient_createClient = findViewById(R.id.Create_Invoice_Select_Client_Create_Client_Button)
        selectClient_createClient.setOnClickListener {
            if (PreferencesManager.checkSUB() != null) {

                val intent = Intent(this, CreateClientActivity::class.java)
                intent.putExtra("PREVIOUS_SCREEN", "CreateInvoiceClientSelectActivity")
                intent.putExtra("scrollPosition", scrollPosition)
                intent.putExtra("INVOICE_ID", invoiceID)
                startActivityForResult(intent, 15)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

            } else {
                if (createClientCount <= maxCreateClient) {
                    val intent = Intent(this, CreateClientActivity::class.java)
                    intent.putExtra("PREVIOUS_SCREEN", "CreateInvoiceClientSelectActivity")
                    intent.putExtra("scrollPosition", scrollPosition)
                    intent.putExtra("INVOICE_ID", invoiceID)
                    startActivityForResult(intent, 15)
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
                        (this as CreateInvoiceClientSelectActivity).startActivityForResult(intent, 1)
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        bottomSheetSubscriptionDialog.dismiss()
                    }

                    view.findViewById<Button>(R.id.Watch_ADS_Button).setOnClickListener {
                        App.getContext().isShowPopup = false

                        showPopupLoadAds(TYPE_ADS.RewardAd)

                        // reload activity after watch ads
                        AdmobRewardAd.Instance.setRewardAdListener { isRewarded ->
                            if (isRewarded) {
                                // set max create Client + 3
                                editor.putInt("maxCreateClient", maxCreateClient + 3)
                                editor.apply()
                                val intent = Intent(
                                    this,
                                    CreateInvoiceClientSelectActivity::class.java
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
        selectClient_add = findViewById(R.id.Create_Invoice_Select_Client_Add)
        updateAddButtonVisibility()

        selectClient_add.setOnClickListener {
            saveTempInvoiceData()
            val intent = Intent(this, CreateInvoiceActivity::class.java)
            intent.putExtra("scrollPosition", scrollPosition)
            intent.putExtra("INVOICE_ID", invoiceID)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        selectClient_backButton = findViewById(R.id.Create_Invoice_Select_Client_Button_Back)
        selectClient_backButton.setOnClickListener {
            val intent = Intent(this, CreateInvoiceActivity::class.java)
            intent.putExtra("scrollPosition", scrollPosition)
            intent.putExtra("INVOICE_ID", invoiceID)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        selectClient_back = findViewById(R.id.Create_Invoice_Select_Client_Back)
        selectClient_back.setOnClickListener {
            val intent = Intent(this, CreateInvoiceActivity::class.java)
            intent.putExtra("scrollPosition", scrollPosition)
            intent.putExtra("INVOICE_ID", invoiceID)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val scrollPosition = intent.getIntExtra("scrollPosition", 0)
        val invoiceID = intent.getStringExtra("INVOICE_ID") ?: ""
        val intent = Intent(this, CreateInvoiceActivity::class.java)
        intent.putExtra("scrollPosition", scrollPosition)
        intent.putExtra("INVOICE_ID", invoiceID)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    // Check if the client list is empty
    private fun isClientListEmpty(context: Context): Boolean {
        val clientListFile = File(context.filesDir, "ClientList.json")
        return !clientListFile.exists()
    }

    // Check if the client list has data
    private fun hasClientListData(context: Context): Boolean {
        val clientListFile = File(context.filesDir, "ClientList.json")
        return clientListFile.length() > 2
    }

    // Load client list
    private fun loadClientList() {
        val tempInvoiceData = readTempInvoiceData()
        val selectedClientId = tempInvoiceData?.optString("selectedClientId")

        val clientListFile = File(this.filesDir, "ClientList.json")
        val clientListJsonString = clientListFile.readText()
        val clientListJsonArray = JSONArray(clientListJsonString)
        selectClient_clientList.clear()

        for (i in 0 until clientListJsonArray.length()) {
            val clientJsonObject = clientListJsonArray.getJSONObject(i)
            val clientId = clientJsonObject.optString("id")

            val client = Client(
                id = clientId,
                name = clientJsonObject.optString("name"),
                phone = clientJsonObject.optString("phone"),
                email = clientJsonObject.optString("email"),
                address = clientJsonObject.optString("address"),
                isSelected = clientId == selectedClientId // Client is selected if ID matches
            )
            selectClient_clientList.add(client)
        }
        selectClient_clientList.sortBy { it.id }
    }


    // setup search
    private fun setupSearchView() {
        selectClient_searchView.setOnClickListener {
            selectClient_searchView.onActionViewExpanded()
        }

        selectClient_searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                selectClient_adapter.filter(newText ?: "")
                return false
            }
        })
    }

    // Handle add button visibility
    fun updateAddButtonVisibility() {
        val isAnyClientSelected = selectClient_clientList.any { it.isSelected }
        if (isAnyClientSelected) {
            selectClient_add.isEnabled = true
            selectClient_add.setTextColor(resources.getColor(R.color.colorPrimary))
        } else {
            selectClient_add.isEnabled = false
            selectClient_add.setTextColor(resources.getColor(R.color.textHint))
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

    // Save the selected client to the temp invoice data file
    private fun saveTempInvoiceData() {
        val tempInvoiceData = readTempInvoiceData() ?: JSONObject()
        val selectedClient = selectClient_clientList.firstOrNull { it.isSelected }

        selectedClient?.let {
            tempInvoiceData.put("selectedClientId", it.id)
        }

        // save client data to tempInvoiceData with array name "clientSelectedData"
        val clientSelectedData = JSONArray()
        for (i in 0 until selectClient_clientList.size) {
            val client = selectClient_clientList[i]
            if (client.isSelected) {
                val clientData = JSONObject()
                clientData.put("id", client.id)
                clientData.put("name", client.name)
                clientData.put("phone", client.phone)
                clientData.put("email", client.email)
                clientData.put("address", client.address)
                clientSelectedData.put(clientData)
            }
        }
        tempInvoiceData.put("clientSelectedData", clientSelectedData)

        val tempFile = File(filesDir, "tempInvoiceData.json")
        tempFile.writeText(tempInvoiceData.toString())
    }

}