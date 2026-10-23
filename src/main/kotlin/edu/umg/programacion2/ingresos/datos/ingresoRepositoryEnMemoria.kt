package edu.umg.programacion2.edu.umg.programacion2.Ingresos.Datos

import edu.umg.programacion2.edu.umg.programacion2.Ingresos.Modelo.ingresos


class ingresoRepositoryEnMemoria : ingresoRepository{
    private val ingresos = mutableListOf<ingresos>()
    private var siguienteId = 1

    override suspend fun agregar(ingreso: ingresos): ingresos {
        val guardado= ingreso.copy(id= siguienteId)
        siguienteId++
        ingresos.add(guardado)
        return guardado
    }
    override suspend fun listar(): List<ingresos> = ingresos.toList()


    override suspend fun buscarPorId(id: Int): ingresos? {
        for (gastos in ingresos) {
            if (gastos.id == siguienteId) {
                return gastos}
        }
        return null
    }

    override suspend fun actualizar(ingreso: ingresos): Boolean {
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