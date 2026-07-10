package ba.dejan.paketomatalati

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import ba.dejan.paketomatalati.ui.theme.PaketomatAlatiTheme
import org.junit.Rule
import org.junit.Test

class CredentialUiInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun neispravanPinNeMijenjaPosljednjiValjanUnosIPrikazujeGresku() {
        composeRule.setContent {
            val context = LocalContext.current
            PaketomatAlatiTheme {
                LoginScreen(UserPreferences(context))
            }
        }
        composeRule.waitForIdle()

        val pinPolje = composeRule.onNodeWithTag("pin_field")
        pinPolje.performScrollTo()
        pinPolje.performTextReplacement("12")
        pinPolje.assertTextContains("12")

        pinPolje.performTextReplacement("12O4")

        pinPolje.assertTextContains("12")
        composeRule.onNodeWithText("Šifra može sadržati samo cifre 0–9.")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
