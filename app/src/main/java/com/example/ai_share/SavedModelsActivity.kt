package com.example.ai_share

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ai_share.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.app.AlertDialog

class SavedModelsActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val models = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_models)

        listView = findViewById(R.id.listView)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, models)
        listView.adapter = adapter
        fetchModelsFromServer()

        listView.setOnItemLongClickListener { _, _, position, _ ->
            // Use the original model file name for deletion
            val modelFileName = models[position]
            val displayName = modelFileName.removeSuffix(".pkl")
            AlertDialog.Builder(this)
                .setTitle("Delete Model")
                .setMessage("Are you sure you want to delete $displayName?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteModel(modelFileName)
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }
    }

    private fun fetchModelsFromServer() {
        ApiClient.apiService.getModels().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                val res = response.body()
                if (res != null) {
                    models.clear()
                    // Only show the name without .pkl
                    models.addAll(res.map { it.removeSuffix(".pkl") })
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@SavedModelsActivity, "Failed to load models", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Toast.makeText(this@SavedModelsActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun deleteModel(displayName: String) {
        // Add .pkl back for deletion
        val modelFileName = if (displayName.endsWith("_model")) "$displayName.pkl" else displayName
        ApiClient.apiService.deleteModel(modelFileName).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@SavedModelsActivity, "$displayName deleted", Toast.LENGTH_SHORT).show()
                    fetchModelsFromServer()
                } else {
                    Toast.makeText(this@SavedModelsActivity, "Failed to delete $displayName", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@SavedModelsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
} 