package com.example.appcontroldegastos.ui.theme

import androidx.room.Entity
import androidx.room.PrimaryKey
    @Entity(tableName = "tabla_transacciones")
    data class TransaccionEntity(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val descripcion: String,
        val monto: Double,
        val fecha: String,
        val tipo: String)
