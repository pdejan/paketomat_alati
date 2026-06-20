package ba.dejan.paketomatalati

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import ba.dejan.paketomatalati.ui.theme.PaketomatAlatiTheme
import org.junit.Rule
import org.junit.Test

class MainScreenInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun generisanjeJeOmogucenoSamoZaPotpunoValjanS10() {
        composeRule.setContent {
            PaketomatAlatiTheme {
                MainScreen(RadnikData("Test", "qr", "1234"))
            }
        }

        val polje = composeRule.onNode(hasSetTextAction())
        val dugme = composeRule.onNodeWithText("GENERIŠI BARKOD")

        dugme.assertIsNotEnabled()
        polje.performTextInput("RR123456784BA")
        dugme.assertIsNotEnabled()

        polje.performTextClearance()
        polje.performTextInput("RR123456785BA")
        dugme.assertIsEnabled()
    }
}
