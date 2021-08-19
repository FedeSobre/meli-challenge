package com.efeinfo.meli.challenge.activities

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.efeinfo.meli.challenge.*
import com.efeinfo.meli.challenge.data.Category
import com.efeinfo.meli.challenge.databinding.CategoryBinding
import com.efeinfo.meli.challenge.databinding.MainBinding

// Main entry point of the application, allows to search products based on a category or a query
class ActivityMain: AppCompatActivity() {

    // The category RecyclerView adapter
    inner class CategoryAdapter: RecyclerView.Adapter<CategoryAdapter.Holder>() {

        // The RecyclerView holder, which takes a Category View Binding as its constructor parameter
        inner class Holder(private val item: CategoryBinding): RecyclerView.ViewHolder(item.root) {

            // Initialize the binding root layout parameters to match the parent width, and wrap its height
            init { item.root.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) }

            // Bind this holder, taking a Category object as its parameter
            fun bind(category: Category) {

                // Set the view name to the category name
                item.Name.text = category.name

                // When this item is clicked, start a result activity that shows products of this category
                item.Category.setOnClickListener { ActivityResults.start(activity, "Category", category.id) }
            }
        }

        // Creates a new holder inflating a Category View Binding
        override fun onCreateViewHolder(group: ViewGroup, type: Int): Holder { return Holder(CategoryBinding.inflate(layoutInflater)) }

        // Bind the holder to the category at the given position
        override fun onBindViewHolder(holder: Holder, pos: Int) { holder.bind(categories[pos]) }

        // The count of the adapter is the size of the categories list
        override fun getItemCount(): Int { return categories.size }
    }

    // View Binding that represents this activity content view
    private lateinit var binding: MainBinding

    // The categories adapter
    private lateinit var adapter: CategoryAdapter

    // The actual categories list
    private var categories = emptyList<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Create a new Main layout binding and set this activity content view to its root
        binding = MainBinding.inflate(layoutInflater).also { setContentView(it.root) }

        // Initialize the views properties
        binding.apply {

            // Set the Categories RecyclerView to have a linear layout, a divider decoration
            // and set the adapter to the previously created category adapter
            Categories.layoutManager = LinearLayoutManager(activity)
            Categories.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
            Categories.adapter = CategoryAdapter().also { adapter = it }

            // When the SearchArea of the SearchBar is clicked, start the search activity
            SearchBar.SearchArea.setOnClickListener { ActivitySearch.start(activity) }

            // When the Favorites ImageView of the SearchBar is clicked, start the favorites activity
            SearchBar.Favorites.setOnClickListener { start<ActivityFavorites> {} }

            // Sets the SearchBar text color to light
            SearchBar.Query.setTextColor(ResourcesCompat.getColor(resources, R.color.lightText, theme))
        }

        // This is executed on a coroutine so we can use the IO Dispatcher when performing the remote request
        launchMain {

            // Request the categories from the MeLi API and store it on the categories list
            // If this operation fails, inform the user about this error
            categories = Server.categories() ?: emptyList<Category>().also { notify(R.string.exCategoriesLoad) }

            // If the categories were successfully loaded
            if (categories.isNotEmpty()) {

                // Notify the adapter that the data set has changed
                adapter.notifyDataSetChanged()

                // Show the Categories RecyclerView
                binding.Categories.show()
            }

            // Hide the Progress bar
            binding.Progress.gone()
        }
    }
}