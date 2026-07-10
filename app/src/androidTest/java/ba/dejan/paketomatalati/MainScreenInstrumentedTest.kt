package ba.dejan.paketomatalati

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
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
        composeRule.waitForIdle()

        val polje = composeRule.onNode(hasSetTextAction())
        val dugme = composeRule.onNodeWithText("GENERIŠI BARKOD")

        dugme.assertIsNotEnabled()
        polje.performTextInput("RR123456784BA")
        dugme.assertIsNotEnabled()

        polje.performTextClearance()
        polje.performTextInput("RR123456785BA")
        dugme.assertIsEnabled()
    }

    @Test
    fun generickiRezimPrihvataAlfanumerickiUnosBezBrojacaICheckmarka() {
        composeRule.setContent {
            PaketomatAlatiTheme {
                MainScreen(
                    radnikData = RadnikData("Test", "qr", "1234"),
                    s10ValidacijaUkljucena = false
                )
            }
        }
        composeRule.waitForIdle()

        val polje = composeRule.onNode(hasSetTextAction())
        val dugme = composeRule.onNodeWithText("GENERIŠI BARKOD")

        polje.performTextInput("aBc123")
        polje.assertTextEquals("aBc123")
        dugme.assertIsEnabled()
        composeRule.onAllNodesWithText("6/32").assertCountEquals(0)
        composeRule.onAllNodesWithContentDescription(
            "Ispravan broj pošiljke"
        ).assertCountEquals(0)

        polje.performTextInput("-")
        polje.assertTextEquals("aBc123")
        composeRule.onNodeWithText(
            "Dozvoljena su samo slova i brojevi, najviše 32 znaka."
        ).assertIsDisplayed()
    }
}
