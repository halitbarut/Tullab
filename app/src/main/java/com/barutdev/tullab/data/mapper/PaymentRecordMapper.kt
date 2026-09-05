package com.barutdev.tullab.data.mapper

import com.barutdev.tullab.data.local.entity.PaymentRecordEntity
import com.barutdev.tullab.domain.model.PaymentRecord

fun PaymentRecordEntity.toDomain(): PaymentRecord = PaymentRecord(
    id = id,
    studentId = studentId,
    amountMinor = amountMinor,
    paidAtEpochMs = paidAtEpochMs
)

fun List<PaymentRecordEntity>.toDomain(): List<PaymentRecord> = map(PaymentRecordEntity::toDomain)

fun PaymentRecord.toEntity(): PaymentRecordEntity = PaymentRecordEntity(
    id = id,
    studentId = studentId,
    amountMinor = amountMinor,
    paidAtEpochMs = paidAtEpochMs
)