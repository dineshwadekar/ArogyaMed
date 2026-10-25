package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.MedicineData
import com.example.model.CartItem
import com.example.model.DeliveryAddress
import com.example.model.Medicine
import com.example.model.MedicineCategory
import com.example.model.Order
import com.example.model.Prescription
import com.example.model.SellerProfile
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class ScreenTab {
    HOME,
    CATEGORIES,
    CART,
    ORDERS,
    PROFILE
}

enum class PendingPrescriptionAction {
    ADD_TO_CART,
    BUY_NOW,
    NONE
}

class PharmaViewModel : ViewModel() {

    private val firestoreRepository by lazy { com.example.data.FirestoreRepository() }

    private val _isSyncingToCloud = MutableStateFlow(false)
    val isSyncingToCloud: StateFlow<Boolean> = _isSyncingToCloud.asStateFlow()

    // Light / Dark Theme toggle state
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    private val _medicinesList = MutableStateFlow<List<Medicine>>(MedicineData.sampleMedicines)
    val medicinesList: StateFlow<List<Medicine>> = _medicinesList.asStateFlow()
    val allMedicines: List<Medicine> get() = _medicinesList.value

    // Seller / Pharmacist Distributor Management
    private val _sellerProfile = MutableStateFlow<SellerProfile?>(null)
    val sellerProfile: StateFlow<SellerProfile?> = _sellerProfile.asStateFlow()

    private val _isSellerPortalOpen = MutableStateFlow(false)
    val isSellerPortalOpen: StateFlow<Boolean> = _isSellerPortalOpen.asStateFlow()

    init {
        try {
            firestoreRepository.listenToProducts(
                onProductsUpdated = { liveProducts ->
                    if (liveProducts.isNotEmpty()) {
                        _medicinesList.value = liveProducts

                        // Reflect updated cost/price immediately in currently opened medicine detail
                        val currentSelected = _selectedMedicine.value
                        if (currentSelected != null) {
                            val updated = liveProducts.find { it.id == currentSelected.id }
                            if (updated != null) {
                                _selectedMedicine.value = updated
                            }
                        }

                        // Reflect updated cost/price in active cart items
                        if (_cartItems.value.isNotEmpty()) {
                            _cartItems.value = _cartItems.value.map { item ->
                                val updatedMed = liveProducts.find { it.id == item.medicine.id }
                                if (updatedMed != null) item.copy(medicine = updatedMed) else item
                            }
                        }
                    }
                }
            )
        } catch (e: Exception) {
            // Local fallback
        }
    }

    fun openSellerPortal() {
        _isSellerPortalOpen.value = true
    }

    fun closeSellerPortal() {
        _isSellerPortalOpen.value = false
    }

    fun registerSeller(name: String, businessName: String, drugLicense: String, email: String, phone: String) {
        val newSeller = SellerProfile(
            sellerId = "seller_" + System.currentTimeMillis().toString().takeLast(6),
            name = name.trim(),
            businessName = businessName.trim().ifBlank { "ArogyaMed Distribution Partner" },
            drugLicenseNumber = drugLicense.trim().ifBlank { "DL-MH-2026-4421" },
            email = email.trim(),
            phone = phone.trim(),
            isVerified = true,
            isLoggedIn = true
        )
        _sellerProfile.value = newSeller
        try {
            firestoreRepository.saveSellerProfile(newSeller)
        } catch (e: Exception) {
            // Local fallback
        }
        _snackbarMessage.value = "Distributor registered! Welcome ${newSeller.businessName}"
    }

