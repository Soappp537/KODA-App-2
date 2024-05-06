package com.example.kodaapplication

import android.content.Context
import android.util.Log
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.net.URLDecoder
import java.nio.channels.FileChannel
import java.util.regex.Matcher
import java.util.regex.Pattern

class TfLiteModel {

    companion object{

        // Load the TensorFlow Lite model
        fun loadModel(context: Context, modelPath: String): Interpreter {
            val fileDescriptor = context.assets.openFd(modelPath)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            val mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            val interpreter = Interpreter(mappedByteBuffer)
            fileDescriptor.close()
            return interpreter
        }

        // Function to read word_dict.json file and load word-index mappings
        fun loadWordIndexMap(context: Context, fileName: String): Map<String, Int> {
            val inputStream = context.assets.open(fileName)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val wordIndexMap = mutableMapOf<String, Int>()
            for (key in jsonObject.keys()) {
                wordIndexMap[key] = jsonObject.getInt(key)
            }
            return wordIndexMap
        }
        fun cleanUrl(url: String, maxLength: Int): IntArray {
//            // Preprocess the input text
//            val preprocessedUrl = preprocessText(url)
//            Log.d("Preprocessed url/text:", preprocessedUrl)

            // Tokenize the preprocessed text using word-index mappings
            val tokens = tokenizeText(preprocessedUrl)
            Log.d("Tokens:", tokens.joinToString(", "))

            // Pad the sequence - based on the model
            val paddedSequence = padSequence(tokens, maxLength)
            Log.d("Padded sequence shape:", "(1, $maxLength)")
            Log.d("Padded sequence:", "[[${paddedSequence.joinToString(" ")}]]")

            return paddedSequence
        }
        // Preprocessing function
        fun preprocessText(text: String): String {
            // Lowercase the text
            var processedText = text.toLowerCase()

            // Check if the text is a URL
            val urlPattern: Pattern = Pattern.compile("http\\S+")
            val urlMatcher: Matcher = urlPattern.matcher(processedText)
            if (urlMatcher.find()) {
                // Extract the URL and preprocess it
                val url = urlMatcher.group()
                processedText = preprocessUrl(url)
            } else {
                // Remove special characters and digits
                processedText = processedText.replace("[^a-zA-Z\\s]".toRegex(), "")
            }

            // Tokenize the text
            val tokens: List<String> = processedText.split("\\s+".toRegex())

            // Remove stopwords
            val stopWords: Set<String> = setOf("a", "an", "the", "is", "are", "am", "be", "been", "being", "have", "has", "had", "do", "does", "did", "will", "would", "shall", "should", "can", "could", "may", "might", "must")
            val filteredTokens: List<String> = tokens.filter { !stopWords.contains(it) }

            // Join tokens back into a string
            return filteredTokens.joinToString(" ")
        }

        // Preprocess URL function
        fun preprocessUrl(url: String): String {
            // Decode URL to handle special characters properly
            var decodedUrl = URLDecoder.decode(url, "UTF-8")

            // Extract query string from the URL
            val queryString = decodedUrl.substringAfter("?")

            // Extract keywords from the query string
            val keywordPattern: Pattern = Pattern.compile("q=([^&]*)")
            val keywordMatcher: Matcher = keywordPattern.matcher(queryString)
            val keywords = StringBuilder()
            while (keywordMatcher.find()) {
                // Extract the keywords and append them to the StringBuilder
                keywords.append(keywordMatcher.group(1)).append(" ")
            }

            // Remove any leading or trailing whitespaces
            return keywords.trim().toString()
        }

        // Tokenize text using word-index mappings
        fun tokenizeText(text: String): List<Int> {
            // Tokenize the text
            val tokens: List<String> = text.split("\\s+".toRegex())
            // Map tokens to their corresponding indices using word-index mappings
            return tokens.map { wordIndexMap[it] ?: 0 }
        }

        // Pad sequence
        fun padSequence(tokens: List<Int>, maxLength: Int): IntArray {
            // Pad the sequence
            val paddedSequence: IntArray = IntArray(maxLength) { 0 }
            val startIdx = maxLength - tokens.size.coerceAtMost(maxLength)
            tokens.take(maxLength).forEachIndexed { index, token ->
                paddedSequence[startIdx + index] = token
            }
            return paddedSequence
        }

        fun modelInference(
            preprocessedUrl: String,
            interpreter: Interpreter,
            wordIndexMap: Map<String, Int>,
            maxLength: Int,
            paddedSequence: IntArray
        ): Int {
            // Perform inference using the TensorFlow Lite model
            val inputShape = interpreter.getInputTensor(0).shape()
            val floatArray = FloatArray(paddedSequence.size) { paddedSequence[it].toFloat() }
            val inputData = Array(1) { floatArray }
            val outputData = Array(1) { FloatArray(interpreter.getOutputTensor(0).shape()[1]) }
            interpreter.run(inputData, outputData)

            // Interpret the model's output
            val output = outputData[0]
            val predictedLabelIndex = output.indexOfMaxOrNull()
            val predictedLabel = if (predictedLabelIndex == 1) 1 else 0

            return predictedLabel
        }
        fun FloatArray.indexOfMaxOrNull(): Int {
            var maxIndex = -1
            var maxValue = Float.MIN_VALUE
            for (i in indices) {
                if (this[i] > maxValue) {
                    maxValue = this[i]
                    maxIndex = i
                }
            }
            return maxIndex
        }

        /*fun containsBlockedKeywords(url: String): Boolean {
            return blockedKeywords.any { keyword -> url.contains(keyword, ignoreCase = true) }
        }*/
    }

}