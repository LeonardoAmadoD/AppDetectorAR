package com.example.myappardetector

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.round

class PrediccionActivity : AppCompatActivity() {
    private lateinit var model: Interpreter
    private lateinit var imageBitmap: Bitmap
    private var gradoDolor: Int = 0
    private var rigidezMatutina: Int = 0
    private var factorR: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediccion)
        // Obtener la ruta de la imagen pasada desde SintomasActivity
        val imagePath = intent.getStringExtra("image_path")
        // Cargar la imagen desde la ruta
        imagePath?.let {
            val imageFile = File(it)
            if (imageFile.exists()) {
                imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            }
        }
        // Obtener los valores de gradoDolor, rigidezMatutina y factorR
        gradoDolor = intent.getIntExtra("gradoDolor", 0)
        rigidezMatutina = intent.getIntExtra("rigidezMatutina", 0)
        factorR = intent.getIntExtra("factorR", 0)
        // Asegurarse de que la imagen tenga el tamaño correcto para el modelo
        imageBitmap = resizeImage(imageBitmap, 240, 240)
        // Cargar el modelo preentrenado
        model = Interpreter(loadModelFile())
        // Botón para ejecutar la predicción
        val btnPrediccion: Button = findViewById(R.id.btnPrediccion)
        btnPrediccion.setOnClickListener {
            val prediction = runInference(imageBitmap)
            // Asignar los resultados de la predicción a variables, multiplicar por 10 y redondear
            val enrojecimiento = round(prediction[0] * 10)
            val inflamacion = round(prediction[1] * 10)
            val deformidad = round(prediction[2] * 10)
            // Pasar los resultados a ResultadosActivity
            val intent = Intent(this, ResultadosActivity::class.java)
            intent.putExtra("enrojecimiento", enrojecimiento)
            intent.putExtra("inflamacion", inflamacion)
            intent.putExtra("deformidad", deformidad)
            intent.putExtra("dolor", gradoDolor)
            intent.putExtra("rigidez", rigidezMatutina)
            intent.putExtra("fr", factorR)
            startActivity(intent)
        }
    }
    // Función para cargar el modelo TensorFlow Lite
    private fun loadModelFile(): ByteBuffer {
        val fileDescriptor = assets.openFd("modelo_Reconocimiento.tflite")
        val inputStream = fileDescriptor.createInputStream()
        val modelBytes = inputStream.readBytes()
        val byteBuffer = ByteBuffer.allocateDirect(modelBytes.size)
        byteBuffer.order(ByteOrder.nativeOrder())
        byteBuffer.put(modelBytes)
        return byteBuffer
    }
    // Función para redimensionar la imagen a las dimensiones necesarias para el modelo
    private fun resizeImage(image: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(image, width, height, false)
    }
    // Función para hacer la predicción con la imagen
    private fun runInference(image: Bitmap): FloatArray {
        val inputSize = 240
        val inputArray = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        inputArray.order(ByteOrder.nativeOrder())
        // Convertir la imagen a ByteBuffer
        val intValues = IntArray(inputSize * inputSize)
        image.getPixels(intValues, 0, image.width, 0, 0, inputSize, inputSize)
        inputArray.rewind()
        for (pixel in intValues) {
            inputArray.putFloat(((pixel shr 16) and 0xFF) / 255.0f)  // Red
            inputArray.putFloat(((pixel shr 8) and 0xFF) / 255.0f)   // Green
            inputArray.putFloat(((pixel) and 0xFF) / 255.0f)         // Blue
        }
        // Ejecutar la predicción
        val output = Array(1) { FloatArray(5) }  // Array para las 5 clases de salida
        model.run(inputArray, output)
        // Devolver la predicción de las 3 salidas (enrojecimiento, inflamación, deformidad)
        return floatArrayOf(output[0][0], output[0][1], output[0][2])
    }
}