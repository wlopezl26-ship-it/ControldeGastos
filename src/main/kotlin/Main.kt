package edu.umg.programacion2

import edu.umg.programacion2.edu.umg.programacion2.gastos.Datos.gastoRepositoryEnMemoria
import edu.umg.programacion2.edu.umg.programacion2.Ingresos.Datos.ingresoRepositoryEnMemoria
import edu.umg.programacion2.edu.umg.programacion2.gastos.modelo.categoria
import edu.umg.programacion2.edu.umg.programacion2.Ingresos.Modelo.categoriaIngresos
import edu.umg.programacion2.edu.umg.programacion2.Servicios.banguatService
import edu.umg.programacion2.edu.umg.programacion2.Servicios.controlGastos
import kotlinx.coroutines.runBlocking


fun main() = runBlocking  {

    val repoGastos = gastoRepositoryEnMemoria()
    val repoIngresos = ingresoRepositoryEnMemoria()
    val control = controlGastos(repoGastos, repoIngresos)

    println("=== Prueba de libreria gastos core===")

    val tasaUSD = banguatService.obtenerTipoCambioDia()
    println("El tipo de cambio Obtenida del BanGuat oficial es de: Q. $tasaUSD")

    val errIngreso = control.registrarIngreso("Sueldo", 4000.0, categoriaIngresos.SALARIO, "20/10/2026")
    val errGasto = control.registrarGasto("Almuerzo", 50.0, categoria.ALIMENTACION, "20/10/2026")

    if (errIngreso != null) println("Error en ingreso: $errIngreso")
    if (errGasto != null) println("Error en gasto: $errGasto")

    println(control.resumen())

    }
