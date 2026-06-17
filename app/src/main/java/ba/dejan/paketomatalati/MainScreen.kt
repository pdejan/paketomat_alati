package ba.dejan.paketomatalati

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.window.SecureFlagPolicy
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
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ba.dejan.paketomatalati.ui.theme.Background
import ba.dejan.paketomatalati.ui.theme.MainColor
import ba.dejan.paketomatalati.ui.theme.SecondaryColor

@Composable
fun MainScreen(radnikData: RadnikData) {
    var uneseniTekst by remember { mutableStateOf("") }
    var showLoginPopup by remember { mutableStateOf(false) }
    var showPackagePopup by remember { mutableStateOf(false) }
    var showCheckDigitWarning by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val pokusajGenerisanja = {
        when {
            uneseniTekst.isBlank() ->
                Toast.makeText(context, "Unesi broj pošiljke!", Toast.LENGTH_SHORT).show()
            izgledaKaoS10(uneseniTekst) && !s10KontrolnaCifraValjana(uneseniTekst) ->
                showCheckDigitWarning = true
            else ->
                showPackagePopup = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Button(
            onClick = {
                val activity = context.findActivity() as? FragmentActivity
                if (activity != null) {
                    zatraziAutentifikaciju(activity) { showLoginPopup = true }
                } else {
                    showLoginPopup = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.TopCenter),
            colors = ButtonDefaults.buttonColors(
                containerColor = MainColor,
                contentColor = SecondaryColor
            )
        ) {
            Text("PRIJAVA NA PAKETOMAT", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                        pokusajGenerisanja()
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
                onClick = { pokusajGenerisanja() },
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
            secure = true,
            onDismiss = { showLoginPopup = false }
        )
    }
    if (showPackagePopup) {
        BarcodeDialog(
            textZaKodiranje = uneseniTekst,
            dodatniTekst = uneseniTekst,
            isQrCode = false,
            onDismiss = { showPackagePopup = false }
        )
    }
    if (showCheckDigitWarning) {
        AlertDialog(
            onDismissRequest = { showCheckDigitWarning = false },
            containerColor = Color.White,
            title = {
                Text(
                    "Provjeri broj pošiljke",
                    fontWeight = FontWeight.Bold,
                    color = SecondaryColor
                )
            },
            text = {
                Text(
                    "Kontrolna cifra se ne poklapa s ostatkom broja, moguća je greška u kucanju. Provjeri broj pošiljke prije nego što ga generišeš.",
                    color = SecondaryColor,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showCheckDigitWarning = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MainColor,
                        contentColor = SecondaryColor
                    )
                ) {
                    Text("ISPRAVI", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCheckDigitWarning = false
                    showPackagePopup = true
                }) {
                    Text("SVEJEDNO GENERIŠI", color = Color.Gray)
                }
            }
        )
    }
}
private sealed interface BarkodStanje {
    data object Generisanje : BarkodStanje
    data class Spremno(val bitmap: ImageBitmap) : BarkodStanje
    data object Greska : BarkodStanje
}
@Composable
fun BarcodeDialog(
    textZaKodiranje: String,
    dodatniTekst: String,
    isQrCode: Boolean = false,
    secure: Boolean = false,
    onDismiss: () -> Unit
) {
    val stanjeKoda by produceState<BarkodStanje>(BarkodStanje.Generisanje, textZaKodiranje, isQrCode) {
        value = BarkodStanje.Generisanje
        val bitmap = withContext(Dispatchers.Default) {
            if (isQrCode) generateQrCode(textZaKodiranje) else generateCode128(textZaKodiranje)
        }
        value = if (bitmap != null) BarkodStanje.Spremno(bitmap) else BarkodStanje.Greska
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            securePolicy = if (secure) SecureFlagPolicy.SecureOn else SecureFlagPolicy.Inherit
        )
    ) {
        // Osvjetljenje postavljamo na PROZOR DIJALOGA, ne na Activity prozor. Dijalog je
        // poseban, neproziran prozor preko cijelog ekrana iznad Activityja, pa override na
        // Activity prozoru sistem može preskočiti (obscured). Postavljanjem na sam dialog
        // prozor garantujemo da barkod bude maksimalno svijetao za skener paketomata.
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        DisposableEffect(dialogWindow) {
            val originalBrightness = dialogWindow?.attributes?.screenBrightness
            dialogWindow?.let {
                it.attributes = it.attributes.apply {
                    screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
                }
            }
            onDispose {
                if (dialogWindow != null && originalBrightness != null) {
                    dialogWindow.attributes = dialogWindow.attributes.apply {
                        screenBrightness = originalBrightness
                    }
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val kodModifier = if (isQrCode) {
                    Modifier.fillMaxWidth().aspectRatio(1f)
                } else {
                    Modifier.fillMaxWidth().height(160.dp)
                }
                when (val trenutnoStanje = stanjeKoda) {
                    is BarkodStanje.Spremno -> Image(
                        bitmap = trenutnoStanje.bitmap,
                        contentDescription = "Barkod / QR Kod",
                        modifier = kodModifier
                    )
                    is BarkodStanje.Generisanje -> Box(modifier = kodModifier)
                    is BarkodStanje.Greska -> Text("Greška pri generisanju koda.", color = SecondaryColor)
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
private fun Context.findActivity(): Activity? {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}