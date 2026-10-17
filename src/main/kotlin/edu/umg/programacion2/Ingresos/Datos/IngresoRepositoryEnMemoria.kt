package edu.umg.programacion2.edu.umg.programacion2.Ingresos.Datos

import edu.umg.programacion2.edu.umg.programacion2.Ingresos.Modelo.Ingresos
import edu.umg.programacion2.edu.umg.programacion2.gastos.modelo.Gastos


class IngresoRepositoryEnMemoria : IngresoRepository{
    private val ingresos = mutableListOf<Ingresos>()
    private var siguienteId = 1

    override suspend fun agregar(ingreso: Ingresos): Ingresos {
        val guardado= ingreso.copy(id= siguienteId)
        siguienteId++
        ingresos.add(guardado)
        return guardado
    }
    override suspend fun listar(): List<Ingresos> = ingresos.toList()


    override suspend fun buscarPorId(id: Int): Ingresos? {
        for (gastos in ingresos) {
            if (gastos.id == siguienteId) {
                return gastos}
        }
        return null
    }

    override suspend fun actualizar(ingreso: Ingresos): Boolean {
        val posicion = buscarPosicion(ingreso.id)
        if (posicion == -1) {
            return false
        }
        ingresos [posicion] = ingreso
        return true
    }

    override suspend fun eliminar(id: Int): Boolean {
        val posicion = buscarPosicion(id)
        if (posicion == -1) {
            return false
        }
        ingresos.removeAt(posicion)
        return true
    }
    private suspend fun buscarPosicion(id: Int): Int {
        for (posicion in ingresos.indices) {
            if (ingresos[posicion].id == id) {
                return posicion
            }
        }
        return -1
    }
}