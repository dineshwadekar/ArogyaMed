package com.example

import com.example.data.MedicineData
import com.example.model.CartItem
import com.example.model.MedicineCategory
import com.example.viewmodel.PharmaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    private lateinit var viewModel: PharmaViewModel

    @Before
    fun setup() {
        viewModel = PharmaViewModel()
    }

    @Test
    fun catalogContainsAuthenticIndianMedicines() {
        val medicines = viewModel.allMedicines
        assertTrue("Catalog should contain sample medicines", medicines.isNotEmpty())

        val dolo = medicines.find { it.name.contains("Dolo", ignoreCase = true) }
        assertNotNull("Dolo 650 should exist in catalog", dolo)
        assertFalse("Dolo 650 is OTC", dolo!!.requiresPrescription)

        val augmentin = medicines.find { it.name.contains("Augmentin", ignoreCase = true) }
        assertNotNull("Augmentin should exist in catalog", augmentin)
    }

    @Test
    fun medicineCartCalculationsAndSavings() {
        val dolo = viewModel.allMedicines.first { it.id == "med_dolo650" }
        val cartItem = CartItem(medicine = dolo, quantity = 2)

        assertEquals(dolo.price * 2, cartItem.totalPrice, 0.01)
        assertEquals(dolo.mrp * 2, cartItem.totalMrp, 0.01)
        assertTrue(cartItem.totalSavings > 0)
    }

    @Test
    fun noPrescriptionMandateFlowOnScheduledMedicine() {
        val telma = viewModel.allMedicines.first { it.id == "med_telma40" }

        // Trigger action on medicine: prescription mandate removed, adds directly to cart
        viewModel.onMedicineAction(telma, isBuyNow = false)

        assertFalse("Prescription dialog should not be mandated", viewModel.isPrescriptionDialogOpen.value)
        assertTrue("Medicine should be added directly to cart", viewModel.cartItems.value.any { it.medicine.id == telma.id })
    }

    @Test
    fun themeToggleFlow() {
        assertFalse("Default theme should be light", viewModel.isDarkMode.value)

        viewModel.toggleDarkMode()
        assertTrue("Theme should toggle to dark", viewModel.isDarkMode.value)

        viewModel.toggleDarkMode()
        assertFalse("Theme should toggle back to light", viewModel.isDarkMode.value)

        viewModel.setDarkMode(true)
        assertTrue("setDarkMode(true) should enable dark mode", viewModel.isDarkMode.value)
    }

    @Test
    fun checkoutWithCashOnDelivery() {
        val dolo = viewModel.allMedicines.first { it.id == "med_dolo650" }
        viewModel.addToCart(dolo, qty = 1)

        viewModel.initiateCartCheckout()
        assertTrue(viewModel.isCheckoutOpen.value)

        viewModel.setPaymentMethod("Cash on Delivery (COD)")
        viewModel.placeOrder()

        val latestOrder = viewModel.latestOrder.value
        assertNotNull(latestOrder)
        assertEquals("Cash on Delivery (COD)", latestOrder?.paymentMethod)
        assertTrue(latestOrder!!.orderId.startsWith("AM-"))
        assertTrue(viewModel.isOrderSuccessOpen.value)
        assertTrue("Cart should be cleared after checkout", viewModel.cartItems.value.isEmpty())
    }

    @Test
    fun userProfileCreationAndEdit() {
        viewModel.saveUserProfile(
            name = "Ramesh Gupta",
            email = "ramesh.gupta@example.com",
            phone = "+91 9876543210"
        )

        val profile = viewModel.userProfile.value
        assertEquals("Ramesh Gupta", profile.fullName)
        assertEquals("ramesh.gupta@example.com", profile.email)
        assertEquals("+91 9876543210", profile.phone)
        assertTrue(profile.isLoggedIn)
    }
}
