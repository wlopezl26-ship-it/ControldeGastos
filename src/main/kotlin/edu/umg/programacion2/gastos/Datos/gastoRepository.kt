package edu.umg.programacion2.edu.umg.programacion2.gastos.Datos

import edu.umg.programacion2.edu.umg.programacion2.gastos.modelo.gastos

interface gastoRepository {
    suspend fun agregar (gasto: gastos): gastos
    suspend fun listar(): List<gastos>
    suspend fun buscarPorId(id: Int): gastos?
    suspend fun actualizar(gastos: gastos): Boolean
    suspend fun eliminar(id: Int): Boolean
}