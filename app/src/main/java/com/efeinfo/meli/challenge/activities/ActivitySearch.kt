package com.efeinfo.meli.challenge.activities

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.efeinfo.meli.challenge.*
import com.efeinfo.meli.challenge.databinding.SearchBinding
import com.efeinfo.meli.challenge.databinding.SearchEntryBinding

// Activity that allows to search products based on a query
class ActivitySearch: AppCompatActivity() {

    companion object {

        // Entry point for this activity with its allowed parameters
        fun start(activity: Activity, initial: String? = null) {

            // Starts the Search activity and if initial is not null, add it to the intent
            activity.start<ActivitySearch> { if (initial != null) putExtra("Initial", initial) }

            // Override the pending transition to a Fade In one
            activity.overridePendingTransition(R.anim.popup_enter, R.anim.none)
        }
    }

    // The recent RecyclerView adapter
    inner class RecentAdapter: RecyclerView.Adapter<RecentAdapter.Holder>() {

        // The RecyclerView holder, which takes a Search Entry View Binding as its constructor parameter
        inner class Holder(private val item: SearchEntryBinding): RecyclerView.ViewHolder(item.root) {

            // Initialize the binding root layout parameters to match the parent width, and wrap its height
            init { item.root.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) }

            // Bind this holder, taking a string as its parameter
            fun bind(suggestion: String) {

                // Set the Name view text to the provided suggestion string
                item.Name.text = suggestion

                // When this item is clicked, start a search with this suggestion as query
                item.Root.setOnClickListener { search(suggestion) }

                // When the Insert view is clicked, replace the current Query text to this suggestion
                item.Insert.setOnClickListener {

                    binding.Query.setText(suggestion)
                    binding.Query.setSelection(binding.Query.length())
                }
            }
        }

        // Creates a new holder inflating a Search Entry View Binding
        override fun onCreateViewHolder(group: ViewGroup, type: Int): Holder { return Holder(SearchEntryBinding.inflate(layoutInflater)) }

        // Bind the holder to the suggestion at the given position
        override fun onBindViewHolder(holder: Holder, pos: Int) { holder.bind(recent[pos]) }

        // The count of the adapter is the size of the recent list
        override fun getItemCount(): Int { return recent.size }
    }

    // View Binding that represents this activity content view
    private lateinit var binding: SearchBinding

    // The recent search adapter
    private lateinit var adapter: RecentAdapter

    // The actual recent search list
    private val recent = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Create a new Search layout binding and set this activity content view to its root
        binding = SearchBinding.inflate(layoutInflater).also { setContentView(it.root) }

        // Load the recent search from the shared preferences and append them to the recent list
        recent.addAll(loadStrings(this, "Recent", "\n"))

        // Initialize the views properties
        binding.apply {

            // If an initial query was provided to this activity, set the Query text to that value
            // Else, set it to an empty string
            Query.setText(intent.getStringExtra("Initial") ?: "")

            // When the Query text changes, update the Clear visibility.
            // If the Query text is empty, hide the Clear view, else show it.
            Query.doOnTextChanged { text, _, _, _ -> if (text.toString() == "") Clear.hide() else Clear.show() }

            // Set the focus to the Query view
            Query.requestFocus()

            // When the action button of the soft keyboard is pressed or the enter key is pressed on
            // a physical keyboard
            Query.setOnEditorActionListener { _, _, event ->

                // To avoid double commitment, only consider a physical key event when that key is released
                if (event is KeyEvent && event.action == KeyEvent.ACTION_DOWN) return@setOnEditorActionListener true

                // if the Query text is not empty, start a search with that text as the query
                Query.text.toString().also { if (it != "") search(it) }

                // Return that this action was handled
                true
            }

            // Set the Categories RecyclerView to have a linear layout and set the adapter to the
            // previously created recent adapter
            Suggestions.layoutManager = LinearLayoutManager(activity)
            Suggestions.adapter = RecentAdapter().also { adapter = it }

            // When Back is pressed, end this activity
            Back.setOnClickListener { finish() }

            // When Clear is pressed, clear the Query text
            Clear.setOnClickListener { Query.setText("") }

            // If the initial value of the query is empty, hide the Clear view
            if (Query.text.toString() == "") Clear.hide()
        }
    }

    override fun onPause() {

        super.onPause()

        // This is used to override the transition when this activity is finishing to a
        // Fade Out one
        if (isFinishing) { overridePendingTransition(0, R.anim.popup_exit) }
    }

    // Function that performs a search with the given query
    fun search(query: String) {

        // If this query was already on the recent list, remove it
        if (recent.contains(query)) recent.remove(query)

        // Insert this query at the beginning of the recent list
        recent.add(0, query)

        // Save the recent list to the shared preferences
        saveStrings(this, recent, "Recent", "\n")

        // Start the results activity with this query search parameters
        ActivityResults.start(this, "Query", query)

        // End this activity
        finish()
    }
}