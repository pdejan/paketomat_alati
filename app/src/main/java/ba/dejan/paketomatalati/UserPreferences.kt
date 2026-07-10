package ba.dejan.paketomatalati

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.InvalidKeyException
import java.security.ProviderException
import java.security.UnrecoverableKeyException
import java.util.concurrent.CancellationException
import javax.crypto.BadPaddingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen

val radnikPreferencesCorruptionHandler = ReplaceFileCorruptionHandler {
    emptyPreferences()
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "radnik_prefs",
    corruptionHandler = radnikPreferencesCorruptionHandler
)

data class RadnikData(
    val ime: String,
    val barCodeSifra: String,
    val sifraRadnika: String
)
sealed interface RadnikStanje {
    data object Ucitavanje : RadnikStanje
    data object Odjavljen : RadnikStanje
    /** Privremena greška čitanja; postojeći podaci se ne brišu i čitanje se ponavlja. */
    data object GreskaCitanja : RadnikStanje
    /** Postoje sačuvani podaci, ali ih nije moguće dekriptovati (npr. ključ iz Keystore-a
     *  je nevažeći). Tretira se kao odjava uz čišćenje podataka i obavještenje korisnika. */
    data object GreskaPodataka : RadnikStanje
    data class Ulogovan(val data: RadnikData) : RadnikStanje
}

sealed interface RezultatOperacijePodataka {
    data object Uspjeh : RezultatOperacijePodataka
    data class Greska(val uzrok: Throwable) : RezultatOperacijePodataka
}

fun s10ValidacijaUkljucena(preferences: Preferences): Boolean =
    preferences[UserPreferences.S10_VALIDACIJA_UKLJUCENA] ?: true

fun MutablePreferences.obrisiPodatkeRadnika() {
    remove(UserPreferences.IME_RADNIKA)
    remove(UserPreferences.BAR_CODE_SIFRA)
    remove(UserPreferences.SIFRA_RADNIKA)
}

fun klasifikujGreskuCitanja(greska: Throwable): RadnikStanje =
    when (greska) {
        is CorruptionException -> RadnikStanje.GreskaPodataka
        is IOException,
        is ProviderException,
        is GeneralSecurityException -> RadnikStanje.GreskaCitanja
        else -> throw greska
    }

fun klasifikujGreskuDekripcije(greska: Throwable): RadnikStanje =
    when (greska) {
        is BadPaddingException,
        is InvalidKeyException,
        is UnrecoverableKeyException,
        is IllegalArgumentException,
        is IndexOutOfBoundsException -> RadnikStanje.GreskaPodataka
        is IOException,
        is ProviderException,
        is GeneralSecurityException -> RadnikStanje.GreskaCitanja
        else -> throw greska
    }

suspend fun izvrsiOperacijuPodataka(
    operacija: suspend () -> Unit
): RezultatOperacijePodataka =
    try {
        operacija()
        RezultatOperacijePodataka.Uspjeh
    } catch (otkazivanje: CancellationException) {
        throw otkazivanje
    } catch (greska: IOException) {
        RezultatOperacijePodataka.Greska(greska)
    } catch (greska: GeneralSecurityException) {
        RezultatOperacijePodataka.Greska(greska)
    } catch (greska: ProviderException) {
        RezultatOperacijePodataka.Greska(greska)
    }

class UserPreferences(private val context: Context) {
    companion object {
        val IME_RADNIKA = stringPreferencesKey("ime_radnika")
        val BAR_CODE_SIFRA = stringPreferencesKey("bar_code_sifra")
        val SIFRA_RADNIKA = stringPreferencesKey("sifra_radnika")
        val S10_VALIDACIJA_UKLJUCENA = booleanPreferencesKey("s10_validacija_ukljucena")
    }
    val s10ValidacijaUkljucenaFlow: Flow<Boolean> = context.dataStore.data
        .map(::s10ValidacijaUkljucena)
        .retryWhen { greska, pokusaj ->
            if (klasifikujGreskuCitanja(greska) == RadnikStanje.GreskaCitanja) {
                delay(((pokusaj + 1).coerceAtMost(6)) * 5_000L)
                true
            } else {
                false
            }
        }
        .catch { greska ->
            if (klasifikujGreskuCitanja(greska) == RadnikStanje.GreskaPodataka) {
                emit(true)
            } else {
                throw greska
            }
        }
        .flowOn(Dispatchers.Default)

    val radnikStanjeFlow: Flow<RadnikStanje> = context.dataStore.data
        .map { preferences ->
            val ime = preferences[IME_RADNIKA]
            val barCode = preferences[BAR_CODE_SIFRA]
            val sifra = preferences[SIFRA_RADNIKA]

            if (ime == null || barCode == null || sifra == null) {
                RadnikStanje.Odjavljen
            } else {
                try {
                    RadnikStanje.Ulogovan(
                        RadnikData(ime, CryptoManager.decrypt(barCode), CryptoManager.decrypt(sifra))
                    )
                } catch (otkazivanje: CancellationException) {
                    throw otkazivanje
                } catch (greska: Exception) {
                    when (val stanjeGreske = klasifikujGreskuDekripcije(greska)) {
                        RadnikStanje.GreskaPodataka -> stanjeGreske
                        RadnikStanje.GreskaCitanja -> throw greska
                        else -> throw greska
                    }
                }
            }
        }
        .retryWhen { greska, pokusaj ->
            val stanjeGreske = klasifikujGreskuCitanja(greska)
            if (stanjeGreske == RadnikStanje.GreskaCitanja) {
                emit(stanjeGreske)
                delay(((pokusaj + 1).coerceAtMost(6)) * 5_000L)
                true
            } else {
                false
            }
        }
        .catch { greska -> emit(klasifikujGreskuCitanja(greska)) }
        .flowOn(Dispatchers.Default)

    suspend fun sacuvajPodatke(
        ime: String,
        barCode: String,
        sifra: String
    ): RezultatOperacijePodataka = izvrsiOperacijuPodataka {
        context.dataStore.edit { preferences ->
            preferences[IME_RADNIKA] = ime
            preferences[BAR_CODE_SIFRA] = CryptoManager.encrypt(barCode)
            preferences[SIFRA_RADNIKA] = CryptoManager.encrypt(sifra)
        }
    }

    suspend fun obrisiPodatke(): RezultatOperacijePodataka = izvrsiOperacijuPodataka {
        context.dataStore.edit { preferences ->
            preferences.obrisiPodatkeRadnika()
        }
    }

    suspend fun postaviS10Validaciju(
        ukljucena: Boolean
    ): RezultatOperacijePodataka = izvrsiOperacijuPodataka {
        context.dataStore.edit { preferences ->
            preferences[S10_VALIDACIJA_UKLJUCENA] = ukljucena
        }
    }
}
