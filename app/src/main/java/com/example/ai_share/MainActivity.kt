package com.example.ai_share

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnTrain = findViewById<Button>(R.id.buttonTrain)
        val btnPredict = findViewById<Button>(R.id.buttonPredict)
        val btnSavedModels = findViewById<Button>(R.id.buttonSavedModels)

        btnTrain.setOnClickListener {
            startActivity(Intent(this@MainActivity, TrainActivity::class.java))
        }
        btnPredict.setOnClickListener {
            startActivity(Intent(this@MainActivity, PredictActivity::class.java))
        }
        btnSavedModels.setOnClickListener {
            startActivity(Intent(this@MainActivity, SavedModelsActivity::class.java))
        }
    }
} 