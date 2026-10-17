package edu.umg.programacion2.edu.umg.programacion2.Ingresos.Datos

import edu.umg.programacion2.edu.umg.programacion2.Ingresos.Modelo.Ingresos
import edu.umg.programacion2.edu.umg.programacion2.gastos.modelo.Gastos

interface IngresoRepository {
    suspend fun agregar (ingreso: Ingresos): Ingresos
    suspend fun listar(): List<Ingresos>
    suspend fun buscarPorId(id: Int): Ingresos?
    suspend fun actualizar(ingreso: Ingresos): Boolean
    suspend fun eliminar(id: Int): Boolean
}