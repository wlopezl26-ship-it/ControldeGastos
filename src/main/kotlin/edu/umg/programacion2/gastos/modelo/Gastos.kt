package edu.umg.programacion2.edu.umg.programacion2.gastos.modelo


data class Gastos (
    val id: Int = 0,
    val descripcion: String,
    val monto: Double,
    val fecha: String,
    val categoria: Categoria,
)