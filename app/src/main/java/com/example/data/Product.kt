package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val expiryDate: Long, // timestamp
    val purchaseDate: Long = System.currentTimeMillis(),
    val quantity: String = "",
    val notes: String = "",
    val isConsumed: Boolean = false,
    val imageUrl: String? = null,
    val price: Double = 0.0,
    val isB2bOnly: Boolean = false
) {
    fun getDaysRemaining(currentTime: Long = System.currentTimeMillis()): Int {
        val diff = expiryDate - currentTime
        return if (diff < 0) {
            val days = (diff / (24 * 60 * 60 * 1000)).toInt()
            if (days == 0) -1 else days
        } else {
            (diff / (24 * 60 * 60 * 1000)).toInt()
        }
    }
}
