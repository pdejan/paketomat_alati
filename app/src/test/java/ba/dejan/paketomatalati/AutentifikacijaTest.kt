package ba.dejan.paketomatalati

import androidx.biometric.BiometricPrompt
import org.junit.Assert.assertEquals
import org.junit.Test

class AutentifikacijaTest {

    @Test
    fun `uspjesna provjera oznacava autentifikaciju dostupnom`() {
        assertEquals(
            DostupnostAutentifikacije.DOSTUPNA,
            provjeriDostupnostAutentifikacije { true }
        )
    }

    @Test
    fun `izuzetak provjere oznacava autentifikaciju nedostupnom`() {
        assertEquals(
            DostupnostAutentifikacije.NEDOSTUPNA,
            provjeriDostupnostAutentifikacije {
                throw IllegalStateException("vendor provjera nije dostupna")
            }
        )
    }

    @Test
    fun `korisnicko odustajanje zadrzava osjetljive podatke zatvorenim`() {
        assertEquals(
            IshodTerminalneGreskeAutentifikacije.ZADRZI_ZATVORENO,
            klasifikujTerminalnuGreskuAutentifikacije(BiometricPrompt.ERROR_USER_CANCELED)
        )
    }

    @Test
    fun `negativno dugme zadrzava osjetljive podatke zatvorenim`() {
        assertEquals(
            IshodTerminalneGreskeAutentifikacije.ZADRZI_ZATVORENO,
            klasifikujTerminalnuGreskuAutentifikacije(BiometricPrompt.ERROR_NEGATIVE_BUTTON)
        )
    }

    @Test
    fun `framework otkazivanje zadrzava osjetljive podatke zatvorenim`() {
        assertEquals(
            IshodTerminalneGreskeAutentifikacije.ZADRZI_ZATVORENO,
            klasifikujTerminalnuGreskuAutentifikacije(BiometricPrompt.ERROR_CANCELED)
        )
    }

    @Test
    fun `operativne terminalne greske propustaju korisnika`() {
        val operativneGreske = listOf(
            BiometricPrompt.ERROR_HW_UNAVAILABLE,
            BiometricPrompt.ERROR_UNABLE_TO_PROCESS,
            BiometricPrompt.ERROR_TIMEOUT,
            BiometricPrompt.ERROR_NO_SPACE,
            BiometricPrompt.ERROR_LOCKOUT,
            BiometricPrompt.ERROR_VENDOR,
            BiometricPrompt.ERROR_LOCKOUT_PERMANENT,
            BiometricPrompt.ERROR_NO_BIOMETRICS,
            BiometricPrompt.ERROR_HW_NOT_PRESENT,
            BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL,
            BiometricPrompt.ERROR_SECURITY_UPDATE_REQUIRED
        )

        operativneGreske.forEach { kodGreske ->
            assertEquals(
                "Kod greske $kodGreske mora pratiti fail-open politiku",
                IshodTerminalneGreskeAutentifikacije.PROPUSTI_KORISNIKA,
                klasifikujTerminalnuGreskuAutentifikacije(kodGreske)
            )
        }
    }

    @Test
    fun `nepoznata terminalna greska prati fail-open politiku`() {
        assertEquals(
            IshodTerminalneGreskeAutentifikacije.PROPUSTI_KORISNIKA,
            klasifikujTerminalnuGreskuAutentifikacije(Int.MAX_VALUE)
        )
    }
}
