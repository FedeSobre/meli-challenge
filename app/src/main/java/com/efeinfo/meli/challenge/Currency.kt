package com.efeinfo.meli.challenge

import java.text.NumberFormat

// Global object that stores and handles the currencies symbols
object Currency {

    // Holder for the currencies symbols, where the key is the currency
    // code, and the value is the currency symbol
    private val currencies = HashMap<String, String>()

    // Generates a price string in the form "Symbol Price" given a currency
    // code and an integer price
    suspend fun parse(currency: String, price: Int): String {

        // Retrieves the currency symbol. If the symbol was already retrieved from the MeLi API,
        // use the stored symbol, else retrieve the symbol from the API and store it on the local
        // holder map. If the retrieval is not successful, use the currency code as the symbol
        val symbol = currencies[currency] ?: Server.currency(currency)?.also { currencies[currency] = it } ?: currency

        // Format the price to a numeric string in the current system locale
        val formatted = NumberFormat.getNumberInstance().run {

            maximumFractionDigits = 0
            format(price)
        }

        // Return the price string
        return "$symbol $formatted"
    }
}