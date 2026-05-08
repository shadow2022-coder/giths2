package com.hastakala.shop.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hastakala.shop.data.local.AppDatabase
import com.hastakala.shop.data.local.Product
import com.hastakala.shop.data.local.Variant
import com.hastakala.shop.data.repository.ShopRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShopRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: ShopRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ShopRepository(
            database = database,
            productDao = database.productDao(),
            variantDao = database.variantDao(),
            saleDao = database.saleDao(),
            moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build(),
            context = context
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun logSaleDecrementsStockAndStoresSale() = runTest {
        val productId = database.productDao().insert(
            Product(name = "Craft Pouch", category = "Pouch", basePrice = 180.0, imageUri = null)
        )
        val variantId = database.variantDao().insert(
            Variant(productId = productId, color = "Green", stock = 5, lowStockThreshold = 0)
        )

        repository.logSale(productId = productId, variantId = variantId, quantity = 2, totalPrice = 360.0)

        val updatedVariant = database.variantDao().getVariantById(variantId)
        val sales = database.saleDao().getSalesSnapshot()

        assertEquals(3, updatedVariant?.stock)
        assertEquals(1, sales.size)
        assertEquals(360.0, sales.first().totalPrice, 0.0)
    }
}
