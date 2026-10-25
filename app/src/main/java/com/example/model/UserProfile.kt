package com.example.model

data class DeliveryAddress(
    val fullName: String = "",
    val phone: String = "",
    val flatHouse: String = "",
    val areaStreet: String = "",
    val landmark: String = "",
    val city: String = "",
    val state: String = "Maharashtra",
    val pincode: String = "",
    val addressType: String = "Home" // Home, Work, Other
) {
    val formattedAddress: String
        get() = listOf(flatHouse, areaStreet, landmark, "$city, $state - $pincode")
            .filter { it.isNotBlank() }
            .joinToString(", ")
}

data class UserProfile(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val isLoggedIn: Boolean = false,
    val savedAddresses: List<DeliveryAddress> = emptyList(),
    val savedPrescriptions: List<Prescription> = emptyList()
)
