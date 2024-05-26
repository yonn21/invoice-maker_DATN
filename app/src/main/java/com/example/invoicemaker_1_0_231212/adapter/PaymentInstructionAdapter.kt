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
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SwipeLayout
import com.example.invoicemaker_1_0_231212.CreateClientActivity
import com.example.invoicemaker_1_0_231212.CreateInvoiceActivity
import com.example.invoicemaker_1_0_231212.CreateInvoiceItemSelectActivity
import com.example.invoicemaker_1_0_231212.CreatePaymentInstructionActivity
import com.example.invoicemaker_1_0_231212.model.PaymentInstruction
import com.example.invoicemaker_1_0_231212.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

// PaymentInstructionList
class PaymentInstructionAdapter(
    private val context: Context,
    private val paymentsInstruction: ArrayList<PaymentInstruction>,
    private val scrollPosition: Int,
    private val InvoiceID: String,
    private val isSetting: Boolean,
    private val parrentScreen: String,
    private val onPaymentClick: (Int) -> Unit
) : RecyclerView.Adapter<PaymentInstructionAdapter.PaymentInstructionViewHolder>() {

    /*// on select all items
    var onSelectAllItems: (() -> Unit)? = null

    // Check if all items are selected
    fun areAllItemsSelected(): Boolean {
        return paymentsInstruction.all { it.isSelected }
    }

    // Select all items
    fun selectAllItems() {
        paymentsInstruction.forEach { it.isSelected = true }
        notifyDataSetChanged()
    }

    // Deselect all items
    fun deselectAllItems() {
        paymentsInstruction.forEach { it.isSelected = false }
        notifyDataSetChanged()
    }*/

    class PaymentInstructionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val payableTo: TextView = view.findViewById(R.id.Payment_Instruction_Name)
        val method: TextView = view.findViewById(R.id.Payment_Instruction_Method)
        val paymentDetail: TextView = view.findViewById(R.id.Payment_Instruction_Detail)
        val paymentInstructionSelectIcon: ImageView = view.findViewById(R.id.Payment_Instruction_Select_Icon)

        var isSwiping = false
        var lastSwipeTime: Long = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentInstructionViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.payment_instruction_recycler_view, parent, false)
        return PaymentInstructionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentInstructionViewHolder, position: Int) {
        if (isSetting) {
            // hide Payment_Instruction_Select_Icon
            holder.paymentInstructionSelectIcon.visibility = View.GONE
        }

        val paymentInstruction = paymentsInstruction[position]

        holder.payableTo.text = paymentInstruction.payableTo
        holder.method.text = paymentInstruction.method
        holder.paymentDetail.text = if (paymentInstruction.paymentDetail == "") {
            ""
        } else {
            paymentInstruction.paymentDetail
        }

        holder.paymentInstructionSelectIcon.setOnClickListener{
            paymentInstruction.isSelected = !paymentInstruction.isSelected
            notifyItemChanged(position)
        }

        val swipeLayout = holder.itemView.findViewById<SwipeLayout>(R.id.Payment_Instruction_Swipe_Layout)
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, null)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.itemView.findViewById(R.id.Payment_Instruction_Layout_Remove))

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

        // set the initial icon
        if (paymentInstruction.isSelected) {
            holder.paymentInstructionSelectIcon.setImageResource(R.drawable.item_list_select)
            // padding to 3dp
            holder.paymentInstructionSelectIcon.setPadding(
                3 * context.resources.displayMetrics.density.toInt(),
                3 * context.resources.displayMetrics.density.toInt(),
                3 * context.resources.displayMetrics.density.toInt(),
                3 * context.resources.displayMetrics.density.toInt())
        } else {
            holder.paymentInstructionSelectIcon.setImageResource(R.drawable.item_list_unselect)
            // padding to 5dp
            holder.paymentInstructionSelectIcon.setPadding(
                5 * context.resources.displayMetrics.density.toInt(),
                5 * context.resources.displayMetrics.density.toInt(),
                5 * context.resources.displayMetrics.density.toInt(),
                5 * context.resources.displayMetrics.density.toInt())
        }

        // Add click listener to change the icon and update the item's selection state
        holder.paymentInstructionSelectIcon.setOnClickListener {
            paymentInstruction.isSelected = !paymentInstruction.isSelected
            notifyItemChanged(position)
        }

        /*// click item in recycler view to save id to tempInvoiceData and go to CreateInvoiceActivity
        holder.itemView.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - holder.lastSwipeTime > 300 && !holder.isSwiping) {
                // Add the id of the payment instruction to tempInvoiceData.json
                val tempInvoiceData = readTempInvoiceData() ?: JSONObject()
                tempInvoiceData.put("selectedPaymentInstruction", paymentInstruction.id)

                // Save the updated tempInvoiceData
                context.openFileOutput("tempInvoiceData.json", Context.MODE_PRIVATE).use { output ->
                    output.write(tempInvoiceData.toString().toByteArray())
                }

                // Navigate to CreateInvoiceActivity
                val intent = Intent(context, CreateInvoiceActivity::class.java)
                intent.putExtra("scrollPosition", scrollPosition)
                intent.putExtra("INVOICE_ID", InvoiceID)
                context.startActivity(intent)
            }
        }*/

        // click item in recycler view to edit
        holder.itemView.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - holder.lastSwipeTime > 300 && !holder.isSwiping) {
                // open edit screen if the time from the last swipe is long enough

                val intent = Intent(context, CreatePaymentInstructionActivity::class.java)
                if (isSetting) {
                    intent.putExtra("PARENT_SCREEN", parrentScreen)
                    intent.putExtra("PREVIOUS_SCREEN", "SettingPaymentInstructionListActivity")
                    intent.putExtra("PAYMENT_INS_ID", paymentInstruction.id)
                } else {
                    intent.putExtra("PREVIOUS_SCREEN", "CreateInvoicePaymentInstructionListActivity")
                    intent.putExtra("PAYMENT_INS_ID", paymentInstruction.id)
                    intent.putExtra("scrollPosition", scrollPosition)
                    intent.putExtra("INVOICE_ID", InvoiceID)
                    intent.putExtra("PARENT_SCREEN", parrentScreen)
                }
                context.startActivity(intent)
            }
        }

        val removeButton = holder.itemView.findViewById<ImageButton>(R.id.Payment_Instruction_Remove_Button)
        removeButton.setOnClickListener {
            // alert dialog
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle("Remove payment instruction")
            alertDialog.setMessage("Are you sure you want to remove this payment instruction?")
            alertDialog.setPositiveButton("Remove", DialogInterface.OnClickListener { _, _ ->
                paymentsInstruction.removeAt(position)
                notifyItemRemoved(position)

                // Save the item list to JSON file
                val paymentInstructionListFile = File(context.filesDir, "PaymentInstruction.json")
                val paymentInstructionJsonArray = JSONArray()
                for (paymentInstruction in paymentsInstruction) {
                    val paymentInstructionJsonObject = JSONObject()
                    paymentInstructionJsonObject.put("id", paymentInstruction.id)
                    paymentInstructionJsonObject.put("payableTo", paymentInstruction.payableTo)
                    paymentInstructionJsonObject.put("method", paymentInstruction.method)
                    paymentInstructionJsonObject.put("paymentDetail", paymentInstruction.paymentDetail)
                    paymentInstructionJsonArray.put(paymentInstructionJsonObject)
                }
                paymentInstructionListFile.writeText(paymentInstructionJsonArray.toString())

                // after remove item, reload position of item
                notifyItemRangeChanged(position, paymentsInstruction.size)

                // toast and make it in center of screen
                val toast = android.widget.Toast.makeText(context, "Payment instruction removed", android.widget.Toast.LENGTH_SHORT)
                toast.setGravity(android.view.Gravity.CENTER, 0, 0)
                toast.show()
            })
            alertDialog.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
            })
            alertDialog.show()
        }

        // holder.itemView.setOnClickListener { onItemClick(position) }
    }

    override fun getItemCount() = paymentsInstruction.size

    private fun readTempInvoiceData(): JSONObject? {
        val tempFile = File(context.filesDir, "tempInvoiceData.json")
        return if (tempFile.exists()) {
            val jsonData = tempFile.readText()
            JSONObject(jsonData)
        } else null
    }
}