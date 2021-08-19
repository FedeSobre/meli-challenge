package com.efeinfo.meli.challenge.activities

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.efeinfo.meli.challenge.*
import com.efeinfo.meli.challenge.adapters.ProductAdapter
import com.efeinfo.meli.challenge.data.Product
import com.efeinfo.meli.challenge.databinding.ResultsBinding

// Activity that displays the results of a search
class ActivityResults: AppCompatActivity() {

    companion object {

        // Entry point for this activity with its required parameters
        fun start(activity: Activity, mode: String, query: String) = activity.start<ActivityResults> {

            // Store the search mode
            putExtra("Mode", mode)

            // Store the search query
            putExtra("Query", query)
        }
    }

    // View Binding that represents this activity content view
    private lateinit var binding: ResultsBinding

    // The recent search adapter
    private lateinit var adapter: ProductAdapter

    // The search mode, can be "Query" or "Category"
    private lateinit var mode: String

    // The search query, if the mode is "Query", stores the actual query, and if the
    // mode is "Category", stores the category ID
    private lateinit var query: String

    // The actual products list
    private val products = ArrayList<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Create a new Results layout binding and set this activity content view to its root
        binding = ResultsBinding.inflate(layoutInflater).also { setContentView(it.root) }

        // Retrieves the mode from the activity intent. If no mode was provided, fail and inform the user about this error
        mode = intent.getStringExtra("Mode") ?: run { fail(R.string.exSeach, "Intent mode was null"); return }

        // Retrieves the query from the activity intent. If no query was provided, fail and inform the user about this error
        query = intent.getStringExtra("Query") ?: run { fail(R.string.exSeach, "Intent query was null"); return }

        // Create a new product adapter
        adapter = ProductAdapter(this, products,

            // If no products are still loaded, return a count of 0
            // Else, return a count of the products size plus one, where this last element represents
            // the update progress bar
            { if(products.isEmpty()) 0 else products.size + 1 },

            // Load the products from MeLi API, using a specific endpoint for each mode. If the operation fails,
            // toast a message informing that to the user
            { (if (mode == "Category") Server.searchCategory(query, products) else Server.searchQuery(query, products)) ?: notify(R.string.exProductsLoad) }
        )

        // Initialize the views properties
        binding.apply {

            // Set the Products RecyclerView to have a linear layout, a divider decoration
            // and set the adapter to the previously created product adapter
            Products.layoutManager = LinearLayoutManager(activity)
            Products.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
            Products.adapter = adapter

            // On search click, start the search activity. If the mode is "Query", provide the query as the initial parameter
            SearchBar.SearchArea.setOnClickListener { ActivitySearch.start(activity, if (mode == "Query") query else null) }

            // When the Favorites ImageView of the SearchBar is clicked, start the favorites activity
            SearchBar.Favorites.setOnClickListener { start<ActivityFavorites> {} }

            // If the mode is "Query", set the SearchBar Query text to the actual query
            if (mode == "Query") SearchBar.Query.text = query
            else SearchBar.Query.setTextColor(ResourcesCompat.getColor(resources, R.color.lightText, theme))
        }

        // This is executed on a coroutine so we can use the IO Dispatcher when performing the remote request
        launchMain {

            // Request the products from the MeLi API, using a specific endpoint for each mode. If the provided
            // mode is invalid, fail and inform the user about this error
            val result = when (mode) {

                "Category" -> Server.searchCategory(query, products)
                "Query" -> Server.searchQuery(query, products)
                else -> { fail(R.string.exSeach, "Invalid mode $mode"); return@launchMain }
            }

            // If the products request failed, fail and inform the user about this error
            result ?: run { fail(R.string.exSeach); return@launchMain }

            // Notify the adapter that the data set has changed
            adapter.notifyDataSetChanged()

            // Hide the Progress bar
            binding.Progress.gone()
        }
    }

    override fun onStart() {

        super.onStart()

        // Notify the adapter that the data set has changed. This is used to update the favorite
        // state of the products if we returned from another activity that changed the currently
        // loaded products favorite state
        adapter.notifyDataSetChanged()
    }
}