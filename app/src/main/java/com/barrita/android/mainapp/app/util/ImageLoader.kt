package com.barrita.android.mainapp.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Carga imágenes desde URL usando OkHttp + BitmapFactory.
 * No añade dependencias que exijan compileSdk mayor a 33.
 */
object ImageLoader {

    private val client = OkHttpClient()
    private val executor: ExecutorService = Executors.newCachedThreadPool()
    private val mainHandler = Handler(Looper.getMainLooper())

    private const val MAX_DECODE_WIDTH = 800
    private const val MAX_DECODE_HEIGHT = 800

    /**
     * Carga la imagen desde [url] y la muestra en [imageView].
     * Si [url] es null/blank o no empieza por "http", no hace nada (para drawables locales usar setImageResource).
     * En RecyclerView: usa el tag del ImageView para evitar mostrar la imagen en una celda reciclada con otra URL.
     */
    fun load(imageView: ImageView, url: String?) {
        if (url.isNullOrBlank() || !url.startsWith("http")) return
        imageView.setTag(url)
        executor.execute {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    mainHandler.post { hideIfTagMatches(imageView, url) }
                    return@execute
                }
                val body = response.body()
                val bytes = body?.bytes() ?: run {
                    mainHandler.post { hideIfTagMatches(imageView, url) }
                    return@execute
                }
                val bitmap = decodeSampled(bytes) ?: run {
                    mainHandler.post { hideIfTagMatches(imageView, url) }
                    return@execute
                }
                mainHandler.post {
                    if (imageView.getTag() == url) {
                        imageView.setImageBitmap(bitmap)
                        imageView.visibility = ImageView.VISIBLE
                    }
                }
            } catch (_: Exception) {
                mainHandler.post { hideIfTagMatches(imageView, url) }
            }
        }
    }

    private fun hideIfTagMatches(imageView: ImageView, url: String) {
        if (imageView.getTag() == url) {
            imageView.visibility = ImageView.GONE
        }
    }

    private fun decodeSampled(bytes: ByteArray): Bitmap? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        val w = opts.outWidth
        val h = opts.outHeight
        if (w <= 0 || h <= 0) return null
        opts.inSampleSize = computeSampleSize(w, h)
        opts.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    }

    private fun computeSampleSize(width: Int, height: Int): Int {
        var size = 1
        while (width / size > MAX_DECODE_WIDTH || height / size > MAX_DECODE_HEIGHT) {
            size *= 2
        }
        return size
    }
}
