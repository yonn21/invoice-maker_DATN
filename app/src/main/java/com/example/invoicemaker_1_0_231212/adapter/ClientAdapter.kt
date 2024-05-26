package com.example.invoicemaker_1_0_231212.adapter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import com.example.invoicemaker_1_0_231212.ClientListActivity
import com.example.invoicemaker_1_0_231212.CreateClientActivity
import com.example.invoicemaker_1_0_231212.R
import com.example.invoicemaker_1_0_231212.model.Client
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

// ClientList
class ClientAdapter(
    private val context: Context,
    private val clients: ArrayList<Client>,
    private val onClientClick: (Int) -> Unit
) : RecyclerView.Adapter<ClientAdapter.ClientViewHolder>() {

    class ClientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.Client_Name)
        val phoneTextView: TextView = view.findViewById(R.id.Client_Phone)
        val emailTextView: TextView = view.findViewById(R.id.Client_Email)

        val clientListLayout: ConstraintLayout = view.findViewById(R.id.Client_Recycler_Layout)

        var isSwiping = false
        var lastSwipeTime: Long = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.client_recycler_view, parent, false)
        return ClientViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        val client = clients[position]

        if (clients.size == 1) {
            holder.clientListLayout.setBackgroundResource(R.drawable.corners_rounded_full)
        } else {
            when (position) {
                0 -> holder.clientListLayout.setBackgroundResource(R.drawable.corners_rounded_top)
                clients.size - 1 -> holder.clientListLayout.setBackgroundResource(R.drawable.corners_rounded_bottom)
            }
        }

        holder.nameTextView.text = client.name
        holder.phoneTextView.text = client.phone
        holder.emailTextView.text = if (client.email == "") {
            "Email has not been set"
        } else {
            client.email
        }

        val swipeLayout = holder.itemView.findViewById<SwipeLayout>(R.id.Client_Swipe_Layout)
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, null)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.itemView.findViewById(R.id.Client_Layout_Remove))

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
                val intent = Intent(context, CreateClientActivity::class.java)
                intent.putExtra("PREVIOUS_SCREEN", "ClientListActivity")
                intent.putExtra("CLIENT_ID", client.id)
                context.startActivity(intent)
            }
        }


        val removeButton = holder.itemView.findViewById<ImageButton>(R.id.Client_Remove_Button)
        removeButton.setOnClickListener {
            // alert dialog
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle("Remove Client")
            alertDialog.setMessage("Are you sure you want to remove this client?")
            alertDialog.setPositiveButton("Remove", DialogInterface.OnClickListener { _, _ ->
                clients.removeAt(position)
                notifyItemRemoved(position)

                // Save the Client list to JSON file
                val clientListFile = File(context.filesDir, "ClientList.json")
                val clientListJsonArray = JSONArray()
                for (client in clients) {
                    val clientJsonObject = JSONObject()
                    clientJsonObject.put("id", client.id)
                    clientJsonObject.put("name", client.name)
                    clientJsonObject.put("phone", client.phone)
                    clientJsonObject.put("email", client.email)
                    clientJsonObject.put("address", client.address)
                    clientListJsonArray.put(clientJsonObject)
                }
                clientListFile.writeText(clientListJsonArray.toString())

                // after remove client, reload position of client
                notifyItemRangeChanged(position, clients.size)
                notifyDataSetChanged()

                // if client list is empty, display button "New Client"
                if (clients.isEmpty()) {
                    val buttonCreateNewClient = (context as ClientListActivity).findViewById<Button>(
                        R.id.Client_List_Button_New_Client)
                    val imageEmptyClient = (context as ClientListActivity).findViewById<ImageView>(R.id.Client_List_Image_Empty)
                    val textEmptyClient = (context as ClientListActivity).findViewById<TextView>(R.id.Client_List_Empty_Text)
                    val cardCreateNewClient = (context as ClientListActivity).findViewById<CardView>(R.id.Client_List_Button_New_Client_Card)
                    val buttonAddClient = context.findViewById<ImageButton>(R.id.Client_List_Button_Add_Client)

                    // Hide search view
                    val searchView = context.findViewById<SearchView>(R.id.Client_List_Search)
                    val searchViewLayout = context.findViewById<CardView>(R.id.Client_List_Search_Layout)
                    searchView.visibility = SearchView.INVISIBLE
                    searchViewLayout.visibility = CardView.GONE
                    searchView.isClickable = false

                    // Display button "New Client"
                    buttonCreateNewClient.visibility = Button.VISIBLE
                    imageEmptyClient.visibility = ImageView.VISIBLE
                    textEmptyClient.visibility = TextView.VISIBLE
                    cardCreateNewClient.visibility = CardView.VISIBLE
                    buttonCreateNewClient.isClickable = true
                    // Hide button "Add Client"
                    buttonAddClient.visibility = Button.INVISIBLE
                    buttonAddClient.isClickable = false

                    // click button "New Client" go to CreateClientActivity
                    buttonCreateNewClient.setOnClickListener {
                        val intent = Intent(context, CreateClientActivity::class.java)
                        intent.putExtra("PREVIOUS_SCREEN", "ClientListActivity")
                        context.startActivity(intent)
                    }
                }

                // toast and make it in center of screen
                val toast = android.widget.Toast.makeText(context, "Client removed", android.widget.Toast.LENGTH_SHORT)
                toast.setGravity(android.view.Gravity.CENTER, 0, 0)
                toast.show()
            })
            alertDialog.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
            })
            alertDialog.show()
        }

        // holder.clientView.setOnClickListener { onClientClick(position) }
    }

    override fun getItemCount() = clients.size

    private var clientListFull: ArrayList<Client> = ArrayList(clients)

    fun filter(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            clientListFull
        } else {
            clientListFull.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
        clients.clear()
        clients.addAll(filteredList)
        notifyDataSetChanged()
    }
}