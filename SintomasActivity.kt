package com.example.myappardetector

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class SintomasActivity : AppCompatActivity() {
    private var gradoDolor: Int = 0
    private var rigidezMatutina: Int = 0
    private var factorR: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sintomas)
        // Obtener el ImageView donde se mostrará la imagen
        val imageView: ImageView = findViewById(R.id.imageView)
        // Obtener la ruta de la imagen pasada en el Intent (si existe)
        val imagePath = intent.getStringExtra("image_path")
        imagePath?.let {
            // Convertir la ruta del archivo en un Bitmap
            val imageFile = File(it)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                imageView.setImageBitmap(bitmap)  // Mostrar la imagen en el ImageView
            }
        }
        // Referencias a los SeekBars y EditText
        val seekBarDolor: SeekBar = findViewById(R.id.seekBarDolor)
        val seekBarRigidez: SeekBar = findViewById(R.id.seekBarRigidez)
        val factorREditText: EditText = findViewById(R.id.factorREditText)
        val btnContinuar: Button = findViewById(R.id.btnContinuar)
        // Referencias a los TextViews para mostrar los valores
        val tvDolorValue: TextView = findViewById(R.id.tvDolorValue)
        val tvRigidezValue: TextView = findViewById(R.id.tvRigidezValue)
        // Configurar el SeekBar para el grado de dolor
        seekBarDolor.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                gradoDolor = progress
                tvDolorValue.text = gradoDolor.toString()  // Actualizar el valor mostrado
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        // Configurar el SeekBar para la rigidez matutina
        seekBarRigidez.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                rigidezMatutina = progress
                tvRigidezValue.text = rigidezMatutina.toString()  // Actualizar el valor mostrado
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        // Configurar el botón "Continuar"
        btnContinuar.setOnClickListener {
            // Obtener el factor R desde el EditText
            factorR = factorREditText.text.toString().toIntOrNull() ?: 0
            // Crear un intent para pasar los datos
            val intent = Intent(this, PrediccionActivity::class.java)
            intent.putExtra("image_path", imagePath)  // Pasar la ruta de la imagen
            intent.putExtra("gradoDolor", gradoDolor)         // Pasar grado de dolor
            intent.putExtra("rigidezMatutina", rigidezMatutina) // Pasar rigidez matutina
            intent.putExtra("factorR", factorR)               // Pasar factor R
            startActivity(intent)
        }
    }
}
