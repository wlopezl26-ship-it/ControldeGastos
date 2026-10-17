package edu.umg.programacion2

//TIP Para <b>ejecutar</b> el código, pulsa <shortcut actionId="Run"/> o
// haga clic en el icono <icon src="AllIcons.Actions.Execute"/> del margen.
fun main() {
    val name = "Kotlin"
    //TIP Presione <shortcut actionId="ShowIntentionActions"/> con el cursor en el texto resaltado
    // para ver cómo IntelliJ IDEA sugiere corregirlo.
    println("Hello, " + name + "!")

    for (i in 1..5) {
        //TIP Presiona <shortcut actionId="Debug"/> para comenzar a depurar tu código. Hemos establecido un punto de interrupción <icon src="AllIcons.Debugger.Db_set_breakpoint"/>
        // para ti, pero siempre puedes añadir más pulsando <shortcut actionId="ToggleLineBreakpoint"/>.
        println("i = $i")
    }
}