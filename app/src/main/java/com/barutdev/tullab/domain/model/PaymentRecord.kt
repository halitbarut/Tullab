package com.barutdev.tullab.domain.model

data class PaymentRecord(
    val id: Int,
    val studentId: Int,
    val amountMinor: Long,
    val paidAtEpochMs: Long
)