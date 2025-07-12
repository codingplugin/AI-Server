package com.example.ai_share

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ai_share.network.ApiClient
import com.example.ai_share.network.PredictGroupResponse // <-- You need to define this model
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import android.util.Base64
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.widget.TextView
import android.provider.OpenableColumns

data class PredictionGroup(
    val personName: String,
    val images: List<AnnotatedImage>
)

data class AnnotatedImage(
    val imageBitmap: Bitmap,
    val originalUri: Uri // or String if you use file paths
)

class PredictActivity : AppCompatActivity() {
    private lateinit var buttonSelectImages: Button
    private lateinit var buttonPredict: Button
    private lateinit var recyclerView: RecyclerView
    private var imageUris: List<Uri> = emptyList()
    private var imageFiles: List<File> = emptyList()
    private lateinit var statusTextView: TextView
    private lateinit var uploadStatusText: TextView

    private val pickImagesLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris != null) {
            imageUris = uris
            imageFiles = uris.mapNotNull { uriToFile(it) }
            uploadStatusText.text = "${uris.size} photos uploaded"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predict)

        buttonSelectImages = findViewById(R.id.buttonSelectImages)
        buttonPredict = findViewById(R.id.buttonPredict)
        recyclerView = findViewById(R.id.predictionGroupsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Add statusTextView
        statusTextView = findViewById(R.id.statusTextView)
        statusTextView.text = ""
        uploadStatusText = findViewById(R.id.uploadStatusText)
        uploadStatusText.text = ""

        buttonSelectImages.setOnClickListener {
            pickImagesLauncher.launch(arrayOf("image/*"))
        }

        buttonPredict.setOnClickListener {
            if (imageFiles.isNotEmpty()) {
                statusTextView.text = "Predicting..."
                predictImages(imageFiles)
            }
        }
    }

    private fun predictImages(files: List<File>) {
        // Prepare multipart request for all images
        val imageParts = files.map { file ->
            val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("images", file.name, reqFile)
        }
        // Call your API (update this to match your backend)
        ApiClient.apiService.predictPersons(imageParts).enqueue(object : Callback<PredictGroupResponse> {
            override fun onResponse(call: Call<PredictGroupResponse>, response: Response<PredictGroupResponse>) {
                val res = response.body()
                if (res != null) {
                    val groups = mutableListOf<PredictionGroup>()
                    // Suppose you have a list of original image files/URIs: imageUris or imageFiles
                    // Map from image index to original Uri
                    val originalUris: List<Uri> = imageUris // or however you track them

                    // Now, when building groups:
                    for ((name, imageList) in res.results) {
                        val annotatedImages = imageList.mapIndexed { idx, base64 ->
                            val bytes = Base64.decode(base64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            // Map the index to the original Uri
                            val originalUri = originalUris.getOrNull(idx) ?: Uri.EMPTY
                            AnnotatedImage(bitmap, originalUri)
                        }
                        groups.add(PredictionGroup(name, annotatedImages))
                    }
                    runOnUiThread {
                        recyclerView.adapter = PredictionGroupAdapter(groups)
                        statusTextView.text = "Prediction complete"
                    }
                } else {
                    runOnUiThread {
                        statusTextView.text = "Prediction failed: Empty response"
                    }
                }
            }
            override fun onFailure(call: Call<PredictGroupResponse>, t: Throwable) {
                runOnUiThread {
                    statusTextView.text = "Prediction failed: ${t.message}"
                }
            }
        })
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val fileName = getFileNameFromUri(uri) ?: "predict_${System.currentTimeMillis()}"
            val file = File(cacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }
} 