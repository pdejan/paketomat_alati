package ba.dejan.paketomatalati

import android.net.Uri
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ba.dejan.paketomatalati.ui.theme.Background
import ba.dejan.paketomatalati.ui.theme.MainColor
import ba.dejan.paketomatalati.ui.theme.SecondaryColor
import com.google.android.gms.common.moduleinstall.InstallStatusListener
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
@Composable
fun LoginScreen(
    userPreferences: UserPreferences
) {
    val context = LocalContext.current
    var imeRadnika by remember { mutableStateOf("") }
    var barCodeSifra by remember { mutableStateOf("") }
    var sifraRadnika by remember { mutableStateOf("") }
    var pinImaGresku by remember { mutableStateOf(false) }
    var cuvanjeUToku by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scanner = remember { GmsBarcodeScanning.getClient(context) }
    val moduleInstall = remember { ModuleInstall.getClient(context) }
    var skenerSePriprema by remember { mutableStateOf(false) }
    var aktivnaInstalacijaSkenera by remember { mutableStateOf<ScannerInstallSession?>(null) }
    val zivotniVijekEkrana = remember { ScannerInstallSession(onUnregister = {}) }
    val activityWindow = context.findActivity()?.window
    DisposableEffect(activityWindow) {
        val secureFlag = WindowManager.LayoutParams.FLAG_SECURE
        val secureFlagJeVecBioPostavljen =
            activityWindow?.attributes?.flags?.and(secureFlag) != 0
        activityWindow?.addFlags(secureFlag)
        onDispose {
            if (!secureFlagJeVecBioPostavljen) {
                activityWindow.clearFlags(secureFlag)
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            zivotniVijekEkrana.dispose()
            aktivnaInstalacijaSkenera?.dispose()
        }
    }
    fun pokreniSkeniranje() {
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                if (!zivotniVijekEkrana.isActive) return@addOnSuccessListener
                barcode.rawValue?.let { skeniraniKod ->
                    barCodeSifra = normalizujQrPayload(skeniraniKod)
                }
            }
            .addOnFailureListener {
                if (!zivotniVijekEkrana.isActive) return@addOnFailureListener
                Toast.makeText(context, "Skener nije dostupan. Pokušaj ponovo ili učitaj iz galerije.", Toast.LENGTH_LONG).show()
            }
    }
    fun skenirajIliPripremiSkener() {
        if (skenerSePriprema || !zivotniVijekEkrana.isActive) return
        skenerSePriprema = true
        var statusListener: InstallStatusListener? = null
        lateinit var sesija: ScannerInstallSession
        sesija = ScannerInstallSession(
            onUnregister = {
                statusListener?.let(moduleInstall::unregisterListener)
            }
        )
        aktivnaInstalacijaSkenera?.dispose()
        aktivnaInstalacijaSkenera = sesija

        fun zavrsiSesiju(akcija: () -> Unit) {
            sesija.finish {
                if (aktivnaInstalacijaSkenera === sesija) {
                    aktivnaInstalacijaSkenera = null
                }
                skenerSePriprema = false
                if (zivotniVijekEkrana.isActive) akcija()
            }
        }

        val prijaviGreskuPripreme = {
            zavrsiSesiju {
                Toast.makeText(context, "Skener se ne može preuzeti. Provjeri internet konekciju ili učitaj iz galerije.", Toast.LENGTH_LONG).show()
            }
        }

        moduleInstall.areModulesAvailable(scanner)
            .addOnSuccessListener { rezultat ->
                if (!sesija.isActive) return@addOnSuccessListener
                if (rezultat.areModulesAvailable()) {
                    zavrsiSesiju { pokreniSkeniranje() }
                } else {
                    Toast.makeText(context, "Preuzimanje skenera, ovo može potrajati nekoliko sekundi…", Toast.LENGTH_LONG).show()
                    val listener = object : InstallStatusListener {
                        override fun onInstallStatusUpdated(status: ModuleInstallStatusUpdate) {
                            when (status.installState) {
                                ModuleInstallStatusUpdate.InstallState.STATE_COMPLETED -> {
                                    zavrsiSesiju { pokreniSkeniranje() }
                                }
                                ModuleInstallStatusUpdate.InstallState.STATE_FAILED,
                                ModuleInstallStatusUpdate.InstallState.STATE_CANCELED -> {
                                    prijaviGreskuPripreme()
                                }
                            }
                        }
                    }
                    statusListener = listener
                    val zahtjev = ModuleInstallRequest.newBuilder()
                        .addApi(scanner)
                        .setListener(listener)
                        .build()
                    moduleInstall.installModules(zahtjev)
                        .addOnSuccessListener { odgovor ->
                            if (odgovor.areModulesAlreadyInstalled()) {
                                zavrsiSesiju { pokreniSkeniranje() }
                            }
                        }
                        .addOnFailureListener {
                            prijaviGreskuPripreme()
                        }
                }
            }
            .addOnFailureListener {
                zavrsiSesiju { pokreniSkeniranje() }
            }
    }
    val slikaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { odabraniUri ->
            try {
                val image = InputImage.fromFilePath(context, odabraniUri)
                val lokalniScanner = BarcodeScanning.getClient()
                lokalniScanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        if (!zivotniVijekEkrana.isActive) return@addOnSuccessListener
                        if (barcodes.isNotEmpty()) {
                            barcodes.first().rawValue?.let { skeniraniTekst ->
                                barCodeSifra = normalizujQrPayload(skeniraniTekst)
                                Toast.makeText(context, "QR kod uspješno učitan!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Nije pronađen QR kod na slici!", Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener {
                        if (!zivotniVijekEkrana.isActive) return@addOnFailureListener
                        Toast.makeText(context, "Greška prilikom analize slike!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {
                        lokalniScanner.close()
                    }
            } catch (e: Exception) {
                Toast.makeText(context, "Neuspješno otvaranje slike!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(38.dp))
        Icon(
            painter = painterResource(id = R.drawable.icon_view_in_ar),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MainColor
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "PAKETOMAT ALATI",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = SecondaryColor,
            modifier = Modifier.padding(bottom = 28.dp)
        )
        val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MainColor,
            unfocusedBorderColor = SecondaryColor,
            focusedLabelColor = Color.Gray,
            unfocusedLabelColor = Color.Gray,
            cursorColor = SecondaryColor,
            focusedTextColor = SecondaryColor,
            unfocusedTextColor = SecondaryColor
        )
        Text(text = "Unesi svoje identifikacione podatke za pristup paketomatu.", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = imeRadnika,
            onValueChange = { noviUnos ->
                val bezNovogReda = noviUnos.replace("\n", "")
                if (bezNovogReda.length <= 30){
                    imeRadnika = bezNovogReda
                }
            },
            label = { Text("Ime i prezime") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = fieldColors
        )
        OutlinedTextField(
            value = barCodeSifra,
            onValueChange = { barCodeSifra = normalizujQrPayload(it) },
            label = { Text("QR šifra paketomata") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            singleLine = true,
            colors = fieldColors,
            trailingIcon = {
                Row(
                    modifier = Modifier.padding(end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        slikaLauncher.launch("image/*")
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_insert_photo),
                            contentDescription = "Učitaj iz galerije.",
                            tint = SecondaryColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    IconButton(
                        onClick = { skenirajIliPripremiSkener() },
                        enabled = !skenerSePriprema
                    ) {
                        if (skenerSePriprema) {
                            CircularProgressIndicator(
                                color = SecondaryColor,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(28.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_qr_code_scanner),
                                contentDescription = "Skeniraj QR kod.",
                                tint = SecondaryColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }
        )
        OutlinedTextField(
            value = sifraRadnika,
            onValueChange = { noviUnos ->
                when {
                    noviUnos.isEmpty() -> {
                        sifraRadnika = ""
                        pinImaGresku = false
                    }
                    pinJeValjan(noviUnos) -> {
                        sifraRadnika = noviUnos
                        pinImaGresku = false
                    }
                    else -> {
                        pinImaGresku = true
                    }
                }
            },
            label = { Text("Šifra") },
            visualTransformation = VisualTransformation.None,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pin_field"),
            singleLine = true,
            isError = pinImaGresku,
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (pinImaGresku) {
                Text(
                    text = "Šifra može sadržati samo cifre 0–9.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }
        }
        Button(
            onClick = {
                if (
                    imeRadnika.isNotBlank() &&
                    barCodeSifra.isNotBlank() &&
                    pinJeValjan(sifraRadnika) &&
                    !pinImaGresku
                ) {
                    cuvanjeUToku = true
                    coroutineScope.launch {
                        try {
                            when (
                                userPreferences.sacuvajPodatke(
                                    ime = imeRadnika,
                                    barCode = barCodeSifra,
                                    sifra = sifraRadnika
                                )
                            ) {
                                RezultatOperacijePodataka.Uspjeh -> Unit
                                is RezultatOperacijePodataka.Greska -> {
                                    Toast.makeText(
                                        context,
                                        "Podaci se ne mogu sačuvati. Pokušaj ponovo.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        } finally {
                            cuvanjeUToku = false
                        }
                    }
                } else {
                    Toast.makeText(context, "Unesi sve podatke!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            enabled = !cuvanjeUToku,
            colors = ButtonDefaults.buttonColors(
                containerColor = MainColor,
                contentColor = SecondaryColor
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            if (cuvanjeUToku) {
                CircularProgressIndicator(
                    color = SecondaryColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "PRIJAVI SE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
