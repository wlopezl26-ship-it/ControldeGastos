package edu.umg.programacion2.edu.umg.programacion2.Servicios

import edu.umg.programacion2.edu.umg.programacion2.gastos.Datos.gastoRepository
import edu.umg.programacion2.edu.umg.programacion2.Ingresos.Datos.ingresoRepository
import edu.umg.programacion2.edu.umg.programacion2.gastos.modelo.categoria
import edu.umg.programacion2.edu.umg.programacion2.Ingresos.Modelo.categoriaIngresos
import edu.umg.programacion2.edu.umg.programacion2.gastos.modelo.gastos
import edu.umg.programacion2.edu.umg.programacion2.Ingresos.Modelo.ingresos

class controlGastos(
    private val gastoRepository: gastoRepository,
    private val ingresoRepository: ingresoRepository
) {
    suspend fun registrarGasto(descripcion: String, monto: Double, categoria: categoria, fecha: String): String? {
        if (descripcion.isBlank()) return "La descripción del gasto no puede estar vacía"
        if (monto <= 0.0) return "La cantidad del gasto debe ser mayor a Q.0.00"
        

        val nuevoGasto = gastos(descripcion = descripcion, monto = monto, categoria = categoria, fecha = fecha)
        gastoRepository.agregar(nuevoGasto)
        return null
    }

    suspend fun registrarIngreso(descripcion: String, monto: Double, categoria: categoriaIngresos, fecha: String): String? {
        if (descripcion.isBlank()) return "La descripción del ingreso no puede estar vacía"
        if (monto <= 0.0) return "La cantidad del ingreso debe ser mayor a Q.0.00"


        val nuevoIngreso = ingresos(descripcion = descripcion, monto = monto, categoria = categoria, fecha = fecha)
        ingresoRepository.agregar(nuevoIngreso)
        return null
    }

    suspend fun totalGastos(): Double = gastoRepository.listar().sumOf { it.monto }
    suspend fun totalIngresos(): Double = ingresoRepository.listar().sumOf { it.monto }
    suspend fun saldoDisponible(): Double = totalIngresos() - totalGastos()

    suspend fun listarGastos(): List<gastos> = gastoRepository.listar()
    suspend fun listarIngresos(): List<ingresos> = ingresoRepository.listar()

    suspend fun buscarGastoPorId(id: Int): gastos? = gastoRepository.buscarPorId(id)
    suspend fun buscarIngresoPorId(id: Int): ingresos? = ingresoRepository.buscarPorId(id)

    suspend fun resumen(): String {
        val totalIng = totalIngresos()
        val totalGas = totalGastos()
        val saldo = saldoDisponible()
        return "Ingresos: ${formatearQuetzales(totalIng)} | Gastos: ${formatearQuetzales(totalGas)} | Saldo: ${formatearQuetzales(saldo)}"
    }

    fun formatearQuetzales(monto: Double): String = "Q. %.2f".format(monto)

}
