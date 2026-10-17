package edu.umg.programacion2.edu.umg.programacion2.gastos.modelo

enum class Categoria(val nombreVisible: String) {
    ALIMENTACION("Alimentación"),
    TRANSPORTE("Transporte"),
    ESTUDIOS("Estudios"),
    ENTRETENIMIENTO("Entretenimiento"),
    SALUD("Salud"),
    OTROS("Otros");
    override fun toString(): String = nombreVisible
}