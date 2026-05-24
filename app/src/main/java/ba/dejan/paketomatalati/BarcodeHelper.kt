package ba.dejan.paketomatalati

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import ba.dejan.paketomatalati.ui.theme.Background
import ba.dejan.paketomatalati.ui.theme.SecondaryColor
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

fun generateCode128(text: String): ImageBitmap? {
    if (text.isBlank()) return null
    return try {
        val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.CODE_128, 900, 400)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        val bojaPozadine = Background.toArgb()
        val bojaKoda = SecondaryColor.toArgb()
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix[x, y]) {
                    bojaKoda
                } else {
                    bojaPozadine
                }
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
fun generateQrCode(text: String): ImageBitmap? {
    if (text.isBlank()) return null
    return try {
        val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 600, 600)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        val bojaPozadine = Background.toArgb()
        val bojaKoda = SecondaryColor.toArgb()
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix[x, y]) {
                    bojaKoda
                } else {
                    bojaPozadine
                }
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}