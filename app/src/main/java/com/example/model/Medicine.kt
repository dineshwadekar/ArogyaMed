package com.example.model

enum class MedicineCategory(val displayName: String, val iconName: String) {
    ALL("All Products", "apps"),
    PRESCRIPTION("Prescription", "medical_services"),
    DIABETES("Diabetes Care", "monitor_heart"),
    CARDIAC("Cardiac & BP", "favorite"),
    AYURVEDA("Ayurvedic & Herbal", "spa"),
    VITAMINS("Vitamins & Immunity", "local_pharmacy"),
    FIRST_AID("First Aid & Pain", "healing"),
    BABY_CARE("Mother & Baby", "child_care"),
    DEVICES("Health Devices", "biotech")
}

data class Medicine(
    val id: String,
    val name: String,
    val composition: String,
    val manufacturer: String,
    val category: MedicineCategory,
    val form: String,
    val packSize: String,
    val mrp: Double,
    val price: Double,
    val discountPercent: Int,
    val requiresPrescription: Boolean,
    val inStock: Boolean = true,
    val rating: Float = 4.8f,
    val ratingCount: Int = 1240,
    val uses: String,
    val howItWorks: String,
    val directions: String,
    val sideEffects: String,
    val storage: String = "Store below 25°C in a dry place. Protect from direct sunlight.",
    val warnings: String = "Keep out of reach of children. Consult doctor before use during pregnancy."
)
