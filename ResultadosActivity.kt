package com.example.myappardetector

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class ResultadosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados)
        // Obtener los resultados pasados desde PrediccionActivity
        val enrojecimiento = intent.getFloatExtra("enrojecimiento", 0f)
        val inflamacion = intent.getFloatExtra("inflamacion", 0f)
        val deformidad = intent.getFloatExtra("deformidad", 0f)
        val dolor = intent.getIntExtra("dolor", 0)
        val rigidez = intent.getIntExtra("rigidez", 0)
        val fr = intent.getIntExtra("fr", 0)
        // Cargar el archivo .tflite desde los assets
        val assetManager = assets
        val fileDescriptor = assetManager.openFd("modelo_Probabilidad.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        // Crear un ByteBuffer para cargar el archivo
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.length
        val modelByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        // Crear el Interpreter con el ByteBuffer
        val tflite = Interpreter(modelByteBuffer)
        // Ejecutar la predicción
        val input = ByteBuffer.allocateDirect(6 * 4)
        input.order(ByteOrder.nativeOrder())
        input.putFloat(dolor.toFloat())
        input.putFloat(rigidez.toFloat())
        input.putFloat(fr.toFloat())
        input.putFloat(enrojecimiento)
        input.putFloat(inflamacion)
        input.putFloat(deformidad)
        // Realizar la predicción
        val output = Array(1) { FloatArray(1) }
        tflite.run(input, output)
        // Obtener el valor de probabilidad
        val probabilidad = output[0][0]
        // Mostrar los resultados en los TextViews correspondientes
        val enrojecimientoTextView: TextView = findViewById(R.id.enrojecimientoResult)
        val inflamacionTextView: TextView = findViewById(R.id.inflamacionResult)
        val deformidadTextView: TextView = findViewById(R.id.deformidadResult)
        val dolorTextView: TextView = findViewById(R.id.dolorResult)
        val rigidezTextView: TextView = findViewById(R.id.rigidezResult)
        val frTextView: TextView = findViewById(R.id.frResult)
        val resultadoTextView: TextView = findViewById(R.id.resultadoTextView)
        // Formatear los resultados a 2 decimales y mostrar
        enrojecimientoTextView.text = "Enrojecimiento: ${String.format("%.2f", enrojecimiento)}"
        inflamacionTextView.text = "Inflamación: ${String.format("%.2f", inflamacion)}"
        deformidadTextView.text = "Deformidad: ${String.format("%.2f", deformidad)}"
        // Mostrar valores de Dolor, Rigidez y FR
        dolorTextView.text = "Dolor: $dolor"
        rigidezTextView.text = "Rigidez: $rigidez"
        frTextView.text = "Factor Reumatoideo (FR): $fr"
        // Mostrar la probabilidad de la predicción
        resultadoTextView.text = "Probabilidad de AR: ${String.format("%.2f", probabilidad*100)}%"
        // Configurar el botón Fin
        val finButton: Button = findViewById(R.id.finButton)
        finButton.setOnClickListener {
            // Navegar a la MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // Finaliza la actividad actual para que no regrese al presionar "atrás"
        }
    }
}
