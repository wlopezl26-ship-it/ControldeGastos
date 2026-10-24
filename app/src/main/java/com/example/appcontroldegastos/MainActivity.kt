package com.example.appcontroldegastos

import com.example.appcontroldegastos.ui.theme.AppDatabase
import com.example.appcontroldegastos.ui.theme.TransaccionDao
import com.example.appcontroldegastos.ui.theme.TransaccionEntity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import edu.umg.programacion2.edu.umg.programacion2.Ingresos.Datos.ingresoRepositoryEnMemoria
import edu.umg.programacion2.edu.umg.programacion2.Ingresos.Modelo.categoriaIngresos
import edu.umg.programacion2.edu.umg.programacion2.Servicios.banguatService
import edu.umg.programacion2.edu.umg.programacion2.Servicios.controlGastos
import edu.umg.programacion2.edu.umg.programacion2.gastos.Datos.gastoRepositoryEnMemoria
import edu.umg.programacion2.edu.umg.programacion2.gastos.modelo.categoria
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainActivity : ComponentActivity() {

    private val repoGastos = gastoRepositoryEnMemoria()
    private val repoIngresos = ingresoRepositoryEnMemoria()
    private val control = controlGastos(repoGastos, repoIngresos)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.obtenerBaseDeDatos(applicationContext)
        val dao = db.transaccionDao()

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AppNavigator(control = control, dao = dao)
            }
        }
    }
}

enum class Pantalla { ONBOARDING, PRINCIPAL, RESUMEN }

