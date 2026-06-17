package ba.dejan.paketomatalati

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

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

    val mozeAutentifikovati = BiometricManager.from(activity)
        .canAuthenticate(dozvoljeno) == BiometricManager.BIOMETRIC_SUCCESS

    if (!mozeAutentifikovati) {
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
            }
        )
        prompt.authenticate(promptInfo)
    } catch (e: Exception) {
        onUspjeh()
    }
}
