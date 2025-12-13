package com.ctonew.composemodular.data.messaging

import android.graphics.Bitmap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class BitmapScaler @Inject constructor() {

    fun scale(bitmap: Bitmap, maxSize: Int): Bitmap {
        if (bitmap.width <= maxSize && bitmap.height <= maxSize) {
            return bitmap
        }

        val scale = minOf(
            maxSize.toFloat() / bitmap.width,
            maxSize.toFloat() / bitmap.height,
        )

        val width = (bitmap.width * scale).roundToInt()
        val height = (bitmap.height * scale).roundToInt()

        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    fun scaleToSquare(bitmap: Bitmap, size: Int): Bitmap {
        if (bitmap.width == size && bitmap.height == size) {
            return bitmap
        }

        val scaled = Bitmap.createScaledBitmap(bitmap, size, size, true)
        if (scaled != bitmap) {
            bitmap.recycle()
        }
        return scaled
    }
}
