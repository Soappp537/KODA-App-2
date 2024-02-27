package com.example.kodaapplication

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

class qrCodeScanner : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scanner)

        val generateButton = findViewById<Button>(R.id.generateButton)
        val qrCodeImageView = findViewById<ImageView>(R.id.qrCodeImageView)

        generateButton.setOnClickListener {
            val qrCodeData = "Your QR Code Data Here" // Replace with your QR code data
            val qrCodeBitmap = generateQRCode(qrCodeData, 512, 512)
            qrCodeImageView.setImageBitmap(qrCodeBitmap)
        }
    }

    private fun generateQRCode(data: String, width: Int, height: Int): Bitmap? {
        val qrCodeWriter = QRCodeWriter()
        try {
            val bitMatrix: BitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height)
            val qrCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    qrCodeBitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            return qrCodeBitmap
        } catch (e: WriterException) {
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show()
        }
        return null
    }
}
