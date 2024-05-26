package com.example.invoicemaker_1_0_231212

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.invoicemaker_1_0_231212.model.BusinessData
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream

class BusinessDataActivity : BaseActivity() {

    companion object {
        private const val REQUEST_CODE_IMAGE_PICK = 2108
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 10011
    }
    private var businessLogoPath: String? = null
    private var tempBitmap: Bitmap? = null // Display logo when pick image from gallery, but not save yet
    private var logoRemoved: Boolean = false // Check if logo is removed

    private lateinit var businessLogo: ImageView
    private lateinit var businessName: EditText
    private lateinit var businessEmail: EditText
    private lateinit var businessPhoneNumber: EditText
    private lateinit var businessAddress: EditText
    private lateinit var buttonSave: Button
    private lateinit var buttonBack: ImageButton
    private lateinit var buttonEditLogo: ImageButton
    private lateinit var buttonRemoveLogo: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_data)

        businessLogo = findViewById(R.id.BusinessData_Logo)
        businessName = findViewById(R.id.BusinessData_Name)
        businessEmail = findViewById(R.id.BusinessData_Email)
        businessPhoneNumber = findViewById(R.id.BusinessData_PhoneNumber)
        businessAddress = findViewById(R.id.BusinessData_Address)
        buttonSave = findViewById(R.id.BusinessData_Button_Save)
        buttonBack = findViewById(R.id.BusinessData_Button_Back)
        buttonEditLogo = findViewById(R.id.BusinessData_Button_EditLogo)
        buttonRemoveLogo = findViewById(R.id.BusinessData_Button_RemoveLogo)
        val buttonSaveCard = findViewById<CardView>(R.id.BusinessData_Button_Save_Card)

        // Display data
        val businessDataFile = File(filesDir, "BusinessData.json")
        if (businessDataFile.exists()) {
            val jsonData = businessDataFile.readText()
            val businessData = Gson().fromJson(jsonData, BusinessData::class.java)

            // Logo
            if (!businessData.businessLogo.isNullOrEmpty() && File(businessData.businessLogo).exists()) {
                businessLogoPath = businessData.businessLogo
                val bitmap = BitmapFactory.decodeFile(businessLogoPath)
                businessLogo.setImageBitmap(bitmap)
                displayLogo()
            } else {
                businessLogo.setImageResource(R.drawable.businessdata_inputlogo)
                displayOnlyInputLogo()
            }

            businessName.setText(businessData.businessName)
            businessEmail.setText(businessData.businessEmail)
            businessPhoneNumber.setText(businessData.businessPhoneNumber)
            businessAddress.setText(businessData.businessAddress)
        }

        // Disable button save if business name or phone number is empty on start
        if (businessName.text.isNullOrEmpty() || businessPhoneNumber.text.isNullOrEmpty()) {
            buttonSave.isClickable = false
            buttonSaveCard.setCardBackgroundColor(resources.getColor(R.color.grey))
        }

        App.isSkipOpenAd = false

        // If logo empty, hide logo edit and remove button, enable logo button on start
        if (businessLogoPath.isNullOrEmpty()) {
            displayOnlyInputLogo()
        } else {
            displayLogo()
        }

        // Pick new logo
        businessLogo.setOnClickListener() {
            App.isSkipOpenAd = true
            pickImageFromGallery()
        }

        // Edit logo
        buttonEditLogo.setOnClickListener() {
            App.isSkipOpenAd = true
            pickImageFromGallery()
        }

        // Remove logo
        buttonRemoveLogo.setOnClickListener() {
            App.isSkipOpenAd = false
            logoRemoved = true
            businessLogo.setImageResource(R.drawable.businessdata_inputlogo)
            displayOnlyInputLogo()
        }

        businessName.setOnClickListener {
            App.isSkipOpenAd = false
        }

        businessEmail.setOnClickListener {
            App.isSkipOpenAd = false
        }

        businessPhoneNumber.setOnClickListener {
            App.isSkipOpenAd = false
        }

        businessAddress.setOnClickListener {
            App.isSkipOpenAd = false
        }

        // Enable button save if business name and phone number is not empty
        businessName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val isNameEmpty = businessName.text.isNullOrEmpty()
                val isPhoneNumberEmpty = businessPhoneNumber.text.isNullOrEmpty()

                if (isNameEmpty || isPhoneNumberEmpty) {
                    buttonSave.isClickable = false
                    buttonSaveCard.setCardBackgroundColor(resources.getColor(R.color.grey))
                } else {
                    buttonSave.isClickable = true
                    buttonSaveCard.setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
                }

                App.isSkipOpenAd = false

                if (isNameEmpty) {
                    businessName.error = "Business name is required"
                }
            }

            override  fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        businessPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val isNameEmpty = businessName.text.isNullOrEmpty()
                val isPhoneNumberEmpty = businessPhoneNumber.text.isNullOrEmpty()

                if (isNameEmpty || isPhoneNumberEmpty) {
                    buttonSave.isClickable = false
                    buttonSaveCard.setCardBackgroundColor(resources.getColor(R.color.grey))
                } else {
                    buttonSave.isClickable = true
                    buttonSaveCard.setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
                }

                App.isSkipOpenAd = false

                if (isPhoneNumberEmpty) {
                    businessPhoneNumber.error = "Phone number is required"
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        // Back button
        buttonBack.setOnClickListener {
            App.isSkipOpenAd = false
            navigateBack()
        }

        // Save button
        buttonSave.setOnClickListener {
            App.isSkipOpenAd = false
            if (logoRemoved) {
                businessLogoPath?.let {
                    val file = File(it)
                    if (file.exists()) {
                        file.delete()
                    }
                    businessLogoPath = null
                }
            } else {
                tempBitmap?.let {
                    businessLogoPath = saveImageToInternalStorage(it)
                }
            }

            val businessData = BusinessData(
                businessLogo = businessLogoPath,
                businessName.text.toString(),
                businessEmail.text.toString().ifEmpty { null },
                businessPhoneNumber.text.toString(),
                businessAddress.text.toString().ifEmpty { null }
            )

            val jsonData = Gson().toJson(businessData)
            val file = File(filesDir, "BusinessData.json")
            file.writeText(jsonData)

            navigateSave()
        }

    }

    private fun navigateBack() {
        finish()
    }

    private fun navigateSave() {
        val previousScreen = intent.getStringExtra("PREVIOUS_SCREEN")

        if (previousScreen == "SettingActivity") {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        else if (previousScreen == "MainActivity" || previousScreen == "StatisticsActivity" || previousScreen == "ItemListActivity" || previousScreen == "ClientListActivity") {
            val intent = Intent(this, CreateInvoiceActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        else {
            val returnIntent = Intent()
            returnIntent.putExtra("BUSINESS_LOGO", businessLogoPath)
            returnIntent.putExtra("BUSINESS_NAME", businessName.text.toString())
            returnIntent.putExtra("BUSINESS_EMAIL", businessEmail.text.toString())
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

        finish()
    }

    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery()
            } else {
                // show toast on center
                val toast = Toast.makeText(applicationContext, "Permission denied", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }
    }

    private fun pickImageFromGallery() {

        if (hasStoragePermission()) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        } else {
            requestStoragePermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            data?.data?.let { imageUri ->
                val tempBitmap: Bitmap?
                if (Build.VERSION.SDK_INT < 29) {
                    tempBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                } else {
                    val source = ImageDecoder.createSource(this.contentResolver, imageUri)
                    tempBitmap = ImageDecoder.decodeBitmap(source)
                }
                tempBitmap?.let {
                    this.tempBitmap = cropToSquareAndRoundCorners(it)
                    businessLogo.setImageBitmap(this.tempBitmap)
                }
                logoRemoved = false
                if (this.tempBitmap != null) {
                    displayLogo()
                } else {
                    displayOnlyInputLogo()
                }
            }
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val filename = "business_logo.png"
        val outputStream: FileOutputStream
        try {
            outputStream = openFileOutput(filename, MODE_PRIVATE)
            val bitmap = cropToSquareAndRoundCorners(bitmap)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
            businessLogoPath = getFileStreamPath(filename).absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return getFileStreamPath(filename).absolutePath
    }

    private fun cropToSquareAndRoundCorners(bitmap: Bitmap): Bitmap {
        val compatibleBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            bitmap
        }

        // Determine the coordinates for the center square crop
        val dimension = Math.min(compatibleBitmap.width, compatibleBitmap.height)
        val x = (compatibleBitmap.width - dimension) / 2
        val y = (compatibleBitmap.height - dimension) / 2

        // Crop to square from the center of the bitmap
        val croppedBitmap = Bitmap.createBitmap(compatibleBitmap, x, y, dimension, dimension)

        // Resize bitmap to 512x512
        val resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, 512, 512, false)

        // Create a bitmap with same dimensions to draw rounded image
        val roundedBitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(roundedBitmap)

        // Paint with shader from resized bitmap
        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(resizedBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }

        // Convert corner radius from dp to px
        val cornerRadiusDp = 21f
        val cornerRadiusPx = cornerRadiusDp * resources.displayMetrics.density

        // Draw rounded rectangle over canvas
        canvas.drawRoundRect(RectF(0f, 0f, 512f, 512f), cornerRadiusPx, cornerRadiusPx, paint)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.recycle()
        }

        return roundedBitmap
    }

    private fun displayOnlyInputLogo() {
        buttonEditLogo.visibility = ImageButton.INVISIBLE
        buttonRemoveLogo.visibility = ImageButton.INVISIBLE
        businessLogo.visibility = ImageView.VISIBLE
        buttonEditLogo.isEnabled = false
        buttonRemoveLogo.isEnabled = false
        businessLogo.isEnabled = true
    }

    private fun displayLogo() {
        buttonEditLogo.visibility = ImageButton.VISIBLE
        buttonRemoveLogo.visibility = ImageButton.VISIBLE
        businessLogo.visibility = ImageView.VISIBLE
        buttonEditLogo.isEnabled = true
        buttonRemoveLogo.isEnabled = true
        businessLogo.isEnabled = false
    }
}