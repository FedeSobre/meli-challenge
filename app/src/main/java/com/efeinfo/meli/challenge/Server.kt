package com.efeinfo.meli.challenge

import android.annotation.SuppressLint
import com.efeinfo.meli.challenge.data.Category
import com.efeinfo.meli.challenge.data.Product
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.min

// Global object that provides the bridge between the application and the Mercado Libre API
object Server {

    // Mercado Libre Domain URL
    private const val domain = "https://api.mercadolibre.com"

    // Mercado Libre Site, currently configured as Mercado Libre Argentina
    private const val site = "MLA"

    // Object that contains the endpoints for the Mercado Libre API
    private object EndPoint {

        // Endpoint for retrieving all the product's categories
        fun categories() = "/sites/$site/categories"

        // Endpoint for retrieving product of a particular category
        // The offset parameter is used for pagination of the results
        fun searchCategory(category: String, offset: String) = "/sites/$site/search?category=$category&offset=$offset&limit=50"

        // Endpoint for retrieving product of a query
        // The offset parameter is used for pagination of the results
        fun searchQuery(query: String, offset: String) = "/sites/$site/search?q=$query&offset=$offset&limit=50"

        // Endpoint for retrieving a product based on its ID
        fun item(item: String) = "/items/$item"

        // Endpoint for retrieving products based on their ID
        fun items(items: List<String>) = "/items?ids=${items.joinToString(",")}"

        // Endpoint for retrieving the description of a particular product
        fun description(item: String) = "/items/$item/description"

        // Endpoint for retrieving the symbol of a particular currency
        fun currency(currency: String) = "/currencies/$currency"
    }

    // OkHttp instance, used for HTTP requests
    private val client = OkHttpClient()

    // Helper function that executes an HTTP request and returns the body of the response as a string
    private fun call(endpoint: String) = client.newCall(Request.Builder().url("$domain$endpoint").build()).execute().body?.string()

    // Helper function that retrieves a list of elements of type T from the MercadoLibre API and parses the result
    // The parameters used are:
    // name: The name of the endpoint for logging purposes
    // endpoint: The actual endpoint provided by the EndPoint Object
    // target: An optional ArrayList that, if provided, will be filled with the parsed data
    // parse: A callback used to convert the string response into a JSON array with the raw data list
    // build: A callback that takes a JSON object and returns a new object of type T
    // duplicate: A callback to check whether an element was already present in the list of elements
    @SuppressLint("DefaultLocale")
    private suspend inline fun<reified T> list(name: String, endpoint: String, target: ArrayList<T>?, crossinline parse: (String) -> JSONArray, crossinline build: suspend (JSONObject) -> T, crossinline duplicate: (T, List<T>) -> Boolean) = withIO {

        // Try to obtain a response from the endpoint
        val response = tryNull("Could not obtain $name from the remote server") { call(endpoint) } ?: return@withIO null

        // Try to create a JSON array from the HTTP response
        val json = tryNull("${name.capitalize()} response was not of the expected type JSONArray") { parse(response) } ?: return@withIO null

        // If target is provided, increase its capacity by the length of elements in the JSON array
        // Else, create a new array list with a capacity equal to the length of elements in the JSON array
        val array = target?.apply { ensureCapacity(size + json.length()) } ?: ArrayList(json.length())

        // Loop trough all the elements of the JSON array
        for (i in 0 until json.length()) {

            // Try to create a new object from the JSON object at position i of the JSON array
            coTryNull("Could not create ${T::class.java.name} object from JSON ${json.get(i)}") { build(json.getJSONObject(i)) }

            // If the object was successfully created (not null) and is not duplicated, add it to the array
            ?.also { if (!duplicate(it, array)) array.add(it) }
        }

        // Returns the array
        array
    }

