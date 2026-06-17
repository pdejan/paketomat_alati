package ba.dejan.paketomatalati

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "radnik_prefs")

data class RadnikData(
    val ime: String,
    val barCodeSifra: String,
    val sifraRadnika: String
)
sealed interface RadnikStanje {
    data object Ucitavanje : RadnikStanje
    data object Odjavljen : RadnikStanje
    /** Postoje sačuvani podaci, ali ih nije moguće dekriptovati (npr. ključ iz Keystore-a
     *  je nevažeći). Tretira se kao odjava uz čišćenje podataka i obavještenje korisnika. */
    data object GreskaPodataka : RadnikStanje
    data class Ulogovan(val data: RadnikData) : RadnikStanje
}
class UserPreferences(private val context: Context) {
    companion object {
        val IME_RADNIKA = stringPreferencesKey("ime_radnika")
        val BAR_CODE_SIFRA = stringPreferencesKey("bar_code_sifra")
        val SIFRA_RADNIKA = stringPreferencesKey("sifra_radnika")
    }
    val radnikStanjeFlow: Flow<RadnikStanje> = context.dataStore.data.map { preferences ->
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
            } catch (e: Exception) {
                RadnikStanje.GreskaPodataka
            }
        }
    }.flowOn(Dispatchers.Default) // dekripcija (Keystore) ne smije na glavni nit
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