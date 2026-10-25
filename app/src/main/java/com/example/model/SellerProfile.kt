package com.example.model

data class SellerProfile(
    val sellerId: String = "",
    val name: String = "",
    val businessName: String = "",
    val drugLicenseNumber: String = "",
    val email: String = "",
    val phone: String = "",
    val isVerified: Boolean = true,
    val isLoggedIn: Boolean = false
)