    fun loginSeller(email: String, businessName: String = "ArogyaMed Partner Pharmacy") {
        val seller = SellerProfile(
            sellerId = "seller_" + (1000..9999).random(),
            name = email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() },
            businessName = businessName,
            drugLicenseNumber = "DL-MH-2026-9842",
            email = email.trim(),
            phone = "+91 98201 54321",
            isVerified = true,
            isLoggedIn = true
        )
        _sellerProfile.value = seller
        try {
            firestoreRepository.saveSellerProfile(seller)
        } catch (e: Exception) {
            // Local fallback
        }
        _snackbarMessage.value = "Logged in as Distributor: ${seller.businessName}"
    }

    fun logoutSeller() {
        _sellerProfile.value = null
        _snackbarMessage.value = "Logged out from Seller Portal"
    }

    fun updateMedicinePrice(medicineId: String, newPrice: Double, newMrp: Double? = null) {
        // 1. Immediately update reactive StateFlow for instant UI recomposition
        _medicinesList.value = _medicinesList.value.map { med ->
            if (med.id == medicineId) {
                val mrp = newMrp ?: med.mrp
                val discount = if (mrp > newPrice && mrp > 0.0) (((mrp - newPrice) / mrp) * 100).toInt() else 0
                med.copy(price = newPrice, mrp = mrp, discountPercent = discount)
            } else med
        }

        // Also update selected medicine if currently open
        val currentSelected = _selectedMedicine.value
        if (currentSelected?.id == medicineId) {
            val mrp = newMrp ?: currentSelected.mrp
            val discount = if (mrp > newPrice && mrp > 0.0) (((mrp - newPrice) / mrp) * 100).toInt() else 0
            _selectedMedicine.value = currentSelected.copy(price = newPrice, mrp = mrp, discountPercent = discount)
        }

        // 2. Persist to Firestore
        firestoreRepository.updateMedicinePrice(
            medicineId = medicineId,
            newPrice = newPrice,
            newMrp = newMrp,
            onSuccess = {
                _snackbarMessage.value = "Updated price to ₹${String.format("%.2f", newPrice)} & synced to Firestore!"
            },
            onError = { err ->
                _snackbarMessage.value = "Price updated in app (Firestore: $err)"
            }
        )
    }

    fun addSellerMedicine(medicine: Medicine) {
        _medicinesList.value = listOf(medicine) + _medicinesList.value
        firestoreRepository.saveMedicine(
            medicine = medicine,
            onSuccess = {
                _snackbarMessage.value = "Medicine ${medicine.name} added to catalog & synced!"
            },
            onError = { err ->
                _snackbarMessage.value = "Medicine added in app (Firestore: $err)"
            }
        )
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        _orderHistory.value = _orderHistory.value.map { order ->
            if (order.orderId == orderId) order.copy(status = newStatus) else order
        }
        firestoreRepository.updateOrderStatus(
            orderId = orderId,
            newStatus = newStatus,
            onSuccess = {
                _snackbarMessage.value = "Order #$orderId status updated to $newStatus"
            },
            onError = { err ->
                _snackbarMessage.value = "Order status updated locally"
            }
        )
    }

    private val _activeTab = MutableStateFlow(ScreenTab.HOME)
    val activeTab: StateFlow<ScreenTab> = _activeTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(MedicineCategory.ALL)
    val selectedCategory: StateFlow<MedicineCategory> = _selectedCategory.asStateFlow()

    private val _selectedMedicine = MutableStateFlow<Medicine?>(null)
    val selectedMedicine: StateFlow<Medicine?> = _selectedMedicine.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _uploadedPrescriptions = MutableStateFlow<List<Prescription>>(
        listOf(MedicineData.sampleVerifiedPrescription)
    )
    val uploadedPrescriptions: StateFlow<List<Prescription>> = _uploadedPrescriptions.asStateFlow()

    private val _activePrescription = MutableStateFlow<Prescription?>(null)
    val activePrescription: StateFlow<Prescription?> = _activePrescription.asStateFlow()

    private val _isPrescriptionDialogOpen = MutableStateFlow(false)
    val isPrescriptionDialogOpen: StateFlow<Boolean> = _isPrescriptionDialogOpen.asStateFlow()

    private val _pendingAction = MutableStateFlow(PendingPrescriptionAction.NONE)
    val pendingAction: StateFlow<PendingPrescriptionAction> = _pendingAction.asStateFlow()

    private val _targetMedicine = MutableStateFlow<Medicine?>(null)
    val targetMedicine: StateFlow<Medicine?> = _targetMedicine.asStateFlow()

    // Profile & Authentication
    private val _userProfile = MutableStateFlow(
        UserProfile(
            fullName = "Dinesh Sharma",
            email = "dinesh.wadekar@gmail.com",
            phone = "+91 98201 54321",
            isLoggedIn = true,
            savedAddresses = listOf(
                DeliveryAddress(
                    fullName = "Dinesh Sharma",
                    phone = "+91 98201 54321",
                    flatHouse = "B-402, Green Meadows",
                    areaStreet = "Linking Road, Bandra West",
                    landmark = "Near National College",
                    city = "Mumbai",
                    state = "Maharashtra",
                    pincode = "400050",
                    addressType = "Home"
                )
            ),
            savedPrescriptions = listOf(MedicineData.sampleVerifiedPrescription)
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Checkout State
    private val _isCheckoutOpen = MutableStateFlow(false)
    val isCheckoutOpen: StateFlow<Boolean> = _isCheckoutOpen.asStateFlow()

    private val _checkoutSingleMedicine = MutableStateFlow<Medicine?>(null)
    val checkoutSingleMedicine: StateFlow<Medicine?> = _checkoutSingleMedicine.asStateFlow()

    private val _deliveryAddress = MutableStateFlow(
        DeliveryAddress(
            fullName = "Dinesh Sharma",
            phone = "9820154321",
            flatHouse = "Flat 402, Green Meadows Tower",
            areaStreet = "Linking Road, Bandra West",
            landmark = "Near National College",
            city = "Mumbai",
            state = "Maharashtra",
            pincode = "400050",
            addressType = "Home"
        )
    )
    val deliveryAddress: StateFlow<DeliveryAddress> = _deliveryAddress.asStateFlow()

    // Payment method - default to Cash on Delivery as requested
    private val _paymentMethod = MutableStateFlow("Cash on Delivery (COD)")
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    // Order History & Latest Order
    private val _orderHistory = MutableStateFlow<List<Order>>(emptyList())
    val orderHistory: StateFlow<List<Order>> = _orderHistory.asStateFlow()

    private val _latestOrder = MutableStateFlow<Order?>(null)
    val latestOrder: StateFlow<Order?> = _latestOrder.asStateFlow()

    private val _isOrderSuccessOpen = MutableStateFlow(false)
    val isOrderSuccessOpen: StateFlow<Boolean> = _isOrderSuccessOpen.asStateFlow()

    private val _currentPincode = MutableStateFlow("400050")
    val currentPincode: StateFlow<String> = _currentPincode.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun syncProductsToCloud() {
        _isSyncingToCloud.value = true
        firestoreRepository.syncDemoProductsToFirestore(
            onSuccess = { count ->
                _isSyncingToCloud.value = false
                _snackbarMessage.value = "Successfully synced $count medicines to Firestore!"
            },
            onError = { err ->
                _isSyncingToCloud.value = false
                _snackbarMessage.value = "Firestore sync: $err"
            }
        )
    }

    fun setActiveTab(tab: ScreenTab) {
        _activeTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: MedicineCategory) {
        _selectedCategory.value = category
    }

    fun selectMedicine(medicine: Medicine?) {
        _selectedMedicine.value = medicine
    }

    fun setPincode(pincode: String) {
        _currentPincode.value = pincode
        _snackbarMessage.value = "Delivery location updated: Pincode $pincode (Express 24-hr delivery available)"
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    /**
     * Handle Add to Cart or Buy Now.
     * Prescription mandate has been removed per request:
     * users can directly add any medicine to cart or purchase immediately.
     */
    fun onMedicineAction(medicine: Medicine, isBuyNow: Boolean) {
        _targetMedicine.value = medicine
        if (isBuyNow) {
            initiateBuyNow(medicine)
        } else {
            addToCart(medicine)
        }
    }


    fun openPrescriptionUploadDialog(action: PendingPrescriptionAction = PendingPrescriptionAction.NONE, med: Medicine? = null) {
        _pendingAction.value = action
        _targetMedicine.value = med
        _isPrescriptionDialogOpen.value = true
    }

    fun closePrescriptionDialog() {
        _isPrescriptionDialogOpen.value = false
        _pendingAction.value = PendingPrescriptionAction.NONE
    }

    fun attachPrescription(prescription: Prescription) {
        _activePrescription.value = prescription
        if (!_uploadedPrescriptions.value.any { it.id == prescription.id }) {
            _uploadedPrescriptions.value = listOf(prescription) + _uploadedPrescriptions.value
        }
        _snackbarMessage.value = "Prescription attached: ${prescription.fileName}"

        // Execute pending action if any
        val med = _targetMedicine.value
        val action = _pendingAction.value
        closePrescriptionDialog()

        if (med != null) {
            when (action) {
                PendingPrescriptionAction.ADD_TO_CART -> addToCart(med)
                PendingPrescriptionAction.BUY_NOW -> initiateBuyNow(med)
                PendingPrescriptionAction.NONE -> Unit
            }
        }
    }

    fun uploadPrescriptionFromDevice(patientName: String, doctorName: String, fileName: String, uriString: String? = null) {
        val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        val newRx = Prescription(
            id = "rx_" + UUID.randomUUID().toString().take(8),
            title = "Doctor's Rx for $patientName",
            fileName = fileName.ifBlank { "Prescription_Document_${System.currentTimeMillis().toString().takeLast(4)}.jpg" },
            uploadedAt = "Uploaded " + dateFormat.format(Date()),
            patientName = patientName.ifBlank { _userProfile.value.fullName.ifBlank { "Patient" } },
            doctorName = doctorName.ifBlank { "Registered Medical Practitioner" },
            status = "Verified by Chief Pharmacist",
            uriString = uriString,
            isSample = false
        )
        attachPrescription(newRx)
    }

    fun useSamplePrescription() {
        attachPrescription(MedicineData.sampleVerifiedPrescription)
    }

    fun addToCart(medicine: Medicine, qty: Int = 1) {
        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.medicine.id == medicine.id }
        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            currentList[existingIndex] = existing.copy(quantity = existing.quantity + qty)
        } else {
            currentList.add(CartItem(medicine = medicine, quantity = qty))
        }
        _cartItems.value = currentList
        _snackbarMessage.value = "${medicine.name} added to cart"
    }

    fun updateCartQuantity(medicineId: String, newQty: Int) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.medicine.id == medicineId }
        if (index >= 0) {
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = currentList[index].copy(quantity = newQty)
            }
            _cartItems.value = currentList
        }
    }

    fun removeFromCart(medicineId: String) {
        _cartItems.value = _cartItems.value.filterNot { it.medicine.id == medicineId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun initiateBuyNow(medicine: Medicine) {
        _checkoutSingleMedicine.value = medicine
        _isCheckoutOpen.value = true
    }

    fun initiateCartCheckout() {
        if (_cartItems.value.isEmpty()) return
        _checkoutSingleMedicine.value = null
        _isCheckoutOpen.value = true
    }


    fun closeCheckout() {
        _isCheckoutOpen.value = false
        _checkoutSingleMedicine.value = null
    }

    fun updateDeliveryAddress(address: DeliveryAddress) {
        _deliveryAddress.value = address
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
    }

    fun placeOrder() {
        val itemsToOrder: List<CartItem> = if (_checkoutSingleMedicine.value != null) {
            listOf(CartItem(medicine = _checkoutSingleMedicine.value!!, quantity = 1))
        } else {
            _cartItems.value
        }

        if (itemsToOrder.isEmpty()) return

        val subtotal = itemsToOrder.sumOf { it.totalPrice }
        val mrpTotal = itemsToOrder.sumOf { it.totalMrp }
        val deliveryFee = if (subtotal >= 499.0) 0.0 else 40.0
        val totalAmount = subtotal + deliveryFee
        val savings = mrpTotal - subtotal

        val orderNum = (10000..99999).random()
        val orderId = "AM-$orderNum"
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val estFormat = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())
        val estDate = estFormat.format(Date(System.currentTimeMillis() + 86400000L * 2))

        val order = Order(
            orderId = orderId,
            items = itemsToOrder,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            totalAmount = totalAmount,
            savings = savings,
            deliveryAddress = _deliveryAddress.value,
            paymentMethod = _paymentMethod.value,
            status = "Order Placed",
            orderDate = dateFormat.format(Date()),
            estimatedDeliveryDate = estDate,
            prescriptionAttached = _activePrescription.value
        )

        _orderHistory.value = listOf(order) + _orderHistory.value
        _latestOrder.value = order

        // Sync order to Firestore so the Seller Admin Panel receives it in real-time
        try {
            firestoreRepository.saveOrderToFirestore(order)
        } catch (e: Exception) {
            // Local fallback
        }

        // If cart items were ordered, clear cart
        if (_checkoutSingleMedicine.value == null) {
            clearCart()
        }

        _isCheckoutOpen.value = false
        _checkoutSingleMedicine.value = null
        _isOrderSuccessOpen.value = true
    }

    fun closeOrderSuccess() {
        _isOrderSuccessOpen.value = false
    }

    // Profile & Auth
    fun saveUserProfile(name: String, email: String, phone: String) {
        _userProfile.value = _userProfile.value.copy(
            fullName = name.trim(),
            email = email.trim(),
            phone = phone.trim(),
            isLoggedIn = true
        )
        // Also update delivery address name and phone
        _deliveryAddress.value = _deliveryAddress.value.copy(
            fullName = name.trim(),
            phone = phone.trim()
        )
        try {
            firestoreRepository.saveUserProfileToFirestore(_userProfile.value)
        } catch (e: Exception) {
            // Local fallback
        }
        _snackbarMessage.value = "Profile saved successfully!"
    }

    fun logoutProfile() {
        _userProfile.value = _userProfile.value.copy(
            isLoggedIn = false
        )
        _snackbarMessage.value = "Logged out from ArogyaMed"
    }

    fun loginProfile(name: String, email: String, phone: String) {
        saveUserProfile(name, email, phone)
    }
}
