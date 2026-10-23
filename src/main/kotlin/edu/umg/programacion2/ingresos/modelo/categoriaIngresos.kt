package edu.umg.programacion2.edu.umg.programacion2.Ingresos.Modelo

enum class categoriaIngresos (val nombreVisible: String) {
        SALARIO("Salario"),
        TRABAJOSEXTRA("Trabajos Extra"),
        BECA("Beca"),
        Ventas("Ventas/Negocio"),
        REINTEGRO("Reintegro/Pago de alguna deuda"),
        REGALO("Regalo/Bonificación"),
        OTROS("Otros");
        override fun toString(): String = nombreVisible
    }
