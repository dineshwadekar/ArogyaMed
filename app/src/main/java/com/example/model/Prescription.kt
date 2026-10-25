package com.example.model

data class Prescription(
    val id: String,
    val title: String,
    val fileName: String,
    val uploadedAt: String,
    val patientName: String,
    val doctorName: String = "Dr. S. K. Sharma (MBBS, MD)",
    val status: String = "Verified by Pharmacist",
    val uriString: String? = null,
    val isSample: Boolean = false
)
