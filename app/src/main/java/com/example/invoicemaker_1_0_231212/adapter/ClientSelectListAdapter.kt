package com.example.invoicemaker_1_0_231212.adapter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SwipeLayout
import com.example.invoicemaker_1_0_231212.CreateClientActivity
import com.example.invoicemaker_1_0_231212.CreateInvoiceClientSelectActivity
import com.example.invoicemaker_1_0_231212.R
import com.example.invoicemaker_1_0_231212.model.Client
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

// CreateInvoiceClientSelect
class ClientSelectListAdapter(
    private val context: Context,
    private val clients: ArrayList<Client>,
    private val scrollPosition: Int,
    private val InvoiceID: String,
    private val onClientClick: (Int) -> Unit
) : RecyclerView.Adapter<ClientSelectListAdapter.ClientSelectViewHolder>() {

    class ClientSelectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val invoiceClientListNameTextView: TextView = view.findViewById(R.id.Create_Invoice_Select_Client_Name)
        val invoiceClientListPhoneTextView: TextView = view.findViewById(R.id.Create_Invoice_Select_Client_Phone)
        val invoiceClientListEmailTextView: TextView = view.findViewById(R.id.Create_Invoice_Select_Client_Email)
        val invoiceClientListSelectIcon: ImageView = view.findViewById(R.id.Create_Invoice_Select_Client_Select)

        val invoiceClientListLayout: ConstraintLayout = view.findViewById(R.id.Create_Invoice_Select_Client_Recycler_Layout)

        var isSwiping = false
        var lastSwipeTime: Long = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientSelectViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.invoice_client_list_recycler_view, parent, false)
        return ClientSelectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClientSelectViewHolder, position: Int) {
        val client = clients[position]

        if (clients.size == 1) {
            holder.invoiceClientListLayout.setBackgroundResource(R.drawable.corners_rounded_full)
        } else {
            when (position) {
                0 -> holder.invoiceClientListLayout.setBackgroundResource(R.drawable.corners_rounded_top)
                clients.size - 1 -> holder.invoiceClientListLayout.setBackgroundResource(R.drawable.corners_rounded_bottom)
            }
        }

        holder.invoiceClientListNameTextView.text = client.name
        holder.invoiceClientListPhoneTextView.text = client.phone
        holder.invoiceClientListEmailTextView.text = if (client.email == "") {
            "Email has not been set"
        } else {
            client.email
        }

        val swipeLayout = holder.itemView.findViewById<SwipeLayout>(R.id.Create_Invoice_Select_Client_Swipe_Layout)
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, null)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.itemView.findViewById(R.id.Create_Invoice_Select_Client_Layout_Remove))

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

        // Set the initial icon
        if (client.isSelected) {
            holder.invoiceClientListSelectIcon.setImageResource(R.drawable.item_list_select)
            // padding to 3dp
            holder.invoiceClientListSelectIcon.setPadding(
                3 * context.resources.displayMetrics.density.toInt(),
                3 * context.resources.displayMetrics.density.toInt(),
                3 * context.resources.displayMetrics.density.toInt(),
                3 * context.resources.displayMetrics.density.toInt())
        } else {
            holder.invoiceClientListSelectIcon.setImageResource(R.drawable.item_list_unselect)
            // padding to 5dp
            holder.invoiceClientListSelectIcon.setPadding(
                5 * context.resources.displayMetrics.density.toInt(),
                5 * context.resources.displayMetrics.density.toInt(),
                5 * context.resources.displayMetrics.density.toInt(),
                5 * context.resources.displayMetrics.density.toInt())
        }

        // Change client selection logic
        holder.invoiceClientListSelectIcon.setOnClickListener {
            val previouslySelectedIndex = clients.indexOfFirst { it.isSelected }
            if (previouslySelectedIndex != -1) {
                clients[previouslySelectedIndex].isSelected = false
                notifyItemChanged(previouslySelectedIndex)
            }

            client.isSelected = true
            notifyItemChanged(position)

            (context as CreateInvoiceClientSelectActivity).updateAddButtonVisibility()
        }

        // click item in recycler view to edit
        holder.itemView.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - holder.lastSwipeTime > 300 && !holder.isSwiping) {
                // open edit screen if the time from the last swipe is long enough

                val intent = Intent(context, CreateClientActivity::class.java)
                intent.putExtra("PREVIOUS_SCREEN", "CreateInvoiceClientSelectActivity")
                intent.putExtra("CLIENT_ID", client.id)
                intent.putExtra("scrollPosition", scrollPosition)
                intent.putExtra("INVOICE_ID", InvoiceID)
                context.startActivity(intent)
            }
        }

        val removeButton = holder.itemView.findViewById<ImageButton>(R.id.Create_Invoice_Select_Client_Remove_Button)
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

                // toast and make it in center of screen
                val toast = android.widget.Toast.makeText(context, "Client removed", android.widget.Toast.LENGTH_SHORT)
                toast.setGravity(android.view.Gravity.CENTER, 0, 0)
                toast.show()

                if (client.isSelected) {
                    client.isSelected = false
                    (context as CreateInvoiceClientSelectActivity).updateAddButtonVisibility()
                }
            })
            alertDialog.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
            })
            alertDialog.show()
        }

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