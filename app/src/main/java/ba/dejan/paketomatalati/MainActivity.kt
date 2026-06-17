package ba.dejan.paketomatalati

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ba.dejan.paketomatalati.ui.theme.PaketomatAlatiTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import ba.dejan.paketomatalati.ui.theme.Background
import ba.dejan.paketomatalati.ui.theme.MainColor
import ba.dejan.paketomatalati.ui.theme.SecondaryColor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userPreferences = UserPreferences(this)
        setContent {
            PaketomatAlatiTheme {
                val stanje by userPreferences.radnikStanjeFlow.collectAsState(initial = RadnikStanje.Ucitavanje)
                Surface(color = Background) {
                    when (val trenutnoStanje = stanje) {
                        is RadnikStanje.Ucitavanje -> SplashScreen()
                        is RadnikStanje.Odjavljen -> LoginScreen(userPreferences = userPreferences)
                        is RadnikStanje.GreskaPodataka -> {
                            val context = LocalContext.current
                            LaunchedEffect(Unit) {
                                Toast.makeText(
                                    context,
                                    "Sačuvani podaci se ne mogu pročitati. Unesi podatke ponovo.",
                                    Toast.LENGTH_LONG
                                ).show()
                                userPreferences.obrisiPodatke()
                            }
                            LoginScreen(userPreferences = userPreferences)
                        }
                        is RadnikStanje.Ulogovan -> UlogovaniInterfejs(
                            radnikData = trenutnoStanje.data,
                            userPreferences = userPreferences
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.icon_view_in_ar),
            contentDescription = null,
            tint = MainColor,
            modifier = Modifier.size(100.dp)
        )
    }
}
@Composable
fun UlogovaniInterfejs(radnikData: RadnikData, userPreferences: UserPreferences) {
    var trenutniEkran by remember { mutableStateOf(0) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_view_in_ar),
                    contentDescription = "Paketomat Alati Logo",
                    tint = MainColor,
                    modifier = Modifier.size(42.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "PAKETOMAT ALATI",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryColor
                )
            }
            IconButton(onClick = {
                trenutniEkran = if (trenutniEkran == 0) 1 else 0
            }) {
                Icon(
                    painter = if (trenutniEkran == 0) painterResource(id = R.drawable.icon_settings) else painterResource(id = R.drawable.icon_arrow_back),
                    contentDescription = "Navigacija",
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            when (trenutniEkran) {
                0 -> MainScreen(radnikData = radnikData)
                1 -> SettingsScreen(radnikData = radnikData, userPreferences = userPreferences)
            }
        }
    }
}