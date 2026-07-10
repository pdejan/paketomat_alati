package ba.dejan.paketomatalati

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import ba.dejan.paketomatalati.ui.theme.SecondaryColor
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    radnikData: RadnikData,
    userPreferences: UserPreferences,
    s10ValidacijaUkljucena: Boolean
) {
    val context = LocalContext.current
    val versionName = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            "1.0"
        }
    }
    val coroutineScope = rememberCoroutineScope()
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var prikazanaS10Validacija by remember(s10ValidacijaUkljucena) {
        mutableStateOf(s10ValidacijaUkljucena)
    }
    var cuvanjeS10Validacije by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Prijavljeni radnik",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ime i prezime: ${radnikData.ime}",
                    fontSize = 15.sp,
                    color = SecondaryColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = { showLogoutConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("ODJAVI SE", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Validacija barkoda",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (prikazanaS10Validacija) {
                        "Provjerava UPU S10 format i kontrolnu cifru."
                    } else {
                        "Dozvoljava 1-32 ASCII slova ili cifre."
                    },
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "UPU S10 validacija",
                        fontSize = 16.sp,
                        color = SecondaryColor,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = prikazanaS10Validacija,
                        enabled = !cuvanjeS10Validacije,
                        onCheckedChange = { novaVrijednost ->
                            prikazanaS10Validacija = novaVrijednost
                            cuvanjeS10Validacije = true
                            coroutineScope.launch {
                                when (
                                    userPreferences.postaviS10Validaciju(novaVrijednost)
                                ) {
                                    RezultatOperacijePodataka.Uspjeh -> Unit
                                    is RezultatOperacijePodataka.Greska -> {
                                        prikazanaS10Validacija = s10ValidacijaUkljucena
                                        Toast.makeText(
                                            context,
                                            "Postavka se ne može sačuvati. Pokušaj ponovo.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                                cuvanjeS10Validacije = false
                            }
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "UPU S10 validacija"
                        }
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PAKETOMAT ALATI v$versionName",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Text(
                text = "Razvoj: Dejan Popović",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            containerColor = Color.White,
            title = {
                Text(
                    "Odjava",
                    fontWeight = FontWeight.Bold,
                    color = SecondaryColor
                )
            },
            text = {
                Text(
                    "Odjava briše sve podatke korisnika. Nastavi?",
                    color = SecondaryColor,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        coroutineScope.launch {
                            when (userPreferences.obrisiPodatke()) {
                                RezultatOperacijePodataka.Uspjeh -> Unit
                                is RezultatOperacijePodataka.Greska -> {
                                    Toast.makeText(
                                        context,
                                        "Podaci se ne mogu obrisati. Pokušaj ponovo.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("ODJAVI SE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("ODUSTANI", color = Color.Gray)
                }
            }
        )
    }
}
