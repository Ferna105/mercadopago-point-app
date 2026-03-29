package com.barrita.android.mainapp.app.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * QR con el mismo payload que la web en /payment/success: solo el UUID del pedido.
 * Corrección H y margen silencioso, alineado a qrcode.react level="H" e includeMargin.
 */
object QrBitmapGenerator {

    private val hints: Map<EncodeHintType, Any> = mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
        EncodeHintType.MARGIN to 2
    )

    fun bitmapForOrderId(orderId: String, sizePx: Int): Bitmap {
        val matrix = MultiFormatWriter().encode(
            orderId,
            BarcodeFormat.QR_CODE,
            sizePx,
            sizePx,
            hints
        )
        val w = matrix.width
        val h = matrix.height
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        for (x in 0 until w) {
            for (y in 0 until h) {
                bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }
}
