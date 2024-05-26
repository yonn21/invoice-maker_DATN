package com.example.invoicemaker_1_0_231212

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class SignatureActivity : BaseActivity() {
    private lateinit var signatureView: SignatureView
    private lateinit var doneButton: Button
    private lateinit var resetButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signature)

        signatureView = findViewById(R.id.signature_view)
        doneButton = findViewById(R.id.signature_done_button)
        resetButton = findViewById(R.id.signature_reset_button)
        backButton = findViewById(R.id.signature_back_button)

        resetButton.setOnClickListener {
            signatureView.clear()
        }

        doneButton.setOnClickListener {
            if (!signatureView.isSignatureEmpty()) {
                val signatureBitmap = signatureView.saveSignature()
                val type = intent.getStringExtra("SIGNATURE_TYPE") ?: return@setOnClickListener
                val savedUri = saveSignatureToFile(signatureBitmap, type)

                val resultIntent = Intent().apply {
                    putExtra("signature_uri", savedUri.toString())
                    putExtra("SIGNATURE_TYPE", type)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                // Remove signature file if it exists
                val type = intent.getStringExtra("SIGNATURE_TYPE") ?: return@setOnClickListener
                val tempPhotoDir = File(filesDir, "TempPhotos")
                val fileName = if (type == "Seller") "sellerSignature.png" else "buyerSignature.png"
                val signatureFile = File(tempPhotoDir, fileName)
                if (signatureFile.exists()) signatureFile.delete()
                setResult(RESULT_CANCELED)
                finish()
            }
        }

        backButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

    }

    private fun saveSignatureToFile(signatureBitmap: Bitmap, type: String): Uri {

        val tempPhotoDir = File(filesDir, "TempPhotos")
        if (!tempPhotoDir.exists()) tempPhotoDir.mkdirs()

        val fileName = if (type == "Seller") "sellerSignature.png" else "buyerSignature.png"
        val signatureFile = File(tempPhotoDir, fileName)

        FileOutputStream(signatureFile).use { out ->
            signatureBitmap.compress(Bitmap.CompressFormat.PNG, 50, out)
        }

        return Uri.fromFile(signatureFile)
    }

}