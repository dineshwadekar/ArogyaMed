package com.example.model

data class Order(
    val orderId: String,
    val items: List<CartItem>,
    val subtotal: Double,
    val deliveryFee: Double,
    val totalAmount: Double,
    val savings: Double,
    val deliveryAddress: DeliveryAddress,
    val paymentMethod: String, // "Cash on Delivery", "UPI", etc.
    val status: String = "Order Placed",
    val orderDate: String,
    val estimatedDeliveryDate: String,
    val prescriptionAttached: Prescription? = null
)
