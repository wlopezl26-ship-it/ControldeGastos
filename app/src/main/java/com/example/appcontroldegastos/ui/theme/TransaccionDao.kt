package com.example.appcontroldegastos.ui.theme

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransaccionDao {

    @Insert
    suspend fun insertar(transaccion: TransaccionEntity)

    @Query("SELECT * FROM tabla_transacciones ORDER BY id DESC")
    suspend fun obtenerTodas(): List<TransaccionEntity>

    @Query("DELETE FROM tabla_transacciones WHERE id = :id")
    suspend fun eliminarPorId(id: Long)
}

