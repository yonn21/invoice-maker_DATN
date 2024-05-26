package com.example.invoicemaker_1_0_231212

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class SignatureView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var path = android.graphics.Path()
    private var paint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 9f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(event.x, event.y)
            }
            else -> return false
        }
        invalidate()
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    fun clear() {
        path.reset()
        invalidate()
    }

    fun isSignatureEmpty(): Boolean {
        return path.isEmpty
    }

    fun saveSignature(cornerRadius: Float = 29f): Bitmap {
        val size = Math.min(width, height)
        val originalBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(originalBitmap)
        canvas.translate((size - width) / 2f, (size - height) / 2f)
        draw(canvas)

        // Resize bitmap to 300x300 pixels before making background transparent
        var resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 300, 300, true)

        // Create a bitmap with the same size but with a transparent background
        var output = Bitmap.createBitmap(resizedBitmap.width, resizedBitmap.height, Bitmap.Config.ARGB_8888)

        val paint = Paint()
        val rect = Rect(0, 0, resizedBitmap.width, resizedBitmap.height)
        val rectF = RectF(rect)

        // Prepare the paint and canvas for rounded corners
        paint.isAntiAlias = true
        paint.color = Color.WHITE
        /*val canvasRounded = Canvas(output)
        canvasRounded.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)*/

        // non rounded corners
        val finalCanvas = Canvas(output)
        finalCanvas.drawRect(rectF, paint)

        // Set the Xfermode of the paint to SRC_IN to only draw the signature where it overlaps with the rounded rectangle
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        finalCanvas.drawBitmap(resizedBitmap, rect, rect, paint)

        return output
    }

    private fun makeBackgroundTransparent(signatureBitmap: Bitmap): Bitmap {
        val width = signatureBitmap.width
        val height = signatureBitmap.height
        val transparentBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(transparentBitmap)
        canvas.drawBitmap(signatureBitmap, 0f, 0f, null)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = signatureBitmap.getPixel(x, y)
                if (isColorWhite(pixel)) {
                    transparentBitmap.setPixel(x, y, Color.TRANSPARENT)
                }
            }
        }
        return transparentBitmap
    }

    private fun isColorWhite(pixel: Int): Boolean {
        val redValue = Color.red(pixel)
        val blueValue = Color.blue(pixel)
        val greenValue = Color.green(pixel)
        // This threshold can be adjusted according to how strict you want to be about what is considered "white".
        return redValue > 200 && greenValue > 200 && blueValue > 200
    }

    private fun isColorBlue(pixel: Int): Boolean {
        val redValue = Color.red(pixel)
        val blueValue = Color.blue(pixel)
        val greenValue = Color.green(pixel)
        // This threshold can be adjusted according to how strict you want to be about what is considered "blue".
        return redValue < 100 && greenValue < 100 && blueValue > 150
    }
}
