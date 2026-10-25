package com.example.model

data class CartItem(
    val medicine: Medicine,
    val quantity: Int = 1
) {
    val totalPrice: Double
        get() = medicine.price * quantity

    val totalMrp: Double
        get() = medicine.mrp * quantity

    val totalSavings: Double
        get() = totalMrp - totalPrice
}
