package com.example.data

import android.util.Log
import com.example.model.CartItem
import com.example.model.Medicine
import com.example.model.MedicineCategory
import com.example.model.Order
import com.example.model.SellerProfile
import com.example.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

class FirestoreRepository {

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private var productsListener: ListenerRegistration? = null

    /**
     * Uploads the full sample demo medicines catalog to Firestore 'products' collection.
     */
    fun syncDemoProductsToFirestore(
        medicines: List<Medicine> = MedicineData.sampleMedicines,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> },
        onSuccess: (count: Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val batch = db.batch()
        var addedCount = 0

        for ((index, med) in medicines.withIndex()) {
            val docRef = db.collection("products").document(med.id)
            val productMap = hashMapOf<String, Any>(
                "id" to med.id,
                "name" to med.name,
                "composition" to med.composition,
                "manufacturer" to med.manufacturer,
                "category" to med.category.name,
                "categoryDisplayName" to med.category.displayName,
                "form" to med.form,
                "packSize" to med.packSize,
                "mrp" to med.mrp,
                "price" to med.price,
                "cost" to med.price, // Synced as both price and cost
                "sellingPrice" to med.price,
                "discountPercent" to med.discountPercent,
                "requiresPrescription" to med.requiresPrescription,
                "inStock" to med.inStock,
                "rating" to med.rating.toDouble(),
                "ratingCount" to med.ratingCount,
                "uses" to med.uses,
                "howItWorks" to med.howItWorks,
                "directions" to med.directions,
                "sideEffects" to med.sideEffects,
                "storage" to med.storage,
                "warnings" to med.warnings,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            batch.set(docRef, productMap, SetOptions.merge())
            addedCount++
            onProgress(index + 1, medicines.size)
        }

        batch.commit()
            .addOnSuccessListener {
                Log.d("FirestoreRepo", "Successfully synced $addedCount demo products to Firestore!")
                onSuccess(addedCount)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreRepo", "Failed to sync demo products", e)
                onError(e.localizedMessage ?: "Failed to sync products to Firestore")
            }
    }

    /**
     * Realtime listener for products from Firestore.
     * Extracts price/cost robustly whether stored as Number or String, or under "cost", "price", or "sellingPrice".
     */
    fun listenToProducts(
        onProductsUpdated: (List<Medicine>) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        productsListener?.remove()
        productsListener = db.collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreRepo", "Error listening to products: ${error.message}")
                    onError(error.localizedMessage ?: "Failed to listen to products")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val products = mutableListOf<Medicine>()
                    for (doc in snapshot.documents) {
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val name = doc.getString("name") ?: ""
                            val composition = doc.getString("composition") ?: ""
                            val manufacturer = doc.getString("manufacturer") ?: ""
                            val categoryStr = doc.getString("category") ?: MedicineCategory.ALL.name
                            val category = try {
                                MedicineCategory.valueOf(categoryStr)
                            } catch (e: Exception) {
                                MedicineCategory.PRESCRIPTION
                            }
                            val form = doc.getString("form") ?: "Tablet"
                            val packSize = doc.getString("packSize") ?: ""

                            // Robust extraction of price/cost (handles number, string, "cost", "price", "sellingPrice")
                            val price: Double = (doc.get("price") as? Number)?.toDouble()
                                ?: (doc.get("cost") as? Number)?.toDouble()
                                ?: (doc.get("sellingPrice") as? Number)?.toDouble()
                                ?: doc.getString("price")?.toDoubleOrNull()
                                ?: doc.getString("cost")?.toDoubleOrNull()
                                ?: doc.getString("sellingPrice")?.toDoubleOrNull()
                                ?: 0.0

                            val mrp: Double = (doc.get("mrp") as? Number)?.toDouble()
                                ?: (doc.get("maxRetailPrice") as? Number)?.toDouble()
                                ?: doc.getString("mrp")?.toDoubleOrNull()
                                ?: doc.getString("maxRetailPrice")?.toDoubleOrNull()
                                ?: price

                            val discountPercent = if (mrp > price && mrp > 0.0) {
                                (((mrp - price) / mrp) * 100).toInt()
                            } else {
                                (doc.get("discountPercent") as? Number)?.toInt() ?: 0
                            }

                            val requiresPrescription = doc.getBoolean("requiresPrescription") ?: false
                            val inStock = doc.getBoolean("inStock") ?: true
                            val rating = (doc.get("rating") as? Number)?.toFloat() ?: 4.8f
                            val ratingCount = (doc.get("ratingCount") as? Number)?.toInt() ?: 100
                            val uses = doc.getString("uses") ?: ""
                            val howItWorks = doc.getString("howItWorks") ?: ""
                            val directions = doc.getString("directions") ?: ""
                            val sideEffects = doc.getString("sideEffects") ?: ""
                            val storage = doc.getString("storage") ?: "Store in cool dry place."
                            val warnings = doc.getString("warnings") ?: ""

                            products.add(
                                Medicine(
                                    id = id,
                                    name = name,
                                    composition = composition,
                                    manufacturer = manufacturer,
                                    category = category,
                                    form = form,
                                    packSize = packSize,
                                    mrp = mrp,
                                    price = price,
                                    discountPercent = discountPercent,
                                    requiresPrescription = requiresPrescription,
                                    inStock = inStock,
                                    rating = rating,
                                    ratingCount = ratingCount,
                                    uses = uses,
                                    howItWorks = howItWorks,
                                    directions = directions,
                                    sideEffects = sideEffects,
                                    storage = storage,
                                    warnings = warnings
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("FirestoreRepo", "Error parsing product ${doc.id}", e)
                        }
                    }
                    if (products.isNotEmpty()) {
                        onProductsUpdated(products)
                    }
                }
            }
    }

