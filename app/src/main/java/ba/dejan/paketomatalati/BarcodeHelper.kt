package ba.dejan.paketomatalati

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import ba.dejan.paketomatalati.ui.theme.Background
import ba.dejan.paketomatalati.ui.theme.SecondaryColor
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

fun generateCode128(text: String): ImageBitmap? =
    generisiKod(text, BarcodeFormat.CODE_128, width = 1440, height = 400)

fun generateQrCode(text: String): ImageBitmap? =
    generisiKod(
        text, BarcodeFormat.QR_CODE, width = 1440, height = 1440,
        hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L)
    )

private fun generisiKod(
    text: String,
    format: BarcodeFormat,
    width: Int,
    height: Int,
    hints: Map<EncodeHintType, Any>? = null
): ImageBitmap? {
    if (text.isBlank()) return null
    return try {
        MultiFormatWriter().encode(text, format, width, height, hints).toImageBitmap()
    } catch (e: Exception) {
        null
    }
}

private fun BitMatrix.toImageBitmap(): ImageBitmap {
    val pixels = IntArray(width * height)
    val bojaPozadine = Background.toArgb()
    val bojaKoda = SecondaryColor.toArgb()
    for (y in 0 until height) {
        val offset = y * width
        for (x in 0 until width) {
            pixels[offset + x] = if (this[x, y]) bojaKoda else bojaPozadine
        }
    }
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return bitmap.asImageBitmap()
}
