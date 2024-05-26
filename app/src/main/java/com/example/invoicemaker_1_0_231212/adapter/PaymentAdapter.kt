package com.example.invoicemaker_1_0_231212.adapter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SwipeLayout
import com.example.invoicemaker_1_0_231212.CreateInvoiceCreatePaymentActivity
import com.example.invoicemaker_1_0_231212.CreateInvoicePaymentHistoryActivity
import com.example.invoicemaker_1_0_231212.MainActivity
import com.example.invoicemaker_1_0_231212.PaymentInstructionListActivity
import com.example.invoicemaker_1_0_231212.model.Payment
import com.example.invoicemaker_1_0_231212.R
import com.example.invoicemaker_1_0_231212.model.PaymentInstruction
import org.json.JSONObject
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Locale

// CreateInvoicePaymentHistory
class PaymentAdapter(
    private val context: Context,
    private val payments: ArrayList<Payment>,
    private val scrollPosition: Int,
    private val InvoiceID: String,
    private val settingArray: Array<out Any>,
    private val onPaymentClick: (Int) -> Unit
) : RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    class PaymentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateView: TextView = view.findViewById(R.id.Create_Invoice_Payment_History_Date)
        val amount: TextView = view.findViewById(R.id.Create_Invoice_Payment_History_Amount)
        val method: TextView = view.findViewById(R.id.Create_Invoice_Payment_History_Method)
        val note: TextView = view.findViewById(R.id.Create_Invoice_Payment_History_Note)

        var isSwiping = false
        var lastSwipeTime: Long = 0
    }

    private var currencyPosition = settingArray[0].toString()
    private var currencySymbol = settingArray[1].toString()
    private var decimalPlaces = settingArray[2].toString().toInt()
    private var numberFormat = settingArray[3].toString().toInt()
    private var dateFormatSetting = settingArray[4].toString()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.invoice_paymet_list_recycler_view, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = payments[position]

        holder.dateView.text = setStringDateBasedOnSettings(payment.date, false)
        holder.method.text = payment.method
        holder.note.text = if (payment.note == "") {
            "No notes"
        } else {
            payment.note
        }
        holder.amount.text = setDouleValueBasedOnSettingsToString(payment.amount, true)

        val swipeLayout = holder.itemView.findViewById<SwipeLayout>(R.id.Create_Invoice_Payment_History_Swipe_Layout)
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, null)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.itemView.findViewById(R.id.Create_Invoice_Payment_History_Layout_Remove))

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
                val intent = Intent(context, CreateInvoiceCreatePaymentActivity::class.java)
                intent.putExtra("PREVIOUS_SCREEN", "CreateInvoicePaymentHistoryActivity")
                intent.putExtra("PAYMENT_ID", payment.id)
                intent.putExtra("scrollPosition", scrollPosition)
                intent.putExtra("INVOICE_ID", InvoiceID)
                context.startActivity(intent)
            }
        }

        val removeButton = holder.itemView.findViewById<ImageButton>(R.id.Create_Invoice_Payment_History_Remove_Button)
        removeButton.setOnClickListener {
            // alert dialog
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle("Remove payment")
            alertDialog.setMessage("Are you sure you want to remove this payment?")
            alertDialog.setPositiveButton("Remove", DialogInterface.OnClickListener { _, _ ->
                payments.removeAt(position)
                notifyItemRemoved(position)

                // save the paymentList[] to tempInvoiceData.json (paymentList[] is the list of payments)
                val tempInvoiceData = File(context.filesDir, "tempInvoiceData.json")
                val tempInvoiceDataJSON = JSONObject(tempInvoiceData.readText())
                val tempInvoiceDataJSONPayments = tempInvoiceDataJSON.getJSONArray("paymentList")
                tempInvoiceDataJSONPayments.remove(position)
                tempInvoiceDataJSON.put("paymentList", tempInvoiceDataJSONPayments)
                tempInvoiceData.writeText(tempInvoiceDataJSON.toString())

                // after remove item, reload position of item
                notifyItemRangeChanged(position, payments.size)

                // update the invoice total amount text view
                val invoiceTotalAmount = (context as CreateInvoicePaymentHistoryActivity).findViewById<TextView>(R.id.Create_Invoice_Payment_History_Total_Amount)
                invoiceTotalAmount.text = setDouleValueBasedOnSettingsToString(payments.sumByDouble { it.amount }, true)

                // if item list is empty
                if (payments.isEmpty()) {
                    // go to create payment activity
                    val intent = Intent(context, CreateInvoiceCreatePaymentActivity::class.java)
                    intent.putExtra("PREVIOUS_SCREEN", "CreateInvoiceActivity")
                    intent.putExtra("INVOICE_ID", InvoiceID)
                    context.startActivity(intent)
                }

                // toast and make it in center of screen
                val toast = android.widget.Toast.makeText(context, "Payment removed", android.widget.Toast.LENGTH_SHORT)
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

    override fun getItemCount() = payments.size

    private fun setStringDateBasedOnSettings(date: String, isConvertTo_ddMMyyyy: Boolean): String {
        if (isConvertTo_ddMMyyyy) {
            // convert to dd/MM/yyyy
            val dateFormat = SimpleDateFormat(dateFormatSetting, Locale.getDefault())
            val dateObj = dateFormat.parse(date)
            return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateObj)
        } else {
            // convert base on date format setting
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateObj = inputFormat.parse(date) ?: return ""
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