package edu.umg.programacion2.edu.umg.programacion2.gastos.Datos

import edu.umg.programacion2.edu.umg.programacion2.gastos.modelo.Gastos

interface GastoRepository {
    suspend fun agregar (gasto: Gastos): Gastos
    suspend fun listar(): List<Gastos>
    suspend fun buscarPorId(id: Int): Gastos?
    suspend fun actualizar(gastos: Gastos): Boolean
    suspend fun eliminar(id: Int): Boolean
}