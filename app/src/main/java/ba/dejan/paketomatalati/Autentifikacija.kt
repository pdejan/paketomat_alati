package ba.dejan.paketomatalati

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class IshodTerminalneGreskeAutentifikacije {
    ZADRZI_ZATVORENO,
    PROPUSTI_KORISNIKA
}

enum class DostupnostAutentifikacije {
    DOSTUPNA,
    NEDOSTUPNA
}

fun provjeriDostupnostAutentifikacije(
    provjera: () -> Boolean
): DostupnostAutentifikacije =
    try {
        if (provjera()) {
            DostupnostAutentifikacije.DOSTUPNA
        } else {
            DostupnostAutentifikacije.NEDOSTUPNA
        }
    } catch (greska: Exception) {
        DostupnostAutentifikacije.NEDOSTUPNA
    }

fun klasifikujTerminalnuGreskuAutentifikacije(
    kodGreske: Int
): IshodTerminalneGreskeAutentifikacije =
    when (kodGreske) {
        BiometricPrompt.ERROR_USER_CANCELED,
        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
        BiometricPrompt.ERROR_CANCELED ->
            IshodTerminalneGreskeAutentifikacije.ZADRZI_ZATVORENO
        else -> IshodTerminalneGreskeAutentifikacije.PROPUSTI_KORISNIKA
    }

/**
 * Traži potvrdu identiteta (otisak/lice ili PIN/šablon/lozinka uređaja) prije prikaza
 * osjetljivih podataka (prijava na paketomat).
 *
 * Ako uređaj nema postavljeno otključavanje, ili autentifikacija iz bilo kojeg razloga
 * nije dostupna, NE zaključavamo korisnika.
 * Auth je dodatni sloj kad ga uređaj podržava, a ne preduslov za rad aplikacije.
 */
fun zatraziAutentifikaciju(activity: FragmentActivity, onUspjeh: () -> Unit) {
    val dozvoljeno = Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL

    val dostupnost = provjeriDostupnostAutentifikacije {
        BiometricManager.from(activity)
            .canAuthenticate(dozvoljeno) == BiometricManager.BIOMETRIC_SUCCESS
    }

    if (dostupnost == DostupnostAutentifikacije.NEDOSTUPNA) {
        onUspjeh()
        return
    }
    try {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Potvrdi identitet")
            .setSubtitle("Otključaj za prikaz prijave na paketomat.")
            .setAllowedAuthenticators(dozvoljeno)
            .build()
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onUspjeh()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (
                        klasifikujTerminalnuGreskuAutentifikacije(errorCode) ==
                        IshodTerminalneGreskeAutentifikacije.PROPUSTI_KORISNIKA
                    ) {
                        onUspjeh()
                    }
                }
            }
        )
        prompt.authenticate(promptInfo)
    } catch (e: Exception) {
        onUspjeh()
    }
}
