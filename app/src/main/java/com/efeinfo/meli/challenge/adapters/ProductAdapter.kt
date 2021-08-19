package com.efeinfo.meli.challenge.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.efeinfo.meli.challenge.*
import com.efeinfo.meli.challenge.activities.ActivityProduct
import com.efeinfo.meli.challenge.data.Product
import com.efeinfo.meli.challenge.databinding.ResultEntryBinding
import com.squareup.picasso.Picasso

// Adapter to show a list of products. The parameters are
// activity: The activity owner of this adapter
// products: The list of products from where to retrieve the data
// count: A callback to get the adapter item count
// update: A callback that will be executed when reached the end of the recycler view and
//         there are more products to load
class ProductAdapter(val activity: Activity, val products: List<Product>, val count: () -> Int, val update: suspend () -> Unit): RecyclerView.Adapter<ProductAdapter.BaseHolder>() {

    // The base View Holder
    abstract inner class BaseHolder(view: View): RecyclerView.ViewHolder(view)

    // The Product Holder, used to show each product, which takes a Result Entry View Binding as parameter
    inner class ProductHolder(private val item: ResultEntryBinding): ProductAdapter.BaseHolder(item.root) {

        // Initialize the binding root layout parameters to match the parent width, and wrap its height
        init { item.root.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) }

        @SuppressLint("SetTextI18n")
        fun bind(product: Product) {

            // Set the Name to the product title
            item.Name.text = product.title

            // Set the Price to the product price
            item.Price.text = product.price

            // Set the Favorite resource whether this product is favorite or not
            item.Favorite.setImageResource(if (Favorites.get(activity).contains(product.id)) R.drawable.ic_favorite else R.drawable.ic_favorite_border)

            // Set the Thumbnail image to the product thumbnail URL
            val thumbSize = dpToPxI(activity, 128f)
            Picasso.get().load(product.thumbnail).resize(thumbSize, thumbSize).centerInside().into(item.Thumbnail)

            // When Favorite is clicked
            item.Favorite.setOnClickListener {

                // If this product is favorite, remove it and update the icon to reflect its state
                if (Favorites.get(activity).contains(product.id)) {

                    Favorites.remove(activity, product.id)
                    item.Favorite.setImageResource(R.drawable.ic_favorite_border)
                }

                // Else, add it and update the icon to reflect its state
                else {

                    Favorites.add(activity, product.id)
                    item.Favorite.setImageResource(R.drawable.ic_favorite)
                }
            }

            // When the item is clicked, start a new Product Activity to display this product
            item.Root.setOnClickListener { ActivityProduct.start(activity, product) }
        }
    }

    // The Progress Holder, used to show the loading progress bar at the bottom of the Recycler View
    inner class ProgressHolder(private val progress: ProgressBar): ProductAdapter.BaseHolder(progress) {

        init {

            // Initialize the binding root layout parameters to match the parent width, and wrap its height
            // Also adds a top and bottom margin of 16 DPI
            progress.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {

                topMargin = dpToPxI(activity, 16f)
                bottomMargin = dpToPxI(activity, 16f)
            }

            // Hides the progress bar
            progress.gone()
        }

        // This is where the main logic for updating the products with the next page takes place
        fun bind() {

            // If a search is already in progress, return
            // Else, set the searching field to true
            if (searching) return else searching = true

            // Show the progress bar
            progress.show()

            // Starts a new coroutine
            launchMain {

                // Calls the update callback
                update()

                // Notify that the data set has changed
                notifyDataSetChanged()

                // Hides the progress bar
                progress.gone()

                // Set the searching field to false
                searching = false
            }
        }
    }

    // Field used to indicate whether there is a search in progress
    // This is used to avoid multiple searches at the same time
    var searching = false

    // If the type of the holder is 0, create a Product Holder, else a Progress Holder
    override fun onCreateViewHolder(group: ViewGroup, type: Int): ProductAdapter.BaseHolder {

        return if (type == 0) ProductHolder(ResultEntryBinding.inflate(activity.layoutInflater))
        else return ProgressHolder(ProgressBar(activity))
    }

    // If the holder is a Product Holder, bind it with the product at the given position
    // Else, start the update logic from the Progress Holder
    override fun onBindViewHolder(holder: ProductAdapter.BaseHolder, pos: Int) {

        if (holder is ProductAdapter.ProductHolder) holder.bind(products[pos])
        else (holder as ProductAdapter.ProgressHolder).bind()
    }

    // The count is provided by the count callback
    override fun getItemCount() = count()

    // If the position is 1+ the last element of the products list and the products list is not empty,
    // Then this is the end of the Recycler View and the Progress Holder should be showed.
    // Else, show a product holder
    override fun getItemViewType(position: Int) = if (position == products.size && products.isNotEmpty()) 1 else 0
}