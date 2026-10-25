package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PharmaBorder
import com.example.ui.theme.PharmaTealLight
import com.example.ui.theme.PharmaTealPrimary
import com.example.ui.theme.PharmaWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PincodeDialog(
    currentPincode: String,
    onDismiss: () -> Unit,
    onPincodeSelected: (String) -> Unit
) {
    var inputPincode by remember { mutableStateOf(currentPincode) }

    val popularCities = listOf(
        "Mumbai" to "400050",
        "Delhi NCR" to "110001",
        "Bengaluru" to "560001",
        "Hyderabad" to "500001",
        "Chennai" to "600001",
        "Kolkata" to "700001",
        "Pune" to "411001"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PharmaTealPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pan-India Delivery Pincode",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Enter 6-digit Indian postal code to check medicine availability & express courier delivery.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = inputPincode,
                    onValueChange = {
                        if (it.length <= 6) inputPincode = it
                    },
                    label = { Text("Pincode (e.g. 400050)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_pincode_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PharmaTealPrimary,
                        unfocusedBorderColor = PharmaBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Popular Delivery Hubs",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    popularCities.take(4).forEach { (city, pin) ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (inputPincode == pin) PharmaTealLight else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    inputPincode = pin
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = city,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (inputPincode == pin) PharmaTealPrimary else TextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = pin,
                                    fontSize = 9.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputPincode.isNotBlank()) {
                        onPincodeSelected(inputPincode)
                    }
                    onDismiss()
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PharmaTealPrimary,
                    contentColor = PharmaWhite
                ),
                modifier = Modifier.testTag("dialog_pincode_apply_button")
            ) {
                Text("Apply & Check", fontWeight = FontWeight.Bold)
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
