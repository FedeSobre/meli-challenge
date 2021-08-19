package com.efeinfo.meli.challenge

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.efeinfo.meli.challenge.data.Category
import com.efeinfo.meli.challenge.data.Product
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.min

@RunWith(AndroidJUnit4::class)
class ServerTest {

    // Test that retrieves the categories from the MeLi API
    @Test
    fun categoriesTest() = runBlocking { Assert.assertNotNull(Server.categories()) }

    // Test that retrieves a category based product list from the MeLi API
    @Test
    fun categoryTest() = runBlocking {

        val categories = Server.categories()!!
        Assert.assertNotNull(Server.searchCategory(categories[0].id))
    }

    // Test an invalid category search
    @Test
    fun invalidCategoryTest() = runBlocking { Assert.assertEquals(emptyList<Category>(), Server.searchCategory("")) }

    // Test that retrieves a query based product list from the MeLi API
    @Test
    fun queryTest() = runBlocking { Assert.assertNotNull(Server.searchQuery("celular")) }

    // Test an invalid query search
    @Test
    fun invalidQueryTest() = runBlocking { Assert.assertEquals(emptyList<Product>(), Server.searchQuery("")) }

    // Test that retrieves multiItem based product list from the MeLi API
    @Test
    fun itemsTest() = runBlocking {

        val categories = Server.categories()!!
        val products = Server.searchCategory(categories[0].id)!!.apply { subList(0, min(size, 20)) }

        Assert.assertNotNull(Server.items(products.map { it.id }))
    }

    // Test an invalid items retrieval
    @Test
    fun invalidItemsTest() = runBlocking { Assert.assertNull(Server.items(emptyList())) }

    // Test that retrieves the pictures list of a product from the MeLi API
    @Test
    fun picturesTest() = runBlocking { Assert.assertNotNull(Server.pictures(Server.searchQuery("celular")!![0].id)) }

    // Test an invalid pictures retrieval
    @Test
    fun invalidPicturesTest() = runBlocking { Assert.assertNull(Server.pictures("")) }

    // Test that retrieves the description of a product from the MeLi API
    @Test
    fun descriptionTest() = runBlocking { Assert.assertNotNull(Server.description(Server.searchQuery("celular")!![0].id)) }

    // Test an invalid description retrieval
    @Test
    fun invalidDescriptionTest() = runBlocking { Assert.assertNull(Server.description("")) }

    // Test that retrieves ARS, USD and EUR currencies from the MeLi API
    @Test
    fun currencyTest() = runBlocking {

        Assert.assertEquals("$", Server.currency("ARS"))
        Assert.assertEquals("U\$S", Server.currency("USD"))
        Assert.assertEquals("€", Server.currency("EUR"))
    }

    // Test an invalid currency from the MeLi API
    @Test
    fun invalidCurrencyTest() = runBlocking { Assert.assertNull(Server.currency("INV")) }
}