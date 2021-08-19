package com.efeinfo.meli.challenge.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.efeinfo.meli.challenge.*
import com.efeinfo.meli.challenge.data.Product
import com.efeinfo.meli.challenge.databinding.ProductBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.delay
import org.json.JSONObject

// Activity that shows a product details
class ActivityProduct: AppCompatActivity() {

    companion object {

        // Entry point for this activity with its required parameters
        // This will take a product as a parameter, and will append it to the intent as a JSON string
        fun start(activity: Activity, product: Product) { activity.start<ActivityProduct> { putExtra("Product", product.toJSON().toString()) } }
    }

    // The pictures RecyclerView adapter
    inner class PicturesAdapter: RecyclerView.Adapter<PicturesAdapter.Holder>() {

        // The RecyclerView holder, which an ImageView as its constructor parameter
        inner class Holder(private val view: ImageView): RecyclerView.ViewHolder(view) {

            // Initialize the view layout parameters to match the parent dimensions
            init { view.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT) }

            // Bind this holder, taking a picture URL object as its parameter
            fun bind(picture: String) {

                // Post an event to the main (UI) thread looper. This is used in the case this view could have a width
                // and height of zero, because it was still not measured on the layout pass
                view.post {

                    // If the width or height of the view are still zero (or negative), use fixed dimensions
                    val width = if (view.width <= 0) 1024 else view.width
                    val height = if (view.height <= 0) dpToPxI(activity, 384f) else view.height

                    // Use the Picasso library to load the remote image into the ImageView
                    // This will resize the image to the previously defined dimensions, and will
                    // center the image in the view
                    Picasso.get().load(picture).resize(width, height).centerInside().into(view)
                }
            }
        }

        // Creates a new holder providing it an ImageView
        override fun onCreateViewHolder(group: ViewGroup, type: Int) = Holder(ImageView(activity))

        // Bind the holder to the picture at the given position
        override fun onBindViewHolder(holder: Holder, pos: Int) { holder.bind(pictures[pos]) }

        // The count of the adapter is the size of the pictures list
        override fun getItemCount() = pictures.size
    }

    // View Binding that represents this activity content view
    private lateinit var binding: ProductBinding

    // The product that is being displayed
    private lateinit var product: Product

    // The list of pictures of this product
    private var pictures = emptyList<String>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Create a new Product layout binding and set this activity content view to its root
        binding = ProductBinding.inflate(layoutInflater).also { setContentView(it.root) }

        // On search click, start the search activity
        binding.Search.setOnClickListener { ActivitySearch.start(this) }

        // As retrieving the product must be executed inside a coroutine scope because it might need to
        // retrieve the currency symbol from the MercadoLibre API, configure everything within the coroutine body
        launchMain {

            // Retrieve the product JSON from the activity intent. If the operation fails, fail and inform the user about the error
            val json = intent.getStringExtra("Product") ?: run { fail(R.string.exProductLoad, "Intent product was null"); return@launchMain }

            // Create a Product object from the JSON object. If the operation fails, fail and inform the user about the error
            product = coTryNull("Product was not of expected JSON type") { Product.build(JSONObject(json)) } ?: run { fail(R.string.exProductLoad); return@launchMain }

            // Try to load the picture list. If the operation fails, inform the user about the error
            pictures = Server.pictures(product.id) ?: emptyList<String>().also { notify(R.string.exPicturesLoad) }

            // Try to load the product description. If the operation fails, inform the user about the error
            binding.Description.text = Server.description(product.id) ?: "".also { notify(R.string.exDecriptionLoad) }

            // When Favorite is clicked
            binding.Favorite.setOnClickListener {

                // If this product is favorite, remove it and update the icon to reflect its state
                if (Favorites.get(this).contains(product.id)) {

                    Favorites.remove(this, product.id)
                    binding.Favorite.setImageResource(R.drawable.ic_favorite_border)
                }

                // Else, add it and update the icon to reflect its state
                else {

                    Favorites.add(this, product.id)
                    binding.Favorite.setImageResource(R.drawable.ic_favorite)
                }
            }

            // If the product condition is "new", set the State text as New, else Used
            binding.State.text = "${resources.getText(if (product.condition == "new") R.string.newItem else R.string.usedItem)}" +

                // Following the above line text, if the product was never sold append nothing
                // Else, append the amount sold plus "Sold". If the amount is 1, use a singular verb, else plural
                if (product.sold <= 0) "" else " | ${product.sold} " + resources.getText(if (product.sold == 1) R.string.soldSingular else R.string.soldPlural)

            // Set the Title text to the product title
            binding.Title.text = product.title

            // Set the Pictures adapter to a new PicturesAdapter object
            binding.Pictures.adapter = PicturesAdapter()

            // When the Pictures Pager changes the current image, update the top left index marker to "Current / Total"
            binding.Pictures.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) { binding.PicturesIndex.text = "${position + 1} / ${pictures.size}" }
            })

            // Update the top left index marker. If no pictures are present, "0 / 0", else "1 / Total"
            binding.PicturesIndex.text = "${if (pictures.isEmpty()) "0" else "1"} / ${pictures.size}"

            // Start a new Send activity to share the product URL using the Android native chooser
            binding.ShareIcon.setOnClickListener {

                val intent = Intent(Intent.ACTION_SEND).apply {

                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.productURL))
                    putExtra(Intent.EXTRA_TEXT, product.permalink)
                }

                startActivity(Intent.createChooser(intent, resources.getString(R.string.productShare)))
            }

            // Set the Price text to the product price
            binding.Price.text = product.price

            // Set the Stock text to "Available stock"
            binding.Stock.text = "${resources.getText(R.string.stock)}: " +

                // Following the above line text, if there is no stock available, inform so
                if (product.available == 0) resources.getText(R.string.noStock)

                // Else, append the available stock plus "Unit". If the amount is 1, use a singular substantive, else plural
                else "${product.available} " + resources.getText(if (product.available == 1) R.string.unit else R.string.units)

            // When Extern is clicked, open this product in the MercadoLibre official page (or app if installed)
            binding.Extern.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(product.permalink))) }

            // Finish the "loading mode" and show the contents of the product
            binding.Progress.gone()
            binding.Scroller.show()
            binding.ExternCard.show()
        }
    }

    override fun onStart() {

        super.onStart()

        // Given the fact that the product object is created on a coroutine block, this
        // operation is also executed in a coroutine block
        launchMain {

            // This should not be necessary, but in the case this block is executed before
            // the onCreate coroutine block, this will prevent an exception
            while (!::product.isInitialized) delay(1)

            // Updates the Favorite drawable based on the actual favorite state of this product
            // This is executed every time the activity starts, so if the favorite state of this product
            // changed on another activity, this can be reflected
            if (Favorites.get(this).contains(product.id)) binding.Favorite.setImageResource(R.drawable.ic_favorite)
            else binding.Favorite.setImageResource(R.drawable.ic_favorite_border)
        }
    }
}