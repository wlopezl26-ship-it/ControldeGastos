package edu.umg.programacion2.edu.umg.programacion2.gastos.Datos

import edu.umg.programacion2.edu.umg.programacion2.gastos.modelo.gastos

class gastoRepositoryEnMemoria : gastoRepository {
    private val gastos = mutableListOf<gastos>()
    private var siguienteId = 1


    override suspend fun agregar(gasto: gastos): gastos {
        val guardado= gasto.copy(id= siguienteId)
        siguienteId++
        gastos.add(guardado)
        return guardado
    }

    override suspend fun listar(): List<gastos> = gastos.toList()


    override suspend fun buscarPorId(id: Int): gastos? {
        for (gastos in gastos) {
            if (gastos.id == siguienteId) {
                return gastos}
        }
        return null
    }

    override suspend fun actualizar(gasto: gastos): Boolean {
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