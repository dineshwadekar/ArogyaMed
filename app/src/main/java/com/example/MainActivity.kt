package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.MedicineCategory
import com.example.ui.components.ArogyaTopBar
import com.example.ui.components.PincodeDialog
import com.example.ui.components.PrescriptionUploadDialog
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MedicineDetailScreen
import com.example.ui.screens.OrderSuccessScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PharmaBorder
import com.example.ui.theme.PharmaTealLight
import com.example.ui.theme.PharmaTealPrimary
import com.example.ui.theme.PharmaWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.PendingPrescriptionAction
import com.example.viewmodel.PharmaViewModel
import com.example.viewmodel.ScreenTab

import com.example.ui.screens.SellerPortalScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PharmaViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkMode) {
                PharmaApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PharmaApp(
    viewModel: PharmaViewModel = viewModel()
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val medicines by viewModel.medicinesList.collectAsStateWithLifecycle()
    val isSellerPortalOpen by viewModel.isSellerPortalOpen.collectAsStateWithLifecycle()
    val sellerProfile by viewModel.sellerProfile.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedMedicine by viewModel.selectedMedicine.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val uploadedPrescriptions by viewModel.uploadedPrescriptions.collectAsStateWithLifecycle()
    val activePrescription by viewModel.activePrescription.collectAsStateWithLifecycle()
    val isPrescriptionDialogOpen by viewModel.isPrescriptionDialogOpen.collectAsStateWithLifecycle()
    val pendingAction by viewModel.pendingAction.collectAsStateWithLifecycle()
    val targetMedicine by viewModel.targetMedicine.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isCheckoutOpen by viewModel.isCheckoutOpen.collectAsStateWithLifecycle()
    val checkoutSingleMedicine by viewModel.checkoutSingleMedicine.collectAsStateWithLifecycle()
    val deliveryAddress by viewModel.deliveryAddress.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.paymentMethod.collectAsStateWithLifecycle()
    val orderHistory by viewModel.orderHistory.collectAsStateWithLifecycle()
    val latestOrder by viewModel.latestOrder.collectAsStateWithLifecycle()
    val isOrderSuccessOpen by viewModel.isOrderSuccessOpen.collectAsStateWithLifecycle()
    val currentPincode by viewModel.currentPincode.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showPincodeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

    // Hardware/Gesture Back Handling
    BackHandler(
        enabled = isSellerPortalOpen || isOrderSuccessOpen || isCheckoutOpen || selectedMedicine != null || activeTab != ScreenTab.HOME
    ) {
        when {
            isSellerPortalOpen -> viewModel.closeSellerPortal()
            isOrderSuccessOpen -> viewModel.closeOrderSuccess()
            isCheckoutOpen -> viewModel.closeCheckout()
            selectedMedicine != null -> viewModel.selectMedicine(null)
            activeTab != ScreenTab.HOME -> viewModel.setActiveTab(ScreenTab.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!isSellerPortalOpen && !isCheckoutOpen && !isOrderSuccessOpen && selectedMedicine == null) {
                ArogyaTopBar(
                    currentPincode = currentPincode,
                    cartItemCount = cartItems.sumOf { it.quantity },
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onPincodeClick = { showPincodeDialog = true },
                    onCartClick = { viewModel.setActiveTab(ScreenTab.CART) },
                    onSellerPortalClick = {
                        viewModel.openSellerPortal()
                    }
                )
            }
        },

        bottomBar = {
            if (!isSellerPortalOpen && !isCheckoutOpen && !isOrderSuccessOpen && selectedMedicine == null) {
                val navBarItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    // Home Tab
                    NavigationBarItem(
                        selected = activeTab == ScreenTab.HOME,
                        onClick = { viewModel.setActiveTab(ScreenTab.HOME) },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == ScreenTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = {
                            Text(
                                text = "Home",
                                fontSize = 11.sp,
                                fontWeight = if (activeTab == ScreenTab.HOME) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = navBarItemColors,
                        modifier = Modifier.testTag("nav_tab_home")
                    )

                    // Categories Tab
                    NavigationBarItem(
                        selected = activeTab == ScreenTab.CATEGORIES,
                        onClick = { viewModel.setActiveTab(ScreenTab.CATEGORIES) },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == ScreenTab.CATEGORIES) Icons.Filled.Category else Icons.Outlined.Category,
                                contentDescription = "Categories"
                            )
                        },
                        label = {
                            Text(
                                text = "Categories",
                                fontSize = 11.sp,
                                fontWeight = if (activeTab == ScreenTab.CATEGORIES) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = navBarItemColors,
                        modifier = Modifier.testTag("nav_tab_categories")
                    )

                    // Cart Tab
                    val totalCartQty = cartItems.sumOf { it.quantity }
                    NavigationBarItem(
                        selected = activeTab == ScreenTab.CART,
                        onClick = { viewModel.setActiveTab(ScreenTab.CART) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (totalCartQty > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ) {
                                            Text(
                                                text = totalCartQty.toString(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (activeTab == ScreenTab.CART) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                                    contentDescription = "Cart"
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "Cart",
                                fontSize = 11.sp,
                                fontWeight = if (activeTab == ScreenTab.CART) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = navBarItemColors,
                        modifier = Modifier.testTag("nav_tab_cart")
                    )

                    // Orders Tab
                    NavigationBarItem(
                        selected = activeTab == ScreenTab.ORDERS,
                        onClick = { viewModel.setActiveTab(ScreenTab.ORDERS) },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == ScreenTab.ORDERS) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                                contentDescription = "Orders"
                            )
                        },
                        label = {
                            Text(
                                text = "Orders",
                                fontSize = 11.sp,
                                fontWeight = if (activeTab == ScreenTab.ORDERS) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = navBarItemColors,
                        modifier = Modifier.testTag("nav_tab_orders")
                    )

                    // Profile Tab
                    NavigationBarItem(
                        selected = activeTab == ScreenTab.PROFILE,
                        onClick = { viewModel.setActiveTab(ScreenTab.PROFILE) },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == ScreenTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "Profile"
                            )
                        },
                        label = {
                            Text(
                                text = "Profile",
                                fontSize = 11.sp,
                                fontWeight = if (activeTab == ScreenTab.PROFILE) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = navBarItemColors,
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {

            when {
                // Seller Portal Flow
                isSellerPortalOpen -> {
                    SellerPortalScreen(
                        sellerProfile = sellerProfile,
                        medicines = medicines,
                        orders = orderHistory,
                        onBack = { viewModel.closeSellerPortal() },
                        onRegisterSeller = { name, bName, dl, email, phone ->
                            viewModel.registerSeller(name, bName, dl, email, phone)
                        },
                        onLoginSeller = { email ->
                            viewModel.loginSeller(email)
                        },
                        onLogoutSeller = {
                            viewModel.logoutSeller()
                        },
                        onUpdatePrice = { medId, newPrice, newMrp ->
                            viewModel.updateMedicinePrice(medId, newPrice, newMrp)
                        },
                        onAddMedicine = { med ->
                            viewModel.addSellerMedicine(med)
                        },
                        onUpdateOrderStatus = { orderId, newStatus ->
                            viewModel.updateOrderStatus(orderId, newStatus)
                        }
                    )
                }

                // Order Success Flow
                isOrderSuccessOpen && latestOrder != null -> {
                    OrderSuccessScreen(
                        order = latestOrder!!,
                        onViewOrders = {
                            viewModel.closeOrderSuccess()
                            viewModel.setActiveTab(ScreenTab.ORDERS)
                        },
                        onContinueShopping = {
                            viewModel.closeOrderSuccess()
                            viewModel.setActiveTab(ScreenTab.HOME)
                        }
                    )
                }

                // Checkout Flow (Form + Address + Cash on Delivery)
                isCheckoutOpen -> {
                    CheckoutScreen(
                        items = cartItems,
                        singleMedicine = checkoutSingleMedicine,
                        initialAddress = deliveryAddress,
                        initialPaymentMethod = paymentMethod,
                        activePrescription = activePrescription,
                        onBack = { viewModel.closeCheckout() },
                        onAddressChange = { viewModel.updateDeliveryAddress(it) },
                        onPaymentMethodChange = { viewModel.setPaymentMethod(it) },
                        onPlaceOrder = { viewModel.placeOrder() }
                    )
                }

                // Medicine Detail View with all requirements
                selectedMedicine != null -> {
                    val currentMed = medicines.find { it.id == selectedMedicine?.id } ?: selectedMedicine!!
                    MedicineDetailScreen(
                        medicine = currentMed,
                        hasAttachedPrescription = activePrescription != null,
                        onBack = { viewModel.selectMedicine(null) },
                        onAddToCart = { med ->
                            viewModel.onMedicineAction(med, isBuyNow = false)
                        },
                        onBuyNow = { med ->
                            viewModel.onMedicineAction(med, isBuyNow = true)
                        }
                    )
                }

                // Main Tab Views
                else -> {
                    when (activeTab) {
                        ScreenTab.HOME -> {
                            HomeScreen(
                                medicines = medicines,
                                cartItems = cartItems,
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onCategorySelect = { viewModel.setSelectedCategory(it) },
                                onMedicineClick = { viewModel.selectMedicine(it) },
                                onAddToCart = { med ->
                                    viewModel.onMedicineAction(med, isBuyNow = false)
                                },
                                onIncrementCart = { med ->
                                    viewModel.addToCart(med, 1)
                                },
                                onDecrementCart = { med ->
                                    val currentQty = cartItems.find { it.medicine.id == med.id }?.quantity ?: 0
                                    viewModel.updateCartQuantity(med.id, currentQty - 1)
                                },
                                onUploadPrescriptionClick = {
                                    viewModel.openPrescriptionUploadDialog(
                                        action = PendingPrescriptionAction.NONE,
                                        med = null
                                    )
                                }
                            )
                        }

                        ScreenTab.CATEGORIES -> {
                            CategoriesScreen(
                                medicines = medicines,
                                onSelectCategory = { cat ->
                                    viewModel.setSelectedCategory(cat)
                                    viewModel.setActiveTab(ScreenTab.HOME)
                                }
                            )
                        }

                        ScreenTab.CART -> {
                            CartScreen(
                                cartItems = cartItems,
                                activePrescription = activePrescription,
                                onIncrement = { id ->
                                    val item = cartItems.find { it.medicine.id == id }
                                    if (item != null) viewModel.updateCartQuantity(id, item.quantity + 1)
                                },
                                onDecrement = { id ->
                                    val item = cartItems.find { it.medicine.id == id }
                                    if (item != null) viewModel.updateCartQuantity(id, item.quantity - 1)
                                },
                                onRemove = { id ->
                                    viewModel.removeFromCart(id)
                                },
                                onUploadPrescription = {
                                    viewModel.openPrescriptionUploadDialog(
                                        action = PendingPrescriptionAction.NONE,
                                        med = null
                                    )
                                },
                                onProceedToCheckout = {
                                    viewModel.initiateCartCheckout()
                                },
                                onBrowseMedicines = {
                                    viewModel.setActiveTab(ScreenTab.HOME)
                                }
                            )
                        }

                        ScreenTab.ORDERS -> {
                            OrdersScreen(
                                orders = orderHistory,
                                onExploreMedicines = { viewModel.setActiveTab(ScreenTab.HOME) }
                            )
                        }

                        ScreenTab.PROFILE -> {
                            ProfileScreen(
                                userProfile = userProfile,
                                prescriptions = uploadedPrescriptions,
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = { viewModel.toggleDarkMode() },
                                onSaveProfile = { name, email, phone ->
                                    viewModel.saveUserProfile(name, email, phone)
                                },
                                onLogout = {
                                    viewModel.logoutProfile()
                                },
                                onUploadNewPrescription = {
                                    viewModel.openPrescriptionUploadDialog(
                                        action = PendingPrescriptionAction.NONE,
                                        med = null
                                    )
                                },
                                onSyncToFirestore = {
                                    viewModel.syncProductsToCloud()
                                },
                                onOpenSellerPortal = {
                                    viewModel.openSellerPortal()
                                }
                            )
                        }

                    }
                }
            }

            // Prescription Upload Modal Sheet
            if (isPrescriptionDialogOpen) {
                PrescriptionUploadDialog(
                    targetMedicine = targetMedicine,
                    pendingAction = pendingAction,
                    savedPrescriptions = uploadedPrescriptions,
                    defaultPatientName = userProfile.fullName,
                    onDismiss = { viewModel.closePrescriptionDialog() },
                    onAttachExisting = { rx ->
                        viewModel.attachPrescription(rx)
                    },
                    onUploadNew = { patientName, doctorName, fileName, uriString ->
                        viewModel.uploadPrescriptionFromDevice(patientName, doctorName, fileName, uriString)
                    },
                    onUseSample = {
                        viewModel.useSamplePrescription()
                    }
                )
            }

            // Pincode Selection Dialog
            if (showPincodeDialog) {
                PincodeDialog(
                    currentPincode = currentPincode,
                    onDismiss = { showPincodeDialog = false },
                    onPincodeSelected = { pin ->
                        viewModel.setPincode(pin)
                    }
                )
            }
        }
    }
}
