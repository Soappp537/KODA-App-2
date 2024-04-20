package com.example.kodaapplication

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import org.tensorflow.lite.Interpreter


class pageForWebFiltering : AppCompatActivity() {
    private lateinit var filterText: EditText
    private lateinit var filterButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_page_for_web_filtering)

        filterText = findViewById(R.id.filter_text)
        filterButton = findViewById(R.id.filter_button)
        filterButton.setOnClickListener {
            val wordToFilter = filterText.text.toString()
            try {
                val filteredWord = filterQueryWithTFLite(wordToFilter)
                // Apply the filtered word logic here
            } catch (e: Exception) {
                // Handle any exceptions here, such as logging the error
                Log.e("FilteringError", "Error filtering word: ${e.message}")
                // You might also want to show a toast or dialog to inform the user
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun filterQueryWithTFLite(query: String): String {
        // Load your TensorFlow Lite model here
        val interpreter = Interpreter(loadModelFile())

        // Prepare the input data for the model
        val input = Array(1) { FloatArray(1) }
        input[0][0] = query.length.toFloat()

            // Prepare the output data for the model
            val OUTPUT_SIZE = 2 // Your output size here
            val output = Array(1) { FloatArray(OUTPUT_SIZE) }

            // Run the inference
            interpreter.run(input, output)

            // Process the output to filter the query
            val result = output[0][0].toInt()
            return if (result == 1) {
                // Filtered word
                "Filtered"
            } else {
                // Not filtered
                query
            }
        }

        private fun loadModelFile(): MappedByteBuffer {
            val assetManager = assets
            val fileDescriptor = assetManager.openFd("FinalModel.tflite")
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }

}