package com.efeinfo.meli.challenge

import android.content.Context

// Global object that stores and handles the favorite products
object Favorites {

    // Holder list for the favorite IDs
    private var favorites: ArrayList<String>? = null

    // Gets the favorite IDs list. If the favorites were already loaded from
    // the shared preferences, simply return that list. If not, load the favorites
    // from the shared preferences, store it on the holder, and return it
    fun get(context: Context) = favorites ?: ArrayList<String>().apply {

        addAll(loadStrings(context, "Favorites", " "))
        favorites = this
    }

    // Add a new product ID to the favorites. After adding the product, save
    // the modified favorites list to the shared preferences
    fun add(context: Context, product: String) = get(context).run {

        add(product)
        saveStrings(context, this, "Favorites", " ")
    }

    // Removes a new product ID to the favorites. After removing the product, save
    // the modified favorites list to the shared preferences
    fun remove(context: Context, product: String) = get(context).run {

        remove(product)
        saveStrings(context, this, "Favorites", " ")
    }
}