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
import androidx.compose.ui.res.painterResource
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
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val jeS10BrojValjan = s10BrojValjan(uneseniTekst)
    val unosImaGresku = uneseniTekst.length == 13 && !jeS10BrojValjan
    val bojaIspravnogUnosa = Color(0xFF2E7D32)

    val pokusajGenerisanja = {
        if (jeS10BrojValjan) {
            focusManager.clearFocus()
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
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
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
                    val normalizovanUnos = noviUnos
                        .let(::normalizujS10Unos)
                    if (normalizovanUnos.length <= 13) {
                        uneseniTekst = normalizovanUnos
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = trenutnaTastatura,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { pokusajGenerisanja() }
                ),
                label = { Text("Broj pošiljke") },
                placeholder = {
                    Text(
                        text = "EE123456785BA",
                        color = Color.Gray.copy(alpha = 0.2f),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                trailingIcon = {
                    when {
                        jeS10BrojValjan -> Icon(
                            painter = painterResource(R.drawable.icon_check_circle),
                            contentDescription = "Ispravan broj pošiljke",
                            tint = bojaIspravnogUnosa,
                            modifier = Modifier.size(26.dp)
                        )
                        uneseniTekst.isNotEmpty() -> Text(
                            text = "${uneseniTekst.length}/13",
                            color = if (unosImaGresku) MaterialTheme.colorScheme.error else Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                isError = unosImaGresku,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (jeS10BrojValjan) bojaIspravnogUnosa else MainColor,
                    unfocusedBorderColor = if (jeS10BrojValjan) bojaIspravnogUnosa else SecondaryColor,
                    focusedLabelColor = if (jeS10BrojValjan) bojaIspravnogUnosa else Color.Gray,
                    unfocusedLabelColor = if (jeS10BrojValjan) bojaIspravnogUnosa else Color.Gray,
                    cursorColor = if (jeS10BrojValjan) bojaIspravnogUnosa else SecondaryColor,
                    focusedTextColor = SecondaryColor,
                    unfocusedTextColor = SecondaryColor
                )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (unosImaGresku) {
                    Text(
                        text = "Neispravan broj. Provjeri broj pošiljke.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }
            }
            Button(
                onClick = { pokusajGenerisanja() },
                enabled = jeS10BrojValjan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryColor,
                    contentColor = Background
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
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
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("ZATVORI", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
internal fun Context.findActivity(): Activity? {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
