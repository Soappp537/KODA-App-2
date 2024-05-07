package com.example.kodaapplication.Classes

import android.content.Context
import android.util.Log
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
            val tokens = tokenizeText(finalText)
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
        // leetspeak to normal words
        fun translateLeetToNormal(leetText: String): String {
            val leetToNormalMap = mapOf(
                'a' to listOf("4", "@", "/-\\", "^", "Д", "α", "Ã", "ã", "Å", "å", "Ā", "ā", "À", "à", "Á", "á", "Â", "â", "Ä", "ä"),
                'b' to listOf("|3", "ß", "!3", "(3", "/3", ")3", "ẞ", "ദ", "♭", "ḃ", "ḅ", "ḇ", "Ḇ", "Ḅ", "൫", "ⓑ"),
                'c' to listOf("¢", "<", "(", "©", "[", "Ç", "ℂ", "℃", "₡", "∁"),
                'd' to listOf("|)", "[)", "I>", "?", "T)", "I7", "cl", "|}", "|]", "ḋ", "ḍ", "ḏ", "ḑ", "ḓ", "d", "Ḓ", "Ḋ", "Ḍ", "Ḏ", "Ḑ"),
                'e' to listOf("3", "£", "€", "[-", "ë", "£", "Ē", "ē", "Ê", "ê", "Ë", "ë", "È", "è", "É", "é", "ℇ", "℈", "℉", "℮", "ℯ", "ⓔ", "∊"),
                'f' to listOf("ƒ", "/=", "Ⅎ"),
                'g' to listOf("&","ℊ", "ġ", "❡", "ⓖ"),
                'h' to listOf("#", "/-/", "\\-\\", "]-[", ")-(", "(-)", ":-:", "|~|", "|-|", "]~[", "}{", "ℋ", "ℌ", "ℍ", "ℎ", "ℏ"),
                'i' to listOf("1", "|", "][", "!", "Ì", "ì", "Ï", "ï", "Ī", "ī", "Î", "î", "Í", "í", "℩", "유"),
                'j' to listOf("ℐ", "ℑ", "ʝ", "Ⓙ", "ⓙ", "♩"),
                'k' to listOf(">|", "|<", "|c", "|(","7<", "K.", "ⓚ", "₭"),
                'l' to listOf("|_", "|", "ℒ"),
                'm' to listOf("/\\/", "/V\\", "[V]", "|/|", "^^", "<\\/>", "{V}", "(v)", "(V)", "|\\|\\", "]\\/[", "Պ", "ണ", "൩", "൬", "ന", "സ", "ⓜ"),
                'n' to listOf("^/", "|\\|", "/\\/", "[\\]", "<\\>", "{\\}", "/V", "^", "ท", "И", "ℕ", "Ṋ", "Ṉ", "Ṇ", "Ṅ", "₦", "ῇ", "ῆ", "ῄ", "ῃ", "ῂ", "ᾗ", "ᾖ", "ᾕ", "ᾔ", "ᾓ", "ᾒ", "ᾑ", "ᾐ", "ή", "ὴ", "ἧ", "ἦ", "ἥ" ,"ἤ", "ἣ", "ἢ", "ἡ", "ṅ", "ṇ", "ṉ", "ṋ", "ἠ", "ഗ"),
                'o' to listOf("0", "()", "[]", "Ø", "°", "Õ", "õ", "Ō", "ō", "Ò", "ò", "Ö", "ö", "Ô", "ô", "Ó", "ó", "Ω", "℧"),
                'p' to listOf("(_,)", "℗", "¶", "ℙ", "ṗ", "ṕ", "ῥ", "ῤ", "Ῥ", "Ṕ"),
                'q' to listOf("զ", "ⓠ", "ҩ", "ℚ", "ǭ", "Ǭ"),
                'r' to listOf("®", "Я", "ℜ", "ℝ", "℞", "Ṟ", "Ṝ", "Ṛ", "Ṙ", "℟", "ℜ", "ṝ", "ṟ", "ṙ", "ṛ", "Ի", "ⓡ"),
                's' to listOf("$", "§", "ʂ", "Ṩ", "Ṧ", "Ṥ", "Ṣ", "Ṡ", "Š", "ş", "ṩ", "ṧ", "ṥ", "ṣ", "ṡ", "ട", "5"),
                't' to listOf("+", "†", "' ][ '", "Ṱ", "Ṯ", "Ṭ", "₮", "ẗ", "ṱ", "ṯ", "ṭ", "ṫ", "ⓣ"),
                'u' to listOf("(_)", "|_|", "บ", "µ"),
                'v' to listOf("\\/\\/", "|/", "\\\\|", "ν", "ѵ", "ⓥ", "ṽ", "ṿ", "Ṽ", "Ṿ"),
                'w' to listOf("\\/\\/", "\\^/", "\\/\\/", "\\\\X/", "\\\\|/", "\\\\_\\_/", "พ", "₩", "ⓦ", "ഡ", "ധ", "ω", "ẁ", "ẃ", "ẅ", "ẇ", "ẉ", "ẘ", "ὠ", "ὡ", "ὢ", "ὣ", "ὤ", "ὥ", "ὦ", "ὧ", "ὼ", "ώ", "ᾠ", "ᾡ", "ᾢ", "ᾣ", "ᾤ", "ᾥ", "ᾦ", "ᾧ", "ῲ", "ῳ", "ῴ", "ῶ", "ῷ", "₩", "Ẁ", "Ẃ", "Ẅ", "Ẇ", "Ẉ"),
                'x' to listOf("><", "}{" , "×", "}{", ")(", "ⓧ", "✗", "✘", "ẋ", "Ẋ", "Ẍ", "ẍ", "x"),
                'y' to listOf("¥", "Ỹ", "Ỵ", "Ỷ", "Ỵ", "Ỳ", "Ύ", "Ὺ", "Ῡ", "Ῠ", "Ὗ", "Ὕ", "Ὓ", "Ὑ", "Ẏ", "ㄚ", "ẏ", "ỹ", "ỷ", "ỳ", "ẙ", "ഴ", "⑂", "൮", "ⓨ"),
                'z' to listOf("2", "ʐ", "ⓩ", "ẑ", "ẓ", "ẕ", "Ẓ", "Ẕ", "Ẑ", "Ž")
            )

            val normalWord = StringBuilder()

            for (char in leetText.toLowerCase()) {
                val normalChar = leetToNormalMap.entries.firstOrNull { it.value.contains(char.toString()) }?.key
                normalWord.append(normalChar ?: char)
            }

            return normalWord.toString()
        }
            //text normalization
        fun normalizeText(text: String): String {
            // Convert text to lowercase
            var normalizedText = text.toLowerCase()

            // Remove punctuation
            normalizedText = normalizedText.replace(Regex("[^a-zA-Z0-9\\s]"), "")

            // Replace multiple whitespaces with a single space
            normalizedText = normalizedText.replace(Regex("\\s+"), " ")

            // Additional normalization steps can be added here, such as handling contractions, etc.

            return normalizedText
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