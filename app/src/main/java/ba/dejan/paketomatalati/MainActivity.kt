package ba.dejan.paketomatalati

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
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

class MainActivity : FragmentActivity() {
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
                        is RadnikStanje.GreskaCitanja -> GreskaCitanjaScreen()
                        is RadnikStanje.GreskaPodataka -> {
                            val context = LocalContext.current
                            LaunchedEffect(Unit) {
                                Toast.makeText(
                                    context,
                                    "Sačuvani podaci se ne mogu pročitati. Unesi podatke ponovo.",
                                    Toast.LENGTH_LONG
                                ).show()
                                if (
                                    userPreferences.obrisiPodatke() is
                                    RezultatOperacijePodataka.Greska
                                ) {
                                    Toast.makeText(
                                        context,
                                        "Podaci se trenutno ne mogu obrisati. Pokušaj ponovo.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
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
private fun GreskaCitanjaScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = MainColor)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Sačuvani podaci se trenutno ne mogu učitati.",
            color = SecondaryColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Aplikacija će pokušati ponovo bez brisanja podataka.",
            color = Color.Gray,
            fontSize = 14.sp
        )
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
private enum class Ekran { Glavni, Postavke }
@Composable
fun UlogovaniInterfejs(radnikData: RadnikData, userPreferences: UserPreferences) {
    var trenutniEkran by remember { mutableStateOf(Ekran.Glavni) }
    val s10ValidacijaUkljucena by userPreferences.s10ValidacijaUkljucenaFlow.collectAsState(
        initial = true
    )
    BackHandler(enabled = trenutniEkran == Ekran.Postavke) {
        trenutniEkran = Ekran.Glavni
    }
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
                trenutniEkran = if (trenutniEkran == Ekran.Glavni) Ekran.Postavke else Ekran.Glavni
            }) {
                Icon(
                    painter = if (trenutniEkran == Ekran.Glavni) painterResource(id = R.drawable.icon_settings) else painterResource(id = R.drawable.icon_arrow_back),
                    contentDescription = if (trenutniEkran == Ekran.Glavni) {
                        "Otvori postavke"
                    } else {
                        "Nazad na glavni ekran"
                    },
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            when (trenutniEkran) {
                Ekran.Glavni -> MainScreen(
                    radnikData = radnikData,
                    s10ValidacijaUkljucena = s10ValidacijaUkljucena
                )
                Ekran.Postavke -> SettingsScreen(
                    radnikData = radnikData,
                    userPreferences = userPreferences,
                    s10ValidacijaUkljucena = s10ValidacijaUkljucena
                )
            }
        }
    }
}
