package com.example.invoicemaker_1_0_231212.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.invoicemaker_1_0_231212.R
import com.example.invoicemaker_1_0_231212.model.Currency

class CurrencyAdapter(
    private var currencies: List<Currency>,
    private val onCurrencySelected: (Currency) -> Unit
) : RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>() {

    class CurrencyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val codeTextView: TextView = itemView.findViewById(R.id.Currency_Code)
        private val symbolTextView: TextView = itemView.findViewById(R.id.Currency_Symbol)
        private val nameTextView: TextView = itemView.findViewById(R.id.Currency_Name)

        fun bind(currency: Currency, onCurrencySelected: (Currency) -> Unit) {
            codeTextView.text = currency.code
            symbolTextView.text = currency.symbol
            nameTextView.text = currency.name
            itemView.setOnClickListener { onCurrencySelected(currency) }
        }
    }

    private var currenciesFull: List<Currency> = currencies.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.currency_recycler_view, parent, false)
        return CurrencyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        holder.bind(currencies[position], onCurrencySelected)
    }

    override fun getItemCount(): Int = currencies.size

    fun filter(query: String) {
        val filteredList = if (query.isEmpty()) {
            currenciesFull
        } else {
            currenciesFull.filter {
                it.name.contains(query, ignoreCase = true) || it.code.contains(query, ignoreCase = true)
            }
        }
        currencies = filteredList
        notifyDataSetChanged()
    }

}
