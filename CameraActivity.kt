package com.example.myappardetector

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CameraActivity : AppCompatActivity() {
    private var capturedImage: Bitmap? = null
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            capturedImage = imageBitmap  // Guardar la imagen en la variable

            // Guardar la imagen como un archivo JPG en el almacenamiento temporal
            capturedImage?.let {
                val savedImageFile = saveImageAsJpeg(it)  // Guardar el archivo
                // Luego de guardar la imagen, abrir la siguiente actividad
                val intent = Intent(this, SintomasActivity::class.java)
                intent.putExtra("image_path", savedImageFile.absolutePath)  // Pasar la ruta del archivo
                startActivity(intent)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            openCamera()
        }
    }
    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            takePictureLauncher.launch(takePictureIntent)
        }
    }
    // Función para guardar la imagen como JPG en el almacenamiento temporal
    private fun saveImageAsJpeg(bitmap: Bitmap): File {
        // Obtener el directorio de caché de la aplicación
        val cacheDir = cacheDir
        // Crear un archivo temporal con extensión .jpg
        val tempFile = File(cacheDir, "temp_image.jpg")
        try {
            // Abrir un flujo de salida para escribir el archivo
            val outputStream = FileOutputStream(tempFile)
            // Comprimir la imagen como JPG (calidad de 100, máxima calidad)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return tempFile // Devolver el archivo
    }
}
