package com.hastakala.shop.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hastakala.shop.data.local.AppDatabase
import com.hastakala.shop.data.local.Product
import com.hastakala.shop.data.local.Sale
import com.hastakala.shop.data.local.Variant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppDatabaseTest {
    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun bestSellerQueryAggregatesByVariant() = runTest {
        val productId = database.productDao().insert(
            Product(name = "Banana Bag", category = "Bag", basePrice = 250.0, imageUri = null)
        )
        val redVariantId = database.variantDao().insert(
            Variant(productId = productId, color = "Red", stock = 10, lowStockThreshold = 2)
        )
        val blueVariantId = database.variantDao().insert(
            Variant(productId = productId, color = "Blue", stock = 8, lowStockThreshold = 2)
        )

        database.saleDao().insert(
            Sale(productId = productId, variantId = redVariantId, quantity = 3, totalPrice = 750.0)
        )
        database.saleDao().insert(
            Sale(productId = productId, variantId = blueVariantId, quantity = 1, totalPrice = 250.0)
        )

        val rows = database.saleDao().observeBestSellerRows().first()

        assertEquals(2, rows.size)
        assertEquals("Red", rows.first().color)
        assertEquals(3, rows.first().quantitySold)
    }
}
