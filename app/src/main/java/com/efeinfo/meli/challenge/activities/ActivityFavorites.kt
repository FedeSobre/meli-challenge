package com.efeinfo.meli.challenge.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.efeinfo.meli.challenge.*
import com.efeinfo.meli.challenge.adapters.ProductAdapter
import com.efeinfo.meli.challenge.data.Product
import com.efeinfo.meli.challenge.databinding.FavoritesBinding

// Activity that shows a list of the favorite products
class ActivityFavorites: AppCompatActivity() {

    // View Binding that represents this activity content view
    private lateinit var binding: FavoritesBinding

    // The products adapter
    private lateinit var adapter: ProductAdapter

    // The actual products list
    private val products = ArrayList<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Create a new Favorites layout binding and set this activity content view to its root
        binding = FavoritesBinding.inflate(layoutInflater).also { setContentView(it.root) }

        // Create a new product adapter
        adapter = ProductAdapter(this, products,

            // If no products are still loaded, return a count of 0
            // Else, if we have loaded all the favorites, return a count equal to the products size
            // Else, return a count of the products size plus one, where this last element represents
            // the update progress bar
            { if(products.isEmpty()) 0 else if (products.size >= Favorites.get(this).size) products.size else products.size + 1 },

            // If there are still favorites to load, load them from the MeLi API. If the operation fails,
            // toast a message informing that to the user
            { if (Favorites.get(this).size != products.size) Server.items(Favorites.get(this), products) ?: notify(R.string.exProductsLoad) }
        )

        // Initialize the views properties
        binding.apply {

            // Set the Products RecyclerView to have a linear layout, a divider decoration
            // and set the adapter to the previously created product adapter
            Products.layoutManager = LinearLayoutManager(activity)
            Products.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
            Products.adapter = adapter

            // On search click, start the search activity
            Search.setOnClickListener { ActivitySearch.start(activity) }
        }
    }

    override fun onStart() {

        super.onStart()

        // Start the "loading mode"
        binding.Progress.show()
        binding.Products.gone()

        // Every time this activity restarts, reload the favorites, so it keeps updated
        // if we come from an activity that changed this list. This is executed on a coroutine
        // so we can use the IO Dispatcher when performing the remote request
        launchMain {

            // Clear the loaded products
            products.clear()

            // If there are no favorites, display the Empty view
            if (Favorites.get(this).size == 0) binding.Empty.show()

            // Else, try to load the favorites from the MeLi API. If this fails, fail this activity and
            // inform the user of the problem
            else Server.items(Favorites.get(this), products) ?: run { fail(R.string.exFavoritesLoad); return@launchMain }

            // Notify the adapter that the data set has changed
            adapter.notifyDataSetChanged()

            // Finish the "loading mode"
            binding.Progress.gone()
            binding.Products.show()
        }
    }
}