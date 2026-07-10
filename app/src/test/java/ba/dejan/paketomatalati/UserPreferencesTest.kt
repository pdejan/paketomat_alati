package ba.dejan.paketomatalati

import androidx.datastore.preferences.core.mutablePreferencesOf
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.ProviderException
import java.util.concurrent.CancellationException
import javax.crypto.AEADBadTagException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertSame
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class UserPreferencesTest {

    @Test
    fun s10Validacija_bezSacuvaneVrijednostiJeUkljucena() {
        assertTrue(s10ValidacijaUkljucena(mutablePreferencesOf()))
    }

    @Test
    fun s10Validacija_postujeSacuvanuIskljucenuVrijednost() {
        val preferences = mutablePreferencesOf()
        preferences[UserPreferences.S10_VALIDACIJA_UKLJUCENA] = false

        assertFalse(s10ValidacijaUkljucena(preferences))
    }

    @Test
    fun brisanjeRadnika_cuvaPostavkuS10Validacije() {
        val preferences = mutablePreferencesOf()
        preferences[UserPreferences.IME_RADNIKA] = "Test Radnik"
        preferences[UserPreferences.BAR_CODE_SIFRA] = "kriptovani-qr"
        preferences[UserPreferences.SIFRA_RADNIKA] = "kriptovani-pin"
        preferences[UserPreferences.S10_VALIDACIJA_UKLJUCENA] = false

        preferences.obrisiPodatkeRadnika()

        assertNull(preferences[UserPreferences.IME_RADNIKA])
        assertNull(preferences[UserPreferences.BAR_CODE_SIFRA])
        assertNull(preferences[UserPreferences.SIFRA_RADNIKA])
        assertFalse(preferences[UserPreferences.S10_VALIDACIJA_UKLJUCENA] ?: true)
    }

    @Test
    fun greskaDekripcije_ioGreskaJeNedestruktivna() {
        assertSame(
            RadnikStanje.GreskaCitanja,
            klasifikujGreskuDekripcije(IOException("Keystore privremeno nedostupan"))
        )
    }

    @Test
    fun greskaDekripcije_providerGreskaJeNedestruktivna() {
        assertSame(
            RadnikStanje.GreskaCitanja,
            klasifikujGreskuDekripcije(ProviderException("provider nedostupan"))
        )
    }

    @Test
    fun greskaDekripcije_neispravanAutentifikacioniTagJeOstecenPodatak() {
        assertSame(
            RadnikStanje.GreskaPodataka,
            klasifikujGreskuDekripcije(AEADBadTagException("tag nije valjan"))
        )
    }

    @Test
    fun greskaDekripcije_neispravanCiphertextJeOstecenPodatak() {
        assertSame(
            RadnikStanje.GreskaPodataka,
            klasifikujGreskuDekripcije(IllegalArgumentException("neispravan Base64"))
        )
    }

    @Test
    fun greskaDekripcije_programskaGreskaSeNeGuta() {
        val greska = IllegalStateException("programska greska")

        val bacena = assertThrows(IllegalStateException::class.java) {
            klasifikujGreskuDekripcije(greska)
        }

        assertSame(greska, bacena)
    }

    @Test
    fun greskaCitanja_ioGreskaPostajeNedestruktivnoStanje() {
        assertSame(
            RadnikStanje.GreskaCitanja,
            klasifikujGreskuCitanja(IOException("privremeno nedostupno"))
        )
    }

    @Test
    fun greskaCitanja_neocekivanaGreskaSeNeGuta() {
        val greska = IllegalStateException("programska greska")

        val bacena = assertThrows(IllegalStateException::class.java) {
            klasifikujGreskuCitanja(greska)
        }

        assertSame(greska, bacena)
    }

    @Test
    fun operacijaPodataka_uspjehVracaEksplicitanRezultat() = runBlocking {
        assertSame(
            RezultatOperacijePodataka.Uspjeh,
            izvrsiOperacijuPodataka { }
        )
    }

    @Test
    fun operacijaPodataka_ioGreskaVracaEksplicitanNeuspjeh() = runBlocking {
        val greska = IOException("upis nije uspio")

        val rezultat = izvrsiOperacijuPodataka { throw greska }

        assertSame(greska, (rezultat as RezultatOperacijePodataka.Greska).uzrok)
    }

    @Test
    fun operacijaPodataka_kriptografskaGreskaVracaEksplicitanNeuspjeh() = runBlocking {
        val greska = GeneralSecurityException("Keystore nije dostupan")

        val rezultat = izvrsiOperacijuPodataka { throw greska }

        assertSame(greska, (rezultat as RezultatOperacijePodataka.Greska).uzrok)
    }

    @Test
    fun operacijaPodataka_greskaSecurityProvideraVracaEksplicitanNeuspjeh() = runBlocking {
        val greska = ProviderException("AndroidKeyStore provider nije dostupan")

        val rezultat = izvrsiOperacijuPodataka { throw greska }

        assertSame(greska, (rezultat as RezultatOperacijePodataka.Greska).uzrok)
    }

    @Test
    fun operacijaPodataka_otkazivanjeCoroutineSeNePretvaraUGresku() {
        val otkazivanje = CancellationException("otkazano")

        val bacena = assertThrows(CancellationException::class.java) {
            runBlocking {
                izvrsiOperacijuPodataka { throw otkazivanje }
            }
        }

        assertSame(otkazivanje, bacena)
    }
}
