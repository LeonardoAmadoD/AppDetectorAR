package com.example.myappardetector

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PredictActivity : AppCompatActivity() {

    private lateinit var tflite: Interpreter
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predict)

        val modelFile = assets.open("modelo_Reconocimiento.tflite").use { inputStream ->
            val bytes = inputStream.readBytes()
            ByteBuffer.allocateDirect(bytes.size).apply {
                order(ByteOrder.nativeOrder())
                put(bytes)
            }
        }

        tflite = Interpreter(modelFile)

        // Suponiendo que la imagen ya está procesada en 'bitmap'
        bitmap = intent.getParcelableExtra("capturedImage")!!

        val predictionResults = predict(bitmap)

        // Mostrar resultados
        val enrojecimientoTextView: TextView = findViewById(R.id.resultadoEnrojecimiento)
        val inflamacionTextView: TextView = findViewById(R.id.resultadoInflamacion)
        val deformidadTextView: TextView = findViewById(R.id.resultadoDeformidad)

        enrojecimientoTextView.text = "Enrojecimiento: ${predictionResults[0]}"
        inflamacionTextView.text = "Inflamación: ${predictionResults[1]}"
        deformidadTextView.text = "Deformidad: ${predictionResults[2]}"
    }

    private fun predict(image: Bitmap): FloatArray {
        // Preparar entrada
        val inputBuffer = ByteBuffer.allocateDirect(1 * 240 * 240 * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        val intValues = IntArray(240 * 240)
        image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)

        var pixelIndex = 0
        for (y in 0 until 240) {
            for (x in 0 until 240) {
                val pixelValue = intValues[pixelIndex++]

                // Normalización del rango [0, 255] a [0, 1]
                inputBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f)
                inputBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)
                inputBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
            }
        }

        // Crear TensorBuffer para salidas
        val outputEnrojecimiento = TensorBuffer.createFixedSize(intArrayOf(1, 5), org.tensorflow.lite.DataType.FLOAT32)
        val outputInflamacion = TensorBuffer.createFixedSize(intArrayOf(1, 5), org.tensorflow.lite.DataType.FLOAT32)
        val outputDeformidad = TensorBuffer.createFixedSize(intArrayOf(1, 5), org.tensorflow.lite.DataType.FLOAT32)

        // Ejecutar modelo
        tflite.runForMultipleInputsOutputs(
            arrayOf(inputBuffer),
            mapOf(
                0 to outputEnrojecimiento.buffer,
                1 to outputInflamacion.buffer,
                2 to outputDeformidad.buffer
            )
        )

        // Obtener los valores de predicción
        val enrojecimientoPrediction = outputEnrojecimiento.floatArray
        val inflamacionPrediction = outputInflamacion.floatArray
        val deformidadPrediction = outputDeformidad.floatArray

        return floatArrayOf(
            enrojecimientoPrediction.indexOfMax(),
            inflamacionPrediction.indexOfMax(),
            deformidadPrediction.indexOfMax()
        )
    }

    // Extensión para obtener el índice del valor máximo
    private fun FloatArray.indexOfMax(): Float {
        return this.indexOfFirst { it == this.maxOrNull()!! }.toFloat()
    }
}
