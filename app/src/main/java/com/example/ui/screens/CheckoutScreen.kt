package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedicineData
import com.example.model.CartItem
import com.example.model.DeliveryAddress
import com.example.model.Medicine
import com.example.model.Prescription
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentGreenLight
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
fun CheckoutScreen(
    items: List<CartItem>,
    singleMedicine: Medicine?,
    initialAddress: DeliveryAddress,
    initialPaymentMethod: String,
    activePrescription: Prescription?,
    onBack: () -> Unit,
    onAddressChange: (DeliveryAddress) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onPlaceOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    var fullName by remember { mutableStateOf(initialAddress.fullName.ifBlank { "Dinesh Sharma" }) }
    var phone by remember { mutableStateOf(initialAddress.phone.ifBlank { "9820154321" }) }
    var flatHouse by remember { mutableStateOf(initialAddress.flatHouse.ifBlank { "Flat 402, Green Meadows" }) }
    var areaStreet by remember { mutableStateOf(initialAddress.areaStreet.ifBlank { "Linking Road, Bandra West" }) }
    var landmark by remember { mutableStateOf(initialAddress.landmark.ifBlank { "Near National College" }) }
    var city by remember { mutableStateOf(initialAddress.city.ifBlank { "Mumbai" }) }
    var selectedState by remember { mutableStateOf(initialAddress.state.ifBlank { "Maharashtra" }) }
    var pincode by remember { mutableStateOf(initialAddress.pincode.ifBlank { "400050" }) }
    var addressType by remember { mutableStateOf(initialAddress.addressType) }
    var stateDropdownExpanded by remember { mutableStateOf(false) }

    var selectedPaymentMethod by remember { mutableStateOf(initialPaymentMethod) }

    fun syncAddress() {
        onAddressChange(
            DeliveryAddress(
                fullName = fullName,
                phone = phone,
                flatHouse = flatHouse,
                areaStreet = areaStreet,
                landmark = landmark,
                city = city,
                state = selectedState,
                pincode = pincode,
                addressType = addressType
            )
        )
    }

    val orderItems: List<CartItem> = if (singleMedicine != null) {
        listOf(CartItem(medicine = singleMedicine, quantity = 1))
    } else {
        items
    }

    val subtotal = orderItems.sumOf { it.totalPrice }
    val mrpTotal = orderItems.sumOf { it.totalMrp }
    val deliveryFee = if (subtotal >= 499.0) 0.0 else 40.0
    val totalAmount = subtotal + deliveryFee
    val savings = mrpTotal - subtotal

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
                        Text(
                            text = "Checkout & Delivery Address",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("checkout_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        },

        bottomBar = {
            Surface(
                color = PharmaWhite,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Payable",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "₹${String.format("%.2f", totalAmount)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmaTealPrimary
                        )
                    }

                    Button(
                        onClick = {
                            syncAddress()
                            onPaymentMethodChange(selectedPaymentMethod)
                            onPlaceOrder()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PharmaTealPrimary,
                            contentColor = PharmaWhite
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("place_order_button")
                    ) {
                        Text(
                            text = if (selectedPaymentMethod.contains("Cash on Delivery"))
                                "Place Order (COD)"
                            else
                                "Place Order",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PharmaWhite)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Customer & Delivery Address Form
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PharmaWhite),
                border = BorderStroke(1.dp, PharmaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PharmaTealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Customer & Delivery Address",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Full Name
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            syncAddress()
                        },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PharmaTealPrimary,
                            unfocusedBorderColor = PharmaBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Mobile Number
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            syncAddress()
                        },
                        label = { Text("Mobile Number (+91)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = {
                            Text(
                                text = "+91 ",
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_phone_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PharmaTealPrimary,
                            unfocusedBorderColor = PharmaBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Flat / House No
                    OutlinedTextField(
                        value = flatHouse,
                        onValueChange = {
                            flatHouse = it
                            syncAddress()
                        },
                        label = { Text("Flat / House No. / Building Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_house_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PharmaTealPrimary,
                            unfocusedBorderColor = PharmaBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Area / Street
                    OutlinedTextField(
                        value = areaStreet,
                        onValueChange = {
                            areaStreet = it
                            syncAddress()
                        },
                        label = { Text("Area / Street / Colony") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_street_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PharmaTealPrimary,
                            unfocusedBorderColor = PharmaBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Landmark
                    OutlinedTextField(
                        value = landmark,
                        onValueChange = {
                            landmark = it
                            syncAddress()
                        },
                        label = { Text("Landmark (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PharmaTealPrimary,
                            unfocusedBorderColor = PharmaBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // City & Pincode in Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = {
                                city = it
                                syncAddress()
                            },
                            label = { Text("City") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("checkout_city_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PharmaTealPrimary,
                                unfocusedBorderColor = PharmaBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = pincode,
                            onValueChange = {
                                if (it.length <= 6) {
                                    pincode = it
                                    syncAddress()
                                }
                            },
                            label = { Text("Pincode (6 digits)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("checkout_pincode_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PharmaTealPrimary,
                                unfocusedBorderColor = PharmaBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Indian State Dropdown
                    ExposedDropdownMenuBox(
                        expanded = stateDropdownExpanded,
                        onExpandedChange = { stateDropdownExpanded = !stateDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedState,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("State") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PharmaTealPrimary,
                                unfocusedBorderColor = PharmaBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = stateDropdownExpanded,
                            onDismissRequest = { stateDropdownExpanded = false }
                        ) {
                            MedicineData.popularIndianStates.forEach { state ->
                                DropdownMenuItem(
                                    text = { Text(state) },
                                    onClick = {
                                        selectedState = state
                                        syncAddress()
                                        stateDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Address Type Chips
                    Text(
                        text = "Address Type",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Home", "Work", "Other").forEach { type ->
                            FilterChip(
                                selected = addressType == type,
                                onClick = {
                                    addressType = type
                                    syncAddress()
                                },
                                label = { Text(type) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (type == "Home") Icons.Default.Home else Icons.Default.Work,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PharmaTealLight,
                                    selectedLabelColor = PharmaTealDark,
                                    selectedLeadingIconColor = PharmaTealDark
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Attached Prescription Review (if present)
            if (activePrescription != null) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentGreenLight),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Prescription Attached",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF14532D)
                            )
                            Text(
                                text = "${activePrescription.title} (${activePrescription.doctorName})",
                                fontSize = 11.sp,
                                color = Color(0xFF166534)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = AccentGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Payment Options - Specially "Cash on Delivery"
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PharmaWhite),
                border = BorderStroke(1.dp, PharmaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Payment Method",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "100% Safe & Secure pan-India transaction",
                        fontSize = 11.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option 1: Cash on Delivery (COD) - Highlighted
                    PaymentOptionRow(
                        title = "Cash on Delivery (COD)",
                        subtitle = "Pay cash or UPI on doorstep delivery (Recommended)",
                        icon = Icons.Default.LocalAtm,
                        isSelected = selectedPaymentMethod.startsWith("Cash on Delivery"),
                        badgeText = "₹0 Extra Fee",
                        onSelect = {
                            selectedPaymentMethod = "Cash on Delivery (COD)"
                            onPaymentMethodChange(selectedPaymentMethod)
                        },
                        tag = "payment_method_cod"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 2: UPI
                    PaymentOptionRow(
                        title = "UPI (Google Pay, PhonePe, Paytm)",
                        subtitle = "Instant payment via any UPI app",
                        icon = Icons.Default.QrCode,
                        isSelected = selectedPaymentMethod.startsWith("UPI"),
                        onSelect = {
                            selectedPaymentMethod = "UPI (Google Pay, PhonePe, Paytm)"
                            onPaymentMethodChange(selectedPaymentMethod)
                        },
                        tag = "payment_method_upi"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 3: Cards
                    PaymentOptionRow(
                        title = "Credit / Debit Card",
                        subtitle = "Visa, Mastercard, RuPay, Maestro",
                        icon = Icons.Default.CreditCard,
                        isSelected = selectedPaymentMethod.startsWith("Credit"),
                        onSelect = {
                            selectedPaymentMethod = "Credit / Debit Card"
                            onPaymentMethodChange(selectedPaymentMethod)
                        },
                        tag = "payment_method_card"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 4: Net Banking
                    PaymentOptionRow(
                        title = "Net Banking",
                        subtitle = "All major Indian banks supported",
                        icon = Icons.Default.AccountBalance,
                        isSelected = selectedPaymentMethod.startsWith("Net"),
                        onSelect = {
                            selectedPaymentMethod = "Net Banking"
                            onPaymentMethodChange(selectedPaymentMethod)
                        },
                        tag = "payment_method_netbanking"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Order Items & Bill Summary
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PharmaWhite),
                border = BorderStroke(1.dp, PharmaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Order Items (${orderItems.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    orderItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.medicine.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Qty: ${item.quantity} • ${item.medicine.packSize}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            Text(
                                text = "₹${String.format("%.2f", item.totalPrice)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = PharmaBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Item Total", fontSize = 12.sp, color = TextSecondary)
                        Text(text = "₹${String.format("%.2f", subtotal)}", fontSize = 12.sp, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Delivery Charges", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = if (deliveryFee == 0.0) "FREE" else "₹40.00",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (deliveryFee == 0.0) AccentGreen else TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Savings on MRP", fontSize = 12.sp, color = AccentGreen)
                        Text(text = "₹${String.format("%.2f", savings)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AccentGreen)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = PharmaBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Payable Amount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            text = "₹${String.format("%.2f", totalAmount)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PharmaTealPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PaymentOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    badgeText: String? = null,
    onSelect: () -> Unit,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp,
            if (isSelected) PharmaTealPrimary else PharmaBorder
        ),
        color = if (isSelected) PharmaTealLight else PharmaWhite,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = PharmaTealPrimary
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) PharmaWhite else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) PharmaTealPrimary else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (badgeText != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AccentGreenLight
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGreen,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}