    // Returns an ArrayList of Category objects representing all the categories in Mercado Libre
    suspend fun categories(target: ArrayList<Category>? = null) = list("categories", EndPoint.categories(), target,

        // The parse callback: The response is already the categories JSON array so return it
        { JSONArray(it) },

        // The build callback: Each element of the JSON array is the actual JSON object with the category data
        { Category(it) },

        // The duplicate callback: No duplicates for the categories endpoint
        { _, _ -> false }
    )

    // Returns an ArrayList of Product objects that belongs to a particular category
    // The endpoint takes a category and an offset as a parameter
    // If target is provided, the offset is the actual size of the target list
    suspend fun searchCategory(category: String, target: ArrayList<Product>? = null) = list("category based products", EndPoint.searchCategory(category, (target?.size ?: 0).toString()), target,

        // The parse callback: The products JSON array is under a key called "results"
        { JSONObject(it).getJSONArray("results") },

        // The build callback: Each element of the JSON array is the actual JSON object with the product data
        { Product.build(it) },

        // The duplicate callback: Check if the list contains an element with the same product ID
        { product, array -> array.find { it.id == product.id } != null }
    )

    // Returns an ArrayList of Product objects that matches the given query
    // The endpoint takes a query and an offset as a parameter
    // If target is provided, the offset is the actual size of the target list
    suspend fun searchQuery(query: String, target: ArrayList<Product>? = null) = list("query based products", EndPoint.searchQuery(query, (target?.size ?: 0).toString()), target,

        // The parse callback: The products JSON array is under a key called "results"
        { JSONObject(it).getJSONArray("results") },

        // The build callback: Each element of the JSON array is the actual JSON object with the product data
        { Product.build(it) },

        // The duplicate callback: Check if the list contains an element with the same product ID
        { product, array -> array.find { it.id == product.id } != null }
    )

    // Returns an ArrayList of Products based on the given IDs
    suspend fun items(items: List<String>, target: ArrayList<Product>? = null): ArrayList<Product>? {

        // Generates a new list based on the offset
        val list = items.subList(min(target?.size ?: 0, items.size), min((target?.size ?: 0) + 20, items.size))

        return list("id based products", EndPoint.items(list), target,

            // The parse callback: The response is already the products JSON array so return it
            { JSONArray(it) },

            // The build callback: The product JSON object is located under a key named "body"
            { Product.build(it.getJSONObject("body")) },

            // The duplicate callback: Check if the list contains an element with the same product ID
            { product, array -> array.find { it.id == product.id } != null }
        )
    }

    // Retrieves the pictures of a particular product
    suspend fun pictures(product: String) = list("product", EndPoint.item(product), null,

        // The parse callback: The pictures JSON array is under a key called "pictures"
        { JSONObject(it).getJSONArray("pictures") },

        // The build callback: The picture URL is located under a key named "url"
        { it.getString("url") },

        // The duplicate callback: Check if the list contains the same URL
        { url, array -> array.find { it == url } != null }
    )

    // Retrieves the description of a particular product
    suspend fun description(product: String) = withIO {

        // Try to obtain a response from the item endpoint
        val response = tryNull("Could not obtain product description from the remote server") { call(EndPoint.description(product)) } ?: return@withIO null

        // Try to create a JSON array from the HTTP response
        val json = tryNull("Product description response was not of the expected type JSON") { JSONObject(response) } ?: return@withIO null

        // Try to get the description. If the "text" key is not empty, return its content
        // Else return the "plain_text" key content
        tryNull("Product description is invalid $json") { json.getString("text").let { if (it != "") it else json.getString("plain_text") } }
    }

    // Retrieves the symbol of a particular currency
    suspend fun currency(currency: String) = withIO {

        // Try to obtain a response from the currency endpoint
        val response = tryNull("Could not obtain currency from the remote server") { call(EndPoint.currency(currency)) } ?: return@withIO null

        // Try to create a JSON array from the HTTP response
        val json = tryNull("Currency response was not of the expected type JSON") { JSONObject(response) } ?: return@withIO null

        // Try to get the symbol from the "symbol" key of the JSON object
        tryNull("Currency is invalid $json") { json.getString("symbol") }
    }
}