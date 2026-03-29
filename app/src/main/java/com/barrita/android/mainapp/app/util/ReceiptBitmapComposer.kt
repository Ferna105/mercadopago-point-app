package com.barrita.android.mainapp.app.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint

/**
 * Un solo bitmap térmico: texto del comprobante + QR del [orderId] debajo (misma cadena que /payment/success).
 */
object ReceiptBitmapComposer {

    /** Ancho total: debe albergar el QR (2× el tamaño anterior de 200px) más padding. */
    private const val RECEIPT_WIDTH_PX = 432
    private const val HORIZONTAL_PADDING = 16f
    private const val VERTICAL_PADDING = 20f
    private const val TEXT_SIZE_PX = 20f
    private const val GAP_BEFORE_QR = 20f
    private const val QR_MODULE_PX = 400

    fun compose(receiptText: String, orderId: String): Bitmap {
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = TEXT_SIZE_PX
            color = Color.BLACK
            typeface = Typeface.MONOSPACE
        }
        val contentWidth = (RECEIPT_WIDTH_PX - 2 * HORIZONTAL_PADDING).toInt().coerceAtLeast(1)
        val textLayout = StaticLayout.Builder.obtain(receiptText, 0, receiptText.length, textPaint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setIncludePad(false)
            .build()

        val qrBitmap = QrBitmapGenerator.bitmapForOrderId(orderId, QR_MODULE_PX)
        val height = (
            VERTICAL_PADDING + textLayout.height + GAP_BEFORE_QR +
                qrBitmap.height + VERTICAL_PADDING
            ).toInt().coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(RECEIPT_WIDTH_PX, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        canvas.save()
        canvas.translate(HORIZONTAL_PADDING, VERTICAL_PADDING)
        textLayout.draw(canvas)
        canvas.restore()

        val qrLeft = (RECEIPT_WIDTH_PX - qrBitmap.width) / 2f
        val qrTop = VERTICAL_PADDING + textLayout.height + GAP_BEFORE_QR
        canvas.drawBitmap(qrBitmap, qrLeft, qrTop, null)
        return bitmap
    }
}
