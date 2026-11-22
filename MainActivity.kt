package com.piu.wasteclassifier

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val pickImage = 101
    private val labels = arrayOf("Batteries", "Clothes / Textiles", "Electronic Waste", "Glass",
        "Light Bulbs", "Metal", "Organic Waste", "Paper", "Plastic")
    private var selectedBitmap: Bitmap? = null
    private var interpreter: Interpreter? = null
    private lateinit var preview: ImageView
    private lateinit var result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val afd = assets.openFd("waste_classifier.tflite")
            val input = FileInputStream(afd.fileDescriptor)
            val mapped = input.channel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.length)
            interpreter = Interpreter(mapped)
            input.close()
            afd.close()
        } catch (_: Exception) { interpreter = null }

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 28)
            setBackgroundColor(0xFFF5F8F6.toInt())
        }
        val title = TextView(this).apply {
            text = "PIU WASTE CLASSIFIER"
            textSize = 23f; setTextColor(0xFF126344.toInt()); gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "AI-powered waste identification"
            textSize = 14f; gravity = Gravity.CENTER; setPadding(0, 8, 0, 24)
        }
        preview = ImageView(this).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(0xFFE3ECE7.toInt())
            layoutParams = LinearLayout.LayoutParams(-1, 260)
        }
        val choose = Button(this).apply {
            text = "Choose Waste Photo"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
                startActivityForResult(Intent.createChooser(intent, "Select waste image"), pickImage)
            }
        }
        val classify = Button(this).apply {
            text = "Identify Waste"
            setOnClickListener { classifySelected() }
        }
        result = TextView(this).apply {
            textSize = 16f; setTextColor(0xFF26352D.toInt()); setPadding(4, 18, 4, 12)
            text = if (interpreter == null)
                "AI model not installed yet. Add your trained waste_classifier.tflite file to app/src/main/assets."
            else "Select a photo, then tap Identify Waste."
        }
        page.addView(title)
        page.addView(subtitle)
        page.addView(preview)
        page.addView(choose)
        page.addView(classify)
        page.addView(result)
        setContentView(ScrollView(this).apply { addView(page) })
    }

    @Deprecated("Use Activity Result API in a future update")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImage && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                contentResolver.openInputStream(uri)?.use { stream ->
                    selectedBitmap = BitmapFactory.decodeStream(stream)
                    preview.setImageBitmap(selectedBitmap)
                    result.text = "Photo selected. Tap Identify Waste."
                }
            }
        }
    }

    private fun classifySelected() {
        val bitmap = selectedBitmap
        val model = interpreter
        if (bitmap == null) { result.text = "Please choose a photo first."; return }
        if (model == null) {
            result.text = "Model file missing. Your original APP.py needs its trained model converted to TensorFlow Lite (.tflite) and added to app/src/main/assets/waste_classifier.tflite."
            return
        }
        try {
            val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
            val input = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4).order(ByteOrder.nativeOrder())
            for (y in 0 until 224) for (x in 0 until 224) {
                val pixel = resized.getPixel(x, y)
                input.putFloat(((pixel shr 16 and 0xFF) / 255.0f))
                input.putFloat(((pixel shr 8 and 0xFF) / 255.0f))
                input.putFloat(((pixel and 0xFF) / 255.0f))
            }
            input.rewind()
            val output = Array(1) { FloatArray(9) }
            model.run(input, output)
            val best = output[0].indices.maxByOrNull { output[0][it] } ?: 0
            val confidence = output[0][best] * 100f
            val advice = when (best) {
                0 -> "Protect the terminals and use an authorised battery collection point."
                1 -> "Reuse or donate wearable clothes; send damaged textiles to a textile recycler."
                2 -> "Use an authorised e-waste collection centre. Never burn electronic parts."
                3 -> "Clean and sort glass; follow local colour-sorting rules."
                4 -> "Keep bulbs unbroken and use a suitable lamp collection point."
                5 -> "Empty and rinse metal containers; separate attached materials when practical."
                6 -> "Keep food and plant waste free from plastic; consider composting."
                7 -> "Keep paper dry and remove food, films and other contaminants."
                else -> "Check the resin code, empty and rinse the item, and follow local recycling rules."
            }
            result.text = "Predicted category: ${labels[best]}\nConfidence: ${String.format(Locale.US, "%.1f", confidence)}%\n\nAdvice: $advice\n\nNote: A photo prediction cannot confirm the exact polymer grade."
        } catch (e: Exception) {
            result.text = "Could not run the model: ${e.localizedMessage}"
        }
    }

    override fun onDestroy() {
        interpreter?.close()
        super.onDestroy()
    }
}
