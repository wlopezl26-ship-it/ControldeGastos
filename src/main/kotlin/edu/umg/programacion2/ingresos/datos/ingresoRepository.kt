package edu.umg.programacion2.edu.umg.programacion2.Ingresos.Datos

import edu.umg.programacion2.edu.umg.programacion2.Ingresos.Modelo.ingresos

interface ingresoRepository {
    suspend fun agregar (ingreso: ingresos): ingresos
    suspend fun listar(): List<ingresos>
    suspend fun buscarPorId(id: Int): ingresos?
    suspend fun actualizar(ingreso: ingresos): Boolean
    suspend fun eliminar(id: Int): Boolean
}