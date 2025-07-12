package com.example.ai_share

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class AnnotatedImageAdapter(
    private val images: List<AnnotatedImage>
) : RecyclerView.Adapter<AnnotatedImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.annotatedImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_annotated_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val bitmap = images[position].imageBitmap
        holder.imageView.setImageBitmap(bitmap)
        holder.imageView.setOnClickListener {
            val context = holder.itemView.context
            // Save bitmap to a temp file
            val tempFile = File(context.cacheDir, "annotated_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            // Pass the file path to the activity
            val intent = Intent(context, FullScreenImageActivity::class.java)
            intent.putExtra("image_path", tempFile.absolutePath)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = images.size
} 