package ba.dejan.paketomatalati.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Aplikacija ima namjenski svijetli izgled sa fiksnim brend bojama.
// Statička light šema izvedena iz brend boja osigurava da i Material default
// komponente (kursor, selekcija teksta, scrim dijaloga, podrazumijevani tekst)
// prate isti izgled, nezavisno od dark/light moda ili dynamic color sistema.
private val AppColorScheme = lightColorScheme(
    primary = MainColor,
    onPrimary = SecondaryColor,
    secondary = SecondaryColor,
    onSecondary = Background,
    background = Background,
    onBackground = SecondaryColor,
    surface = Color.White,
    onSurface = SecondaryColor
)

@Composable
fun PaketomatAlatiTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}