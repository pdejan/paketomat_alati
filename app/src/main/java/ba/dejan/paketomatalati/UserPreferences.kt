package ba.dejan.paketomatalati

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "radnik_prefs")

data class RadnikData(
    val ime: String,
    val barCodeSifra: String,
    val sifraRadnika: String
)

/** Stanje prijave: dok se DataStore učitava ([Ucitavanje]) ne znamo još da li je radnik prijavljen. */
sealed interface RadnikStanje {
    data object Ucitavanje : RadnikStanje
    data object Odjavljen : RadnikStanje
    data class Ulogovan(val data: RadnikData) : RadnikStanje
}
class UserPreferences(private val context: Context) {
    companion object {
        val IME_RADNIKA = stringPreferencesKey("ime_radnika")
        val BAR_CODE_SIFRA = stringPreferencesKey("bar_code_sifra")
        val SIFRA_RADNIKA = stringPreferencesKey("sifra_radnika")
    }
    val radnikDataFlow: Flow<RadnikData?> = context.dataStore.data.map { preferences ->
        val ime = preferences[IME_RADNIKA]
        val barCode = preferences[BAR_CODE_SIFRA]
        val sifra = preferences[SIFRA_RADNIKA]

        if (ime != null && barCode != null && sifra != null) {
            try {
                RadnikData(ime, CryptoManager.decrypt(barCode), CryptoManager.decrypt(sifra))
            } catch (e: Exception) {
                // Šifrovani podaci se ne mogu pročitati (npr. nakon resetovanja
                // Keystore ključa) -> tretiraj kao da radnik nije prijavljen.
                null
            }
        } else {
            null
        }
    }
    val radnikStanjeFlow: Flow<RadnikStanje> = radnikDataFlow.map { data ->
        if (data != null) RadnikStanje.Ulogovan(data) else RadnikStanje.Odjavljen
    }
    suspend fun sacuvajPodatke(ime: String, barCode: String, sifra: String) {
        context.dataStore.edit { preferences ->
            preferences[IME_RADNIKA] = ime
            preferences[BAR_CODE_SIFRA] = CryptoManager.encrypt(barCode)
            preferences[SIFRA_RADNIKA] = CryptoManager.encrypt(sifra)
        }
    }
    suspend fun obrisiPodatke() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}