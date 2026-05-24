package ba.dejan.paketomatalati

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.TextStyle
import android.widget.Toast
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import ba.dejan.paketomatalati.ui.theme.Background
import ba.dejan.paketomatalati.ui.theme.MainColor
import ba.dejan.paketomatalati.ui.theme.SecondaryColor

@Composable
fun MainScreen(radnikData: RadnikData) {
    var uneseniTekst by remember { mutableStateOf("") }
    var showLoginPopup by remember { mutableStateOf(false) }
    var showPackagePopup by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Button(
            onClick = { showLoginPopup = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.TopCenter),
            colors = ButtonDefaults.buttonColors(
                containerColor = MainColor,
                contentColor = SecondaryColor
            )
        ) {
            Text("Prijava na paketomat", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .offset(y = (-60).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val trenutnaTastatura = when {
                uneseniTekst.length < 2 -> KeyboardType.Text
                uneseniTekst.length < 11 -> KeyboardType.Number
                else -> KeyboardType.Text
            }
            OutlinedTextField(
                value = uneseniTekst,
                onValueChange = { noviUnos ->
                    if (noviUnos.length <= 13) {
                        uneseniTekst = noviUnos.uppercase()
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = trenutnaTastatura,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (uneseniTekst.isNotBlank()) {
                            showPackagePopup = true
                        } else {
                            Toast.makeText(context, "Unesi broj pošiljke!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ),
                label = { Text("Broj pošiljke") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                //  textAlign = TextAlign.Center
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MainColor,
                    unfocusedBorderColor = SecondaryColor,
                    focusedLabelColor = Color.Gray,
                    unfocusedLabelColor = Color.Gray,
                    cursorColor = SecondaryColor,
                    focusedTextColor = SecondaryColor,
                    unfocusedTextColor = SecondaryColor
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (uneseniTekst.isNotBlank()) {
                        showPackagePopup = true
                    } else {
                        Toast.makeText(context, "Unesi broj pošiljke!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryColor,
                    contentColor = Background
                )
            ) {
                Text("GENERIŠI BARKOD", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    if (showLoginPopup) {
        BarcodeDialog(
            textZaKodiranje = radnikData.barCodeSifra,
            dodatniTekst = "Šifra: ${radnikData.sifraRadnika}",
            isQrCode = true,
            onDismiss = { showLoginPopup = false }
        )
    }
    if (showPackagePopup) {
        BarcodeDialog(
            textZaKodiranje = uneseniTekst,
            dodatniTekst = "$uneseniTekst",
            isQrCode = false,
            onDismiss = { showPackagePopup = false }
        )
    }
}
@Composable
fun BarcodeDialog(
    textZaKodiranje: String,
    dodatniTekst: String,
    isQrCode: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val barcodeBitmap: ImageBitmap? = remember(textZaKodiranje, isQrCode) {
        if (isQrCode) generateQrCode(textZaKodiranje) else generateCode128(textZaKodiranje)
    }
    DisposableEffect(Unit) {
        val window = (context as Activity).window
        val layoutParams = window.attributes
        val originalBrightness = layoutParams.screenBrightness
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        window.attributes = layoutParams
        onDispose {
            layoutParams.screenBrightness = originalBrightness
            window.attributes = layoutParams
        }
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (barcodeBitmap != null) {
                    Image(
                        bitmap = barcodeBitmap,
                        contentDescription = "Barkod / QR Kod",
                        modifier = if (isQrCode) {
                            Modifier.fillMaxWidth().aspectRatio(1f)
                        } else {
                            Modifier.fillMaxWidth().height(160.dp)
                        }
                    )
                } else {
                    Text("Greška pri generisanju koda", color = SecondaryColor)
                }
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = dodatniTekst,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(64.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MainColor,
                        contentColor = SecondaryColor
                    )
                ) {
                    Text("ZATVORI", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}