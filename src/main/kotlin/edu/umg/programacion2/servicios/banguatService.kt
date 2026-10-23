package edu.umg.programacion2.edu.umg.programacion2.Servicios

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object banguatService {

    private var tipoCambioGuardado: Double = 7.75

    fun obtenerTipoCambioDia(): Double {
        try {
            val url= URL("https://www.banguat.gob.gt/variables/ws/TipoCambio.aspx/TipoCambioDia")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/soap+xml; charset=utf-8")
            conn.connectTimeout=3000
            conn.readTimeout=3000

            if (conn.responseCode == 200) {
                val reader= BufferedReader(InputStreamReader(conn.inputStream))
                val respuesta=reader.readText()
                reader.close()

                val regex ="<referencia>([0-9]+)</referencia>".toRegex()
                val match=regex.find(respuesta)
                if(match!=null){
                    tipoCambioGuardado = match.groupValues[1].toDouble()
                }
            }
        }
        catch (e:Exception){
            println("Aviso BanguatService: No se pudo conectar a la al servidor, usando tasa de respaldo (\$tipoCambioGuardado)")
        }
        return tipoCambioGuardado
    }
}