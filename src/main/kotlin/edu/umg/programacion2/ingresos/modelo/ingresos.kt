package edu.umg.programacion2.edu.umg.programacion2.Ingresos.Modelo


data class ingresos (
    val id: Int = 0,
    val descripcion: String,
    val monto: Double,
    val fecha: String,
    val categoria: categoriaIngresos,

    )