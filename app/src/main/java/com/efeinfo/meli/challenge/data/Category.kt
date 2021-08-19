package com.efeinfo.meli.challenge.data

import org.json.JSONObject

// Data object to store a Category
data class Category(val id: String, val name: String) {

    constructor(json: JSONObject): this(

        json.getString("id"),
        json.getString("name")
    )
}