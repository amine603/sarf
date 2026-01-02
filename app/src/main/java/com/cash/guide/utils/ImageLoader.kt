package com.cash.guide.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.IOException

/**
 * Utility class for loading images from assets
 * Implements in-memory caching to avoid reloading on recomposition
 */
object ImageLoader {
    // Simple in-memory cache for loaded bitmaps
    private val bitmapCache = mutableMapOf<String, ImageBitmap>()

    /**
     * Loads an image from assets and converts it to ImageBitmap
     * Uses caching to avoid reloading on recomposition
     * 
     * @param context Android context to access assets
     * @param assetPath Path to the asset file (e.g., "sarfpic/200.png")
     * @param trimTransparentPadding If true, trims fully-transparent padding around PNGs so
     * border/shadow can follow the currency shape instead of a big invisible box.
     * @return ImageBitmap if loaded successfully, null otherwise
     */
    fun loadImageFromAssets(
        context: Context,
        assetPath: String,
        trimTransparentPadding: Boolean = false
    ): ImageBitmap? {
        val cacheKey = if (trimTransparentPadding) "$assetPath|trim" else assetPath
        // Check cache first
        bitmapCache[cacheKey]?.let {
            return it
        }

        return try {
            // Open asset file
            val inputStream = context.assets.open(assetPath)
            
            // Decode bitmap
            val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
            
            // Close stream
            inputStream.close()

            val processed = if (trimTransparentPadding) trimTransparent(bitmap) else bitmap
            
            // Convert to ImageBitmap
            val imageBitmap = processed.asImageBitmap()
            
            // Cache it
            bitmapCache[cacheKey] = imageBitmap
            
            imageBitmap
        } catch (e: IOException) {
            // Asset file not found or error reading
            e.printStackTrace()
            null
        }
    }

    private fun trimTransparent(source: Bitmap): Bitmap {
        // Only makes sense for images with alpha (PNGs with transparency).
        if (!source.hasAlpha()) return source

        val w = source.width
        val h = source.height
        if (w <= 1 || h <= 1) return source

        var minX = w
        var minY = h
        var maxX = -1
        var maxY = -1

        // Scan pixels: find bounds where alpha > threshold.
        // We keep threshold small to avoid chopping anti-aliased edges.
        val threshold = 10
        val row = IntArray(w)
        for (y in 0 until h) {
            source.getPixels(row, 0, w, 0, y, w, 1)
            for (x in 0 until w) {
                val alpha = (row[x] ushr 24) and 0xFF
                if (alpha > threshold) {
                    if (x < minX) minX = x
                    if (y < minY) minY = y
                    if (x > maxX) maxX = x
                    if (y > maxY) maxY = y
                }
            }
        }

        // No visible pixels
        if (maxX < minX || maxY < minY) return source

        val cropW = (maxX - minX + 1).coerceAtLeast(1)
        val cropH = (maxY - minY + 1).coerceAtLeast(1)

        // If it didn't really change, return original.
        if (cropW == w && cropH == h) return source

        return Bitmap.createBitmap(source, minX, minY, cropW, cropH)
    }

    /**
     * Clears the bitmap cache
     * Useful if memory is a concern
     */
    fun clearCache() {
        bitmapCache.clear()
    }
}

