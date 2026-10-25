package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Medicine
import com.example.model.MedicineCategory
import com.example.model.Order
import com.example.model.SellerProfile
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentGreenLight
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentRedLight
import com.example.ui.theme.PharmaBorder
import com.example.ui.theme.PharmaTealDark
import com.example.ui.theme.PharmaTealLight
import com.example.ui.theme.PharmaTealPrimary
import com.example.ui.theme.PharmaWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerPortalScreen(
    sellerProfile: SellerProfile?,
    medicines: List<Medicine>,
    orders: List<Order>,
    onBack: () -> Unit,
    onRegisterSeller: (name: String, businessName: String, drugLicense: String, email: String, phone: String) -> Unit,
    onLoginSeller: (email: String) -> Unit,
    onLogoutSeller: () -> Unit,
    onUpdatePrice: (medicineId: String, newPrice: Double, newMrp: Double?) -> Unit,
    onAddMedicine: (Medicine) -> Unit,
    onUpdateOrderStatus: (orderId: String, newStatus: String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    var selectedDashboardTab by remember { mutableStateOf(0) } // 0: Products & Cost, 1: Orders
    var editingMedicine by remember { mutableStateOf<Medicine?>(null) }
    var isAddingMedicineOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                TopAppBar(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = 6.dp),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Seller & Distributor Portal",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (sellerProfile != null) sellerProfile.businessName else "Distribute Medicines Pan-India",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        if (sellerProfile != null) {
                            IconButton(onClick = onLogoutSeller) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Log Out",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        }

    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PharmaWhite)
                .padding(innerPadding)
        ) {
            if (sellerProfile == null) {
                // Seller Login & Registration Forms
                SellerAuthView(
                    onRegister = onRegisterSeller,
                    onLogin = onLoginSeller
                )
            } else {
                // Logged In Seller Dashboard
                Column(modifier = Modifier.fillMaxSize()) {
                    // Seller Verified Badge Header
                    Surface(
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, PharmaBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(PharmaTealLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Business,
                                        contentDescription = null,
                                        tint = PharmaTealPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = sellerProfile.businessName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified",
                                            tint = AccentGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = "DL: ${sellerProfile.drugLicenseNumber} • ${sellerProfile.email}",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            Button(
                                onClick = { isAddingMedicineOpen = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PharmaTealPrimary,
                                    contentColor = PharmaWhite
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Med", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Dashboard Tabs
                    TabRow(
                        selectedTabIndex = selectedDashboardTab,
                        containerColor = PharmaWhite,
                        contentColor = PharmaTealPrimary
                    ) {
                        Tab(
                            selected = selectedDashboardTab == 0,
                            onClick = { selectedDashboardTab = 0 },
                            text = { Text("Medicines & Cost (${medicines.size})", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedDashboardTab == 1,
                            onClick = { selectedDashboardTab = 1 },
                            text = { Text("Customer Orders (${orders.size})", fontWeight = FontWeight.Bold) }
                        )
                    }

                    when (selectedDashboardTab) {
                        0 -> {
                            // Medicines List with Quick Price Update
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = PharmaTealLight,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CurrencyRupee,
                                                contentDescription = null,
                                                tint = PharmaTealDark,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Tap 'Update Cost' on any medicine to change price in Firestore in real time.",
                                                fontSize = 12.sp,
                                                color = PharmaTealDark,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                items(medicines, key = { it.id }) { med ->
                                    SellerMedicineRow(
                                        medicine = med,
                                        onEditPriceClick = { editingMedicine = med }
                                    )
                                }
                            }
                        }
                        1 -> {
                            // Live Customer Orders Dispatcher
                            if (orders.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingBag,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "No Orders Received Yet",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Orders placed by customers from the app will show up here.",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(orders, key = { it.orderId }) { order ->
                                        SellerOrderCard(
                                            order = order,
                                            onUpdateStatus = { status ->
                                                onUpdateOrderStatus(order.orderId, status)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Edit Price Dialog
            if (editingMedicine != null) {
                PriceUpdateDialog(
                    medicine = editingMedicine!!,
                    onDismiss = { editingMedicine = null },
                    onConfirm = { newPrice, newMrp ->
                        onUpdatePrice(editingMedicine!!.id, newPrice, newMrp)
                        editingMedicine = null
                    }
                )
            }

            // Add Medicine Modal Dialog
            if (isAddingMedicineOpen) {
                AddMedicineDialog(
                    onDismiss = { isAddingMedicineOpen = false },
                    onSave = { newMed ->
                        onAddMedicine(newMed)
                        isAddingMedicineOpen = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SellerAuthView(
    onRegister: (name: String, businessName: String, drugLicense: String, email: String, phone: String) -> Unit,
    onLogin: (email: String) -> Unit
) {
    var authModeTab by remember { mutableStateOf(0) } // 0: Login, 1: Register

    var name by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var drugLicense by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(PharmaTealLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = null,
                tint = PharmaTealPrimary,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Join ArogyaMed Distributor Network",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Text(
            text = "Sell & supply medicines to patients all across India",
            fontSize = 12.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Tab Row: Login / Register
        TabRow(
            selectedTabIndex = authModeTab,
            containerColor = Color(0xFFF1F5F9),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .height(44.dp)
        ) {
            Tab(
                selected = authModeTab == 0,
                onClick = { authModeTab = 0 },
                text = { Text("Seller Login", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = authModeTab == 1,
                onClick = { authModeTab = 1 },
                text = { Text("Register Pharmacy", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (authModeTab == 0) {
            // LOGIN FORM
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PharmaWhite),
                border = BorderStroke(1.dp, PharmaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Distributor Email ID") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("seller_login_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PharmaTealPrimary,
                            unfocusedBorderColor = PharmaBorder
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("seller_login_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PharmaTealPrimary,
                            unfocusedBorderColor = PharmaBorder
                        )
                    )

                    Button(
                        onClick = {
                            val targetEmail = email.ifBlank { "partner@pharmacy.in" }
                            onLogin(targetEmail)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PharmaTealPrimary,
                            contentColor = PharmaWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("seller_login_submit_button")
                    ) {
                        Text("Log In as Seller", fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = {
                            email = "distributor@arogyamed.in"
                            password = "password123"
                            onLogin("distributor@arogyamed.in")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("1-Tap Demo Seller Login", color = PharmaTealPrimary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            // REGISTER FORM
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PharmaWhite),
                border = BorderStroke(1.dp, PharmaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Pharmacist / Contact Person Name *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("seller_reg_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PharmaTealPrimary,
                            unfocusedBorderColor = PharmaBorder
                        )
                    )

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Pharmacy / Distributor Business Name *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("seller_reg_business_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PharmaTealPrimary,
                            unfocusedBorderColor = PharmaBorder
                        )
                    )

                    OutlinedTextField(
                        value = drugLicense,
                        onValueChange = { drugLicense = it },
                        label = { Text("Drug License / CDSCO Number *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("seller_reg_license_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PharmaTealPrimary,
                            unfocusedBorderColor = PharmaBorder
                        )
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Official Business Email *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("seller_reg_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PharmaTealPrimary,
                            unfocusedBorderColor = PharmaBorder
                        )
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Number (+91) *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("seller_reg_phone_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PharmaTealPrimary,
                            unfocusedBorderColor = PharmaBorder
                        )
                    )

                    Button(
                        onClick = {
                            val finalName = name.ifBlank { "Ramesh Patil" }
                            val finalBusiness = businessName.ifBlank { "Apollo Pharmacy Partner" }
                            val finalLicense = drugLicense.ifBlank { "DL-MH-2026-8812" }
                            val finalEmail = email.ifBlank { "ramesh@apollopartner.in" }
                            val finalPhone = phone.ifBlank { "+91 98200 12345" }
                            onRegister(finalName, finalBusiness, finalLicense, finalEmail, finalPhone)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PharmaTealPrimary,
                            contentColor = PharmaWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("seller_register_submit_button")
                    ) {
                        Text("Register Pharmacy & Start Selling", fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = {
                            name = "Ramesh Patil"
                            businessName = "MedPlus Distribution Hub"
                            drugLicense = "DL-KA-2026-4921"
                            email = "ramesh@medplusdist.in"
                            phone = "9820054321"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fill Sample Pharmacy Info", color = PharmaTealPrimary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SellerMedicineRow(
    medicine: Medicine,
    onEditPriceClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = PharmaWhite),
        border = BorderStroke(1.dp, PharmaBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = medicine.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (medicine.requiresPrescription) AccentRedLight else AccentGreenLight
                    ) {
                        Text(
                            text = if (medicine.requiresPrescription) "Rx" else "OTC",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (medicine.requiresPrescription) AccentRed else AccentGreen,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${medicine.composition} • ${medicine.manufacturer}",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Live Price: ₹${String.format("%.2f", medicine.price)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PharmaTealPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MRP: ₹${String.format("%.2f", medicine.mrp)}",
                        fontSize = 11.sp,
                        color = TextMuted,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = "${medicine.discountPercent}% OFF",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onEditPriceClick,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, PharmaTealPrimary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PharmaTealPrimary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("edit_cost_button_${medicine.id}")
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Update Cost", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SellerOrderCard(
    order: Order,
    onUpdateStatus: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = PharmaWhite),
        border = BorderStroke(1.dp, PharmaBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.orderId}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${order.deliveryAddress.fullName.ifBlank { "Customer" }} • ${order.orderDate}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${String.format("%.2f", order.totalAmount)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PharmaTealPrimary
                    )
                    Text(
                        text = order.paymentMethod,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = PharmaBorder)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Address: ${order.deliveryAddress.formattedAddress}",
                fontSize = 11.sp,
                color = TextSecondary
            )
            Text(
                text = "Contact: ${order.deliveryAddress.phone}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = AccentGreenLight
                ) {
                    Text(
                        text = "Status: ${order.status}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF14532D),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (order.status == "Order Placed") {
                        Button(
                            onClick = { onUpdateStatus("Prescription Verified") },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Verify Rx", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (order.status == "Prescription Verified" || order.status == "Order Placed") {
                        Button(
                            onClick = { onUpdateStatus("Dispatched") },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Dispatch", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (order.status == "Dispatched") {
                        Button(
                            onClick = { onUpdateStatus("Delivered") },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Mark Delivered", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceUpdateDialog(
    medicine: Medicine,
    onDismiss: () -> Unit,
    onConfirm: (newPrice: Double, newMrp: Double?) -> Unit
) {
    var priceInput by remember { mutableStateOf(medicine.price.toString()) }
    var mrpInput by remember { mutableStateOf(medicine.mrp.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Update Medicine Cost / Price", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(medicine.name, fontSize = 12.sp, color = TextMuted)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    label = { Text("Selling Cost / Price (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_new_price_input")
                )

                OutlinedTextField(
                    value = mrpInput,
                    onValueChange = { mrpInput = it },
                    label = { Text("Maximum Retail Price (MRP ₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_new_mrp_input")
                )

                val parsedPrice = priceInput.toDoubleOrNull() ?: medicine.price
                val parsedMrp = mrpInput.toDoubleOrNull() ?: medicine.mrp
                val discount = if (parsedMrp > parsedPrice && parsedMrp > 0.0) (((parsedMrp - parsedPrice) / parsedMrp) * 100).toInt() else 0

                Text(
                    text = "Customer Discount: $discount% OFF • Syncs to Firestore immediately",
                    fontSize = 11.sp,
                    color = AccentGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = priceInput.toDoubleOrNull() ?: medicine.price
                    val m = mrpInput.toDoubleOrNull() ?: medicine.mrp
                    onConfirm(p, m)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary),
                modifier = Modifier.testTag("dialog_save_price_button")
            ) {
                Text("Save & Sync to Firestore", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = PharmaWhite,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun AddMedicineDialog(
    onDismiss: () -> Unit,
    onSave: (Medicine) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var composition by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var packSize by remember { mutableStateOf("Strip of 10 Tablets") }
    var mrp by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MedicineCategory.PRESCRIPTION) }
    var requiresRx by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Distribute New Medicine", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medicine Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = composition,
                    onValueChange = { composition = it },
                    label = { Text("Salt Composition *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = manufacturer,
                    onValueChange = { manufacturer = it },
                    label = { Text("Manufacturer *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = packSize,
                    onValueChange = { packSize = it },
                    label = { Text("Pack Size *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mrp,
                        onValueChange = { mrp = it },
                        label = { Text("MRP (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Cost (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = requiresRx,
                        onClick = { requiresRx = !requiresRx },
                        label = { Text(if (requiresRx) "Prescription Required" else "OTC Product") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = price.toDoubleOrNull() ?: 100.0
                    val m = mrp.toDoubleOrNull() ?: 120.0
                    val discount = if (m > p) (((m - p) / m) * 100).toInt() else 0
                    val med = Medicine(
                        id = "med_" + System.currentTimeMillis().toString().takeLast(6),
                        name = name.ifBlank { "Sample Medicine" },
                        composition = composition.ifBlank { "Generic Salt Composition" },
                        manufacturer = manufacturer.ifBlank { "Cipla Ltd" },
                        category = category,
                        form = "Tablet",
                        packSize = packSize,
                        mrp = m,
                        price = p,
                        discountPercent = discount,
                        requiresPrescription = requiresRx,
                        uses = "Approved therapeutic formulation.",
                        howItWorks = "Mechanism of pharmacological action.",
                        directions = "Take as prescribed by doctor.",
                        sideEffects = "Consult physician if discomfort occurs."
                    )
                    onSave(med)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PharmaTealPrimary)
            ) {
                Text("Add Medicine", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = PharmaWhite,
        shape = RoundedCornerShape(14.dp)
    )
}
