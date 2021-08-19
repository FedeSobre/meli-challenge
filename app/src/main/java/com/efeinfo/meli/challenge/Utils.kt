package com.efeinfo.meli.challenge

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.coroutines.*

// Collection of helper methods to change the visibility of a view in a less verbose way
fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.INVISIBLE }
fun View.gone() { visibility = View.GONE }

// Converts from Display DPI to Pixels
fun dpToPxI(context: Context, dp: Float): Int { return (dp * context.resources.displayMetrics.density).toInt() }

// Helper method to toast a message to the screen
fun Activity.notify(id: Int, short: Boolean = false) = Toast.makeText(this, resources.getText(id), if (short) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()

// Helper value to retrieve "this" activity
// This is used inside nested blocks of code where "this" has been modified
// Less verbose than "this@ActivityName"
inline val Activity.activity: Activity; get() = this

// Function to finish an activity if an error occurred
fun Activity.fail(display: Int, log: String? = null) {

    notify(display)
    log?.also { Log.e("MeLi", it) }
    finish()
}

// Helper function to execute a coroutine in the main (UI) thread
fun launchMain(callback: suspend () -> Unit) { CoroutineScope(Dispatchers.Main).launch { callback() } }

// Helper function to change the scope of a coroutine
suspend fun<T> withIO(callback: suspend () -> T) = withContext(Dispatchers.IO) { callback() }

// Methods to try to execute a block of code and, if this blocks throws an exception,
// logs that exception to the console and returns null
fun<T> tryNull(report: String, callback: () -> T) = try { callback() } catch (ex: Exception) { Log.e("MeLi", "$report: ${ex.localizedMessage ?: "No exception message"}"); null }
suspend fun<T> coTryNull(report: String, callback: suspend () -> T) = try { callback() } catch (ex: Exception) { Log.e("MeLi", "$report: ${ex.localizedMessage ?: "No exception message"}"); null }

// Helper method to store a list of strings to the shared preferences
fun saveStrings(context: Context, strings: List<String>, name: String, separator: String) {

    context.getSharedPreferences("com.efeinfo.meli.challenge", Context.MODE_PRIVATE).edit().run {

        putString(name, strings.joinToString(separator))
        apply()
    }
}

// Helper method to load a list of strings from the shared preferences
fun loadStrings(context: Context, name: String, delimiter: String) =
    context.getSharedPreferences("com.efeinfo.meli.challenge", Context.MODE_PRIVATE)
    .getString(name, "")
    ?.split(delimiter)
    ?.filter { it.isNotBlank() }
    ?: emptyList()

// Helper method to start a new activity from another activity
inline fun<reified T> Activity.start(callback: Intent.() -> Unit) { startActivity(Intent(this, T::class.java).apply { callback() }) }