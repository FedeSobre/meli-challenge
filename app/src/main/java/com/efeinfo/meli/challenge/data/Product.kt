package com.efeinfo.meli.challenge.data

import com.efeinfo.meli.challenge.Currency
import org.json.JSONObject

// Data object to store a Product
data class Product(

    var id: String,
    var title: String,

    var price: String,

    var available: Int,
    var sold: Int,

    var condition: String,

    var permalink: String,
    var thumbnail: String,

    ) {

    companion object {

        // Builder for a Product instance. This is used because products might need to
        // retrieve the currency symbol from the MercadoLibre API, thus it must be executed
        // in a suspend context
        suspend fun build(json: JSONObject) = Product(

            json.getString("id"),
            json.getString("title"),

            // fullPrice is used when generating a JSON Object from the "toJSON" method
            if (json.has("fullPrice")) json.getString("fullPrice")

            // If from a MercadoLibre API response, parse the product currency
            else Currency.parse(json.getString("currency_id"), json.getInt("price")),

            json.getInt("available_quantity"),
            json.getInt("sold_quantity"),

            json.getString("condition"),

            json.getString("permalink"),
            json.getString("thumbnail")
        )
    }

    // Creates a JSON object with this Product instance data
    // Used to serialize the data and pass it to the Product Activity
    fun toJSON() = JSONObject().apply {

        put("id", id)
        put("title", title)

        put("fullPrice", price)

        put("available_quantity", available)
        put("sold_quantity", sold)

        put("condition", condition)

        put("permalink", permalink)
        put("thumbnail", thumbnail)
    }
}