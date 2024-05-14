package com.example.kodaapplication.Classes

import android.content.Context
import android.util.Log
import android.util.Patterns
import com.example.kodaapplication.Activities.finalText
import com.example.kodaapplication.Activities.wordIndexMap
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
            fileDescriptor.close()
            return Interpreter(mappedByteBuffer)
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
            val tokens = tokenizeText(url)
            Log.d("Tokens:", tokens.joinToString(", "))

            val paddedSequence = padSequence(tokens, maxLength)
            Log.d("Padded sequence shape:", "(1, $maxLength)")
            Log.d("Padded sequence:", "[[${paddedSequence.joinToString(" ")}]]")

            return paddedSequence
        }

        fun preprocessText(text: String): String {
            if (Patterns.WEB_URL.matcher(text).matches()) {
                return preprocessUrl(text)
            }
            var processedText = text.toLowerCase()
            processedText = processedText.replace("[^a-zA-Z\\s]".toRegex(), "")

            val tokens: List<String> = processedText.split("\\s+".toRegex()).filter { it.isNotBlank() }
            val stopWords: Set<String> = setOf("a", "an", "the", "is", "are", "am", "be", "been", "being", "have", "has", "had", "do", "does", "did", "will", "would", "shall", "should", "can", "could", "may", "might", "must")
            val filteredTokens: List<String> = tokens.filter { !stopWords.contains(it) }

            return filteredTokens.joinToString(" ")
        }


        fun preprocessUrl(url: String): String {
            val decodedUrl = URLDecoder.decode(url, "UTF-8")
            val queryString = decodedUrl.substringAfter("?")
            val keywordPattern: Pattern = Pattern.compile("q=([^&]*)")
            val keywordMatcher: Matcher = keywordPattern.matcher(queryString)
            val keywords = StringBuilder()
            while (keywordMatcher.find()) {
                keywords.append(keywordMatcher.group(1)).append(" ")
            }
            return keywords.trim().toString()
        }

        fun tokenizeText(text: String): List<Int> {
            val tokens: List<String> = text.split("\\s+".toRegex())
            return tokens.map { wordIndexMap[it] ?: 0 }
        }

        fun padSequence(tokens: List<Int>, maxLength: Int): IntArray {
            val paddedSequence: IntArray = IntArray(maxLength) { 0 }
            val startIdx = maxLength - tokens.size.coerceAtMost(maxLength)
            tokens.take(maxLength).forEachIndexed { index, token ->
                paddedSequence[startIdx + index] = token
            }
            return paddedSequence
        }

        fun translateLeetToNormal(leetText: String): String {
            val leetToNormalMap = mapOf(
                'a' to listOf("4", "@", "/-\\", "^", "Д", "α", "Ã", "ã", "Å", "å", "Ā", "ā", "À", "à", "Á", "á", "Â", "â", "Ä", "ä"),
                'b' to listOf("|3", "ß", "!3", "(3", "/3", ")3", "ẞ", "ദ", "♭", "ḃ", "ḅ", "ḇ", "Ḇ", "Ḅ", "൫", "ⓑ"),
                'c' to listOf("¢", "<", "(", "©", "[", "Ç", "ℂ", "℃", "₡", "∁"),
                'd' to listOf("|)", "[)", "I>", "?", "T)", "I7", "cl", "|}", "|]", "ḋ", "ḍ", "ḏ", "ḑ", "ḓ", "d", "Ḓ", "Ḋ", "Ḍ", "Ḏ", "Ḑ"),
                'e' to listOf("3", "£", "€", "[-", "ë", "£", "Ē", "ē", "Ė", "ė", "Ę", "ę", "Ě", "ě"),
                'f' to listOf("ƒ", "|=", "ph", "|#", "ʃ", "Ⅎ", "ſ"),
                'g' to listOf("6", "&", "(_+", "₲", "ǥ", "ĝ", "ğ", "ġ", "ģ", "Ḡ"),
                'h' to listOf("|-|", "#", "[-]", "<~>", "(-)", "):-:", ")-(", "}{", "ɦ", "ḧ", "ḩ", "ḥ", "Ḥ", "Ḫ"),
                'i' to listOf("1", "!", "|", "][", "]", "!", "ȋ", "ḭ", "ǐ", "ī", "į", "ḯ", "Ḭ"),
                'j' to listOf("._|", "_|", "._]", "_]", "_)", "ʝ", "ĵ", "ǰ", "ɉ"),
                'k' to listOf("|<", "|{", "ɮ", "ḳ", "ḵ", "ⓚ", "Κ", "к"),
                'l' to listOf("|_", "|", "|'", "1", "ℓ", "ḻ", "ḷ", "ḹ", "Ḽ", "Ḷ", "Ḹ"),
                'm' to listOf("/\\/\\", "|\\/|", "^^", "em", "AA", "[]\\/][", "ḿ", "ṁ", "ṃ", "ⓜ"),
                'n' to listOf("|\\|", "/\\/", "/V", "₪", "ท", "И", "и", "ṉ", "ṅ", "ṇ", "Ṇ", "Ṉ", "Ṋ"),
                'o' to listOf("0", "()", "[]", "{}", "<>", "Ø", "ō", "ő", "ơ", "ø", "ɵ", "Φ"),
                'p' to listOf("|*", "|o", "|º", "|^(o)", "|>", "9", "þ", "ρ", "ṕ", "ṗ", "Ṕ", "Ṗ"),
                'q' to listOf("(_,)", "9", "Ø", "Q", "ℚ", "ɋ", "Ɋ"),
                'r' to listOf("|2", "|?", "/2", "|^", "lz", "®", "ŕ", "ř", "ṙ", "ṛ", "ṟ", "ⓡ"),
                's' to listOf("5", "$", "§", "ŝ", "ṡ", "š", "ş", "ṥ", "ṣ", "ṩ", "Ṣ", "Ṩ"),
                't' to listOf("7", "+", "-|-", "1", "†", "ŧ", "ƭ", "ț", "ṱ", "Ṫ", "Ṭ", "Ṱ"),
                'u' to listOf("|_|", "(_)", "Y", "µ", "บ", "Ц", "ย", "ṳ", "ṷ", "ṵ", "Ṷ", "Ṻ", "Ữ"),
                'v' to listOf("\\/", "|/", "\\|", "Ɣ", "ṽ", "ṿ", "ⅴ", "Ⅴ"),
                'w' to listOf("\\/\\/", "|/\\|", "\\\\'", "'//", "VV", "\\V", "\\_|_/", "\\_:_/", "ш", "щ", "ẃ", "ẁ", "ẅ", "ẇ", "ẉ", "ẘ"),
                'x' to listOf("><", "Ж", "×", "ẋ", "ẍ", "Ẍ"),
                'y' to listOf("`/", "¥", "Ɏ", "ý", "ÿ", "ỳ", "ŷ", "ẏ", "ỵ", "ẙ", "Ỷ", "Ỹ"),
                'z' to listOf("2", "7_", ">_", "≥", "ʒ", "ź", "ž", "ż", "ẑ", "ẓ", "Ẕ", "Ẓ")
            )

            val translatedText = StringBuilder()

            for (char in leetText) {
                val lowerChar = char.toLowerCase()
                if (leetToNormalMap.containsKey(lowerChar)) {
                    translatedText.append(lowerChar)
                } else {
                    translatedText.append(char)
                }
            }
            return translatedText.toString()
        }

        fun normalizeText(text: String): String {
            return text.toLowerCase().replace("[^a-z0-9\\s]".toRegex(), "")
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

       /* fun containsBlockedKeywords(url: String): Boolean {
            return blockedKeywords.any { keyword -> url.contains(keyword, ignoreCase = true) }
        }*/
    }

}