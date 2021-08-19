package com.efeinfo.meli.challenge

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoritesTest {

    lateinit var context: Context

    @Before
    fun before() { context = InstrumentationRegistry.getInstrumentation().targetContext }

    // Test a favorite insertion
    @Test
    fun insertion() {

        context.getSharedPreferences("com.efeinfo.meli.challenge", Context.MODE_PRIVATE).edit().remove("Favorites").commit()

        Assert.assertEquals(0, Favorites.get(context).size)
        Favorites.add(context, "MLA123456789")

        Assert.assertEquals(1, Favorites.get(context).size)
        Assert.assertEquals("MLA123456789", Favorites.get(context)[0])
    }

    // Test multiple favorites insertions
    @Test
    fun multiInsertion() {

        context.getSharedPreferences("com.efeinfo.meli.challenge", Context.MODE_PRIVATE).edit().remove("Favorites").commit()

        Assert.assertEquals(0, Favorites.get(context).size)
        Favorites.add(context, "MLA000000001")

        Assert.assertEquals(1, Favorites.get(context).size)
        Assert.assertEquals("MLA000000001", Favorites.get(context)[0])

        Favorites.add(context, "MLA000000002")

        Assert.assertEquals(2, Favorites.get(context).size)
        Assert.assertEquals("MLA000000002", Favorites.get(context)[1])

        Favorites.add(context, "MLA000000003")

        Assert.assertEquals(3, Favorites.get(context).size)
        Assert.assertEquals("MLA000000003", Favorites.get(context)[2])
    }

    // Test a favorite deletion
    @Test
    fun deletion() {

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        context.getSharedPreferences("com.efeinfo.meli.challenge", Context.MODE_PRIVATE).edit().remove("Favorites").putString("Favorites", "MLA123456789").commit()

        Assert.assertEquals(1, Favorites.get(context).size)
        Assert.assertEquals("MLA123456789", Favorites.get(context)[0])

        Favorites.remove(context, "MLA123456789")

        Assert.assertEquals(0, Favorites.get(context).size)
    }

    // Test multiple favorites deletions
    @Test
    fun multiDeletion() {

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        context.getSharedPreferences("com.efeinfo.meli.challenge", Context.MODE_PRIVATE).edit().remove("Favorites").putString("Favorites", "MLA000000001 MLA000000002 MLA000000003").commit()

        Assert.assertEquals(3, Favorites.get(context).size)
        Assert.assertEquals("MLA000000001", Favorites.get(context)[0])

        Favorites.remove(context, "MLA000000001")

        Assert.assertEquals(2, Favorites.get(context).size)
        Assert.assertEquals("MLA000000002", Favorites.get(context)[0])

        Favorites.remove(context, "MLA000000002")

        Assert.assertEquals(1, Favorites.get(context).size)
        Assert.assertEquals("MLA000000003", Favorites.get(context)[0])

        Favorites.remove(context, "MLA000000003")

        Assert.assertEquals(0, Favorites.get(context).size)
    }
}