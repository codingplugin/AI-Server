package com.example.ai_share

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.ai_share.network.ApiClient
import com.example.ai_share.network.TrainResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class TrainActivity : AppCompatActivity() {
    private lateinit var editTextName: TextInputEditText
    private lateinit var buttonSelectImages: MaterialButton
    private lateinit var buttonStartTraining: MaterialButton
    private lateinit var recyclerViewImages: RecyclerView
    private lateinit var progressBar: android.widget.ProgressBar

    private var imageUris: List<Uri> = emptyList()

    private val pickImagesLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris != null) {
            imageUris = uris
            // TODO: Update recyclerViewImages adapter with selected images
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train)

        editTextName = findViewById(R.id.editTextName)
        buttonSelectImages = findViewById(R.id.buttonSelectImages)
        buttonStartTraining = findViewById(R.id.buttonStartTraining)
        recyclerViewImages = findViewById(R.id.recyclerViewImages)
        progressBar = findViewById(R.id.progressBarTraining)

        buttonSelectImages.setOnClickListener {
            pickImagesLauncher.launch(arrayOf("image/*"))
        }

        buttonStartTraining.setOnClickListener {
            val name = editTextName.text.toString().trim()
            if (name.isEmpty() || imageUris.isEmpty()) {
                Toast.makeText(this, "Please enter a name and select images", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            buttonStartTraining.isEnabled = false
            progressBar.visibility = android.widget.ProgressBar.VISIBLE
            uploadImagesToServer(name, imageUris)
        }
    }

    private fun uploadImagesToServer(name: String, uris: List<Uri>) {
        val images = mutableListOf<MultipartBody.Part>()
        for (uri in uris) {
            val file = uriToFile(uri)
            if (file != null) {
                val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                images.add(MultipartBody.Part.createFormData("images", file.name, reqFile))
            }
        }
        val nameBody = RequestBody.create("text/plain".toMediaTypeOrNull(), name)
        ApiClient.apiService.trainPerson(nameBody, images).enqueue(object : Callback<TrainResponse> {
            override fun onResponse(call: Call<TrainResponse>, response: Response<TrainResponse>) {
                val res = response.body()
                runOnUiThread {
                    buttonStartTraining.isEnabled = true
                    progressBar.visibility = android.widget.ProgressBar.GONE
                    if (res?.success == true) {
                        Toast.makeText(this@TrainActivity, "Model is saved!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@TrainActivity, "Training failed: ${res?.error}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            override fun onFailure(call: Call<TrainResponse>, t: Throwable) {
                runOnUiThread {
                    buttonStartTraining.isEnabled = true
                    progressBar.visibility = android.widget.ProgressBar.GONE
                    Toast.makeText(this@TrainActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(cacheDir, "train_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { inputStream.copyTo(it) }
            file
        } catch (e: Exception) {
            null
        }
    }
} 