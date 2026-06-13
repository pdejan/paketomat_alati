package ba.dejan.paketomatalati

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import ba.dejan.paketomatalati.ui.theme.SecondaryColor
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    radnikData: RadnikData,
    userPreferences: UserPreferences
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
                    fontSize = 14.sp,
                    color = Color.Gray,
                //  modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Ime i prezime: ${radnikData.ime}",
                    fontSize = 16.sp,
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
                    )
                ) {
                    Text("ODJAVI SE", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                            userPreferences.obrisiPodatke()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
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