@Composable
fun AppNavigator(control: controlGastos, dao: TransaccionDao) {
    val context = LocalContext.current

    val prefs = remember { context.getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE) }

    val yaRegistrado = remember { prefs.getBoolean("registrado", false) }

    var pantallaActual by remember {
        mutableStateOf(if (yaRegistrado) Pantalla.PRINCIPAL else Pantalla.ONBOARDING)
    }

    var nombre by remember { mutableStateOf(prefs.getString("nombre", "") ?: "") }
    var edad by remember { mutableStateOf(prefs.getString("edad", "") ?: "") }
    var gmail by remember { mutableStateOf(prefs.getString("gmail", "") ?: "") }
    var telefono by remember { mutableStateOf(prefs.getString("telefono", "") ?: "") }
    var resumenText by remember { mutableStateOf("Cargando saldo...") }

    var listaHistorial by remember { mutableStateOf<List<TransaccionEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val guardados = dao.obtenerTodas()
            listaHistorial = guardados
            for (t in guardados) {
                if (t.tipo == "GASTO") {
                    control.registrarGasto(t.descripcion, t.monto, categoria.OTROS, t.fecha)
                } else {
                    control.registrarIngreso(t.descripcion, t.monto, categoriaIngresos.OTROS, t.fecha)
                }
            }
            resumenText = control.resumen()
        }
    }

    fun recargarHistorial() {
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            val guardados = dao.obtenerTodas()
            withContext(Dispatchers.Main) {
                listaHistorial = guardados
            }
        }
    }

    when (pantallaActual) {
        Pantalla.ONBOARDING -> OnboardingScreen(
            nombre = nombre, onNombreChange = { nombre = it },
            edad = edad, onEdadChange = { edad = it },
            gmail = gmail, onGmailChange = { gmail = it },
            telefono = telefono, onTelefonoChange = { telefono = it },
            onComenzar = {
                if (nombre.isNotBlank()) {
                    prefs.edit()
                        .putBoolean("registrado", true)
                        .putString("nombre", nombre)
                        .putString("edad", edad)
                        .putString("gmail", gmail)
                        .putString("telefono", telefono)
                        .apply()
                    pantallaActual = Pantalla.PRINCIPAL
                }
            }
        )
        Pantalla.PRINCIPAL -> ControlGastosScreen(
            nombreUsuario = nombre,
            control = control,
            dao = dao,
            resumenActual = resumenText,
            onActualizarResumen = { nuevoResumen ->
                resumenText = nuevoResumen
                recargarHistorial()
            },
            irAMovimientos = { pantallaActual = Pantalla.RESUMEN }
        )
        Pantalla.RESUMEN -> SummaryScreen(
            nombreUsuario = nombre,
            gmail = gmail,
            telefono = telefono,
            edad = edad,
            resumenText = resumenText,
            historial = listaHistorial,
            dao = dao,
            onCambioRealizado = { recargarHistorial() },
            volver = { pantallaActual = Pantalla.PRINCIPAL }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlGastosScreen(
    nombreUsuario: String,
    control: controlGastos,
    dao: TransaccionDao,
    resumenActual: String,
    onActualizarResumen: (String) -> Unit,
    irAMovimientos: () -> Unit
) {
    var tipoAccion by remember { mutableStateOf("GASTO") }
    var descripcion by remember { mutableStateOf("") }
    var montoStr by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var mensajeEstado by remember { mutableStateOf<String?>(null) }
    var esDolares by remember { mutableStateOf(false) }

    val categoriasGasto = categoria.values()
    val categoriasIngreso = categoriaIngresos.values()

    var catGastoSeleccionada by remember { mutableStateOf(categoriasGasto.firstOrNull() ?: categoria.OTROS) }
    var catIngresoSeleccionada by remember { mutableStateOf(categoriasIngreso.firstOrNull() ?: categoriaIngresos.OTROS) }

    var expandedGasto by remember { mutableStateOf(false) }
    var expandedIngreso by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    fun mostrarCalendario() {
        val calendario = Calendar.getInstance()
        val anio = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        android.app.DatePickerDialog(context, { _, yearSelected, monthOfYear, dayOfMonth ->
            val diaStr = String.format(java.util.Locale.US, "%02d", dayOfMonth)
            val mesStr = String.format(java.util.Locale.US, "%02d", monthOfYear + 1)
            fecha = "$diaStr/$mesStr/$yearSelected".trim()
        }, anio, mes, dia).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "¡Hola, $nombreUsuario! ",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "   Monto Disponible y Resumen:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = resumenActual,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // SELECTOR DE MONEDA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (esDolares) "Moneda: Dólares ($)" else "Moneda: Quetzales (Q)",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = esDolares,
                onCheckedChange = { esDolares = it }
            )
        }

        // SECCIONES (GASTO E INGRESO)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { tipoAccion = "GASTO" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tipoAccion == "GASTO") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    "Registrar Gasto",
                    color = if (tipoAccion == "GASTO") MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { tipoAccion = "INGRESO" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tipoAccion == "INGRESO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    "Registrar Ingreso",
                    color = if (tipoAccion == "INGRESO") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }


        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = montoStr,
            onValueChange = { montoStr = it },
            label = { Text(if (esDolares) "Monto ($ USD)" else "Monto (Q GTQ)") },
            modifier = Modifier.fillMaxWidth()
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = fecha,
                onValueChange = { },
                readOnly = true,
                label = { Text("Fecha (Toca para seleccionar)") },
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { mostrarCalendario() }
            )
        }


        if (tipoAccion == "GASTO") {
            ExposedDropdownMenuBox(
                expanded = expandedGasto,
                onExpandedChange = { expandedGasto = !expandedGasto }
            ) {
                OutlinedTextField(
                    value = catGastoSeleccionada.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría de Gasto") },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedGasto,
                    onDismissRequest = { expandedGasto = false }
                ) {
                    categoriasGasto.forEach { itemCat ->
                        DropdownMenuItem(
                            text = { Text(itemCat.name) },
                            onClick = {
                                catGastoSeleccionada = itemCat
                                expandedGasto = false
                            }
                        )
                    }
                }
            }
        } else {
            ExposedDropdownMenuBox(
                expanded = expandedIngreso,
                onExpandedChange = { expandedIngreso = !expandedIngreso }
            ) {
                OutlinedTextField(
                    value = catIngresoSeleccionada.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría de Ingreso") },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedIngreso,
                    onDismissRequest = { expandedIngreso = false }
                ) {
                    categoriasIngreso.forEach { itemCat ->
                        DropdownMenuItem(
                            text = { Text(itemCat.name) },
                            onClick = {
                                catIngresoSeleccionada = itemCat
                                expandedIngreso = false
                            }
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                val fechaLimpia = fecha.trim()
                if (fechaLimpia.isBlank()) {
                    mensajeEstado = "Por favor selecciona una fecha"
                    return@Button
                }

                val montoIngresado = montoStr.toDoubleOrNull() ?: 0.0
                if (montoIngresado <= 0.0) {
                    mensajeEstado = "Por favor ingresa un monto válido"
                    return@Button
                }

                coroutineScope.launch {
                    val montoFinal = if (esDolares) {
                        val tipoCambio = withContext(Dispatchers.IO) {
                            try { banguatService.obtenerTipoCambioDia() } catch (e: Exception) { 7.80 }
                        }
                        montoIngresado * (tipoCambio ?: 7.80)
                    } else {
                        montoIngresado
                    }

                    val esGasto = (tipoAccion == "GASTO")
                    val error = if (esGasto) {
                        control.registrarGasto(descripcion, montoFinal, catGastoSeleccionada, fechaLimpia)
                    } else {
                        control.registrarIngreso(descripcion, montoFinal, catIngresoSeleccionada, fechaLimpia)
                    }

                    if (error != null) {
                        mensajeEstado = error
                    } else {
                        withContext(Dispatchers.IO) {
                            dao.insertar(
                                TransaccionEntity(
                                    descripcion = descripcion,
                                    monto = montoFinal,
                                    tipo = tipoAccion,
                                    fecha = fechaLimpia
                                )
                            )
                        }

                        mensajeEstado = if (esGasto) "¡Gasto registrado con éxito!" else "¡Ingreso registrado con éxito!"
                        onActualizarResumen(withContext(Dispatchers.IO) { control.resumen() })
                        descripcion = ""
                        montoStr = ""
                        fecha = ""
                        esDolares = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (tipoAccion == "GASTO") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(if (tipoAccion == "GASTO") "Confirmar Gasto" else "Confirmar Ingreso")
        }

        if (mensajeEstado != null) {
            Text(
                text = mensajeEstado!!,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // BOTÓN INFERIOR DE MOVIMIENTOS
        OutlinedButton(
            onClick = irAMovimientos,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(" Ver Historial de Movimientos y Perfil")
        }
    }
}

// --- PANTALLA DE RESUMEN CON HISTORIAL DE TRANSACCIONES ---
@Composable
fun SummaryScreen(
    nombreUsuario: String,
    gmail: String,
    telefono: String,
    edad: String,
    resumenText: String,
    historial: List<TransaccionEntity>,
    dao: TransaccionDao,
    onCambioRealizado: () -> Unit,
    volver: () -> Unit
) {

    var selectedId by remember { mutableStateOf<Long?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Resumen y Perfil",
            style = MaterialTheme.typography.headlineMedium
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Información de Usuario", style = MaterialTheme.typography.titleMedium)
                HorizontalDivider()
                Text("Nombre: $nombreUsuario")
                Text("Edad: $edad años")
                Text("Correo: $gmail")
                Text("Teléfono: $telefono")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Resumen Financiero Consolidado", style = MaterialTheme.typography.titleMedium)
                HorizontalDivider()
                Text(resumenText, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Text("      Historial de Transacciones", style = MaterialTheme.typography.titleLarge)

        if (historial.isEmpty()) {
            Text(
                text = "No hay transacciones registradas aún.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        } else {
            historial.forEach { item ->
                val isSelected = selectedId == item.id
                val esGasto = item.tipo == "GASTO"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            // Selecciona o deselecciona al hacer clic
                            selectedId = if (isSelected) null else item.id
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (esGasto) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    // Usamos Column para contener la fila principal y el botón de eliminar abajo si se selecciona
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.descripcion,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Fecha: ${item.fecha}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Text(
                                text = if (esGasto) "- Q. ${String.format(java.util.Locale.US, "%.2f", item.monto)}"
                                else "+ Q. ${String.format(java.util.Locale.US, "%.2f", item.monto)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (esGasto) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }

                        if (isSelected) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error // Color rojo de alerta
                                    ),
                                    onClick = {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            dao.eliminarPorId(item.id)
                                            withContext(Dispatchers.Main) {
                                                onCambioRealizado() // Actualiza la lista en la UI
                                                selectedId = null   // Cierra la selección
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Borrar"
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Eliminar registro")
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = volver, modifier = Modifier.fillMaxWidth()) {
            Text("Regresar al Panel Principal")
        }
    }
}

@Composable
fun OnboardingScreen(
    nombre: String,
    onNombreChange: (String) -> Unit,
    edad: String,
    onEdadChange: (String) -> Unit,
    gmail: String,
    onGmailChange: (String) -> Unit,
    telefono: String,
    onTelefonoChange: (String) -> Unit,
    onComenzar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally // <-- Esto centra todo horizontalmente
    ) {
        Text(
            text = "Registro de Usuario",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = onNombreChange,
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = edad,
            onValueChange = onEdadChange,
            label = { Text("Edad") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = gmail,
            onValueChange = onGmailChange,
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = telefono,
            onValueChange = onTelefonoChange,
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onComenzar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Comenzar")
        }
    }
}