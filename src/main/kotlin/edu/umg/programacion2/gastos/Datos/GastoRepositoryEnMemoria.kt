package edu.umg.programacion2.edu.umg.programacion2.gastos.Datos

import edu.umg.programacion2.edu.umg.programacion2.gastos.modelo.Gastos

class GastoRepositoryEnMemoria : GastoRepository {
    private val gastos = mutableListOf<Gastos>()
    private var siguienteId = 1


    override suspend fun agregar(gasto: Gastos): Gastos {
        val guardado= gasto.copy(id= siguienteId)
        siguienteId++
        gastos.add(guardado)
        return guardado
    }

    override suspend fun listar(): List<Gastos> = gastos.toList()


    override suspend fun buscarPorId(id: Int): Gastos? {
        for (gastos in gastos) {
            if (gastos.id == siguienteId) {
                return gastos}
        }
        return null
    }

    override suspend fun actualizar(gasto: Gastos): Boolean {
        val posicion = buscarPosicion(gasto.id)
        if (posicion == -1) {
            return false
        }
        gastos [posicion] = gasto
        return true
    }

    override suspend fun eliminar(id: Int): Boolean {
    val posicion = buscarPosicion(id)
        if (posicion == -1) {
            return false
        }
        gastos.removeAt(posicion)
        return true
    }
    private suspend fun buscarPosicion(id: Int): Int {
    for (posicion in gastos.indices) {
        if (gastos[posicion].id == id) {
            return posicion
        }
    }
    return -1
    }

}