    /**
     * Updates medicine cost/price directly in Firestore.
     * Updates both 'price' and 'cost' fields so either can be queried.
     */
    fun updateMedicinePrice(
        medicineId: String,
        newPrice: Double,
        newMrp: Double? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val updates = hashMapOf<String, Any>(
            "price" to newPrice,
            "cost" to newPrice,
            "sellingPrice" to newPrice,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )
        if (newMrp != null && newMrp > 0.0) {
            updates["mrp"] = newMrp
            val discount = if (newMrp > newPrice) (((newMrp - newPrice) / newMrp) * 100).toInt() else 0
            updates["discountPercent"] = discount
        }

        db.collection("products").document(medicineId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FirestoreRepo", "Updated medicine $medicineId cost to $newPrice in Firestore")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreRepo", "Failed to update cost for $medicineId", e)
                onError(e.localizedMessage ?: "Failed to update price in Firestore")
            }
    }

    /**
     * Adds or updates a complete medicine to Firestore.
     */
    fun saveMedicine(
        medicine: Medicine,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val productMap = hashMapOf<String, Any>(
            "id" to medicine.id,
            "name" to medicine.name,
            "composition" to medicine.composition,
            "manufacturer" to medicine.manufacturer,
            "category" to medicine.category.name,
            "categoryDisplayName" to medicine.category.displayName,
            "form" to medicine.form,
            "packSize" to medicine.packSize,
            "mrp" to medicine.mrp,
            "price" to medicine.price,
            "cost" to medicine.price,
            "sellingPrice" to medicine.price,
            "discountPercent" to medicine.discountPercent,
            "requiresPrescription" to medicine.requiresPrescription,
            "inStock" to medicine.inStock,
            "rating" to medicine.rating.toDouble(),
            "ratingCount" to medicine.ratingCount,
            "uses" to medicine.uses,
            "howItWorks" to medicine.howItWorks,
            "directions" to medicine.directions,
            "sideEffects" to medicine.sideEffects,
            "storage" to medicine.storage,
            "warnings" to medicine.warnings,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("products").document(medicine.id)
            .set(productMap, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.localizedMessage ?: "Failed to save medicine") }
    }

    /**
     * Saves placed orders directly to Firestore so the Seller Admin Panel receives them in real-time.
     */
    fun saveOrderToFirestore(
        order: Order,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val itemsList = order.items.map { item ->
            hashMapOf(
                "medicineId" to item.medicine.id,
                "medicineName" to item.medicine.name,
                "packSize" to item.medicine.packSize,
                "quantity" to item.quantity,
                "price" to item.medicine.price,
                "cost" to item.medicine.price,
                "totalPrice" to item.totalPrice
            )
        }

        val orderMap = hashMapOf<String, Any>(
            "orderId" to order.orderId,
            "orderDate" to order.orderDate,
            "estimatedDeliveryDate" to order.estimatedDeliveryDate,
            "status" to order.status,
            "paymentMethod" to order.paymentMethod,
            "subtotal" to order.subtotal,
            "deliveryFee" to order.deliveryFee,
            "savings" to order.savings,
            "totalAmount" to order.totalAmount,
            "itemsCount" to order.items.size,
            "items" to itemsList,
            "customerName" to order.deliveryAddress.fullName,
            "customerPhone" to order.deliveryAddress.phone,
            "deliveryAddress" to order.deliveryAddress.formattedAddress,
            "city" to order.deliveryAddress.city,
            "state" to order.deliveryAddress.state,
            "pincode" to order.deliveryAddress.pincode,
            "addressType" to order.deliveryAddress.addressType,
            "prescriptionAttached" to (order.prescriptionAttached != null),
            "prescriptionTitle" to (order.prescriptionAttached?.title ?: "N/A"),
            "prescriptionFileName" to (order.prescriptionAttached?.fileName ?: "N/A"),
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("orders").document(order.orderId)
            .set(orderMap)
            .addOnSuccessListener {
                Log.d("FirestoreRepo", "Order ${order.orderId} written to Firestore!")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreRepo", "Failed to save order to Firestore", e)
                onError(e.localizedMessage ?: "Failed to save order")
            }
    }

    /**
     * Updates an order's fulfillment status in Firestore (e.g. from Seller Portal).
     */
    fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        db.collection("orders").document(orderId)
            .update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.localizedMessage ?: "Failed to update order status") }
    }

    /**
     * Saves or registers seller in Firestore 'sellers' collection.
     */
    fun saveSellerProfile(
        seller: SellerProfile,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val sellerMap = hashMapOf<String, Any>(
            "sellerId" to seller.sellerId,
            "name" to seller.name,
            "businessName" to seller.businessName,
            "drugLicenseNumber" to seller.drugLicenseNumber,
            "email" to seller.email,
            "phone" to seller.phone,
            "isVerified" to seller.isVerified,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("sellers").document(seller.sellerId)
            .set(sellerMap, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.localizedMessage ?: "Failed to save seller profile") }
    }

    /**
     * Saves customer profile details to Firestore 'users' collection.
     */
    fun saveUserProfileToFirestore(profile: UserProfile) {
        val userMap = hashMapOf<String, Any>(
            "fullName" to profile.fullName,
            "email" to profile.email,
            "phone" to profile.phone,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )
        val docId = if (profile.email.isNotBlank()) profile.email.replace(".", "_") else "guest_user"
        db.collection("users").document(docId)
            .set(userMap, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FirestoreRepo", "User profile synced to Firestore!")
            }
    }

    fun cleanup() {
        productsListener?.remove()
    }
}
