package ba.dejan.paketomatalati

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ba.dejan.paketomatalati.ui.theme.PaketomatAlatiTheme
import org.junit.Rule
import org.junit.Test

class UlogovaniInterfejsInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun sistemskoNazadIzPostavkiVracaNaGlavniEkran() {
        composeRule.setContent {
            val context = LocalContext.current
            PaketomatAlatiTheme {
                UlogovaniInterfejs(
                    radnikData = RadnikData("Test Radnik", "qr", "1234"),
                    userPreferences = UserPreferences(context)
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Otvori postavke").performClick()
        composeRule.onNodeWithText("Prijavljeni radnik").assertIsDisplayed()
        composeRule.onNodeWithText("UPU S10 validacija").assertIsDisplayed()

        composeRule.activity.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("GENERIŠI BARKOD").assertIsDisplayed()
    }
}
