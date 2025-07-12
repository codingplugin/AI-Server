package com.example.ai_share

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.net.Uri
import android.graphics.Bitmap
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import android.content.Context

class PredictionGroupAdapter(
    private val groups: List<PredictionGroup>
) : RecyclerView.Adapter<PredictionGroupAdapter.GroupViewHolder>() {

    class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val personName: TextView = view.findViewById(R.id.personName)
        val imagesRecyclerView: RecyclerView = view.findViewById(R.id.imagesRecyclerView)
        val shareButton: View = view.findViewById(R.id.shareButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.group_person, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.personName.text = group.personName
        holder.imagesRecyclerView.layoutManager =
            LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.imagesRecyclerView.adapter = AnnotatedImageAdapter(group.images)
        holder.shareButton.setOnClickListener {
            val uris = group.images.map { it.originalUri }
            val context = holder.itemView.context
            val zipFile = File(context.cacheDir, "${group.personName}_photos.zip")
            createZipFromUris(context, uris, zipFile)

            val zipUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, zipUri)
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share zip"))
        }
    }

    override fun getItemCount() = groups.size

    fun createZipFromUris(context: Context, uris: List<Uri>, zipFile: File) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            for (uri in uris) {
                var fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "image.jpg"
                // Ensure the file has a valid image extension
                if (!(fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) || fileName.endsWith(".png", true))) {
                    fileName += ".jpg"
                }
                zos.putNextEntry(ZipEntry(fileName))
                context.contentResolver.openInputStream(uri)?.use { input ->
                    input.copyTo(zos)
                }
                zos.closeEntry()
            }
        }
    }
} 