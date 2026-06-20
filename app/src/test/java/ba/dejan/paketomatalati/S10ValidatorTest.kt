package ba.dejan.paketomatalati

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class S10ValidatorTest {

    @Test
    fun izgledaKaoS10_prihvataIspravanFormat() {
        assertTrue(izgledaKaoS10("RR123456785BA"))
    }

    @Test
    fun izgledaKaoS10_odbijaPogresanFormat() {
        assertFalse(izgledaKaoS10(""))
        assertFalse(izgledaKaoS10("ABC123"))
        assertFalse(izgledaKaoS10("RR12345678BA"))   // samo 8 cifara
        assertFalse(izgledaKaoS10("R1123456785BA"))  // cifra u oznaci usluge
        assertFalse(izgledaKaoS10("rr123456785ba"))  // mala slova
    }

    @Test
    fun potpunaValidacija_prihvataSamoIspravanS10() {
        assertTrue(s10BrojValjan("RR123456785BA"))

        listOf(
            "RR12345678BA",   // 12 znakova
            "RR123456785B",   // nedostaje završno slovo
            "R1123456785BA",  // cifra u oznaci usluge
            "RR12345A785BA",  // slovo u numeričkom dijelu
            "RR123456784BA"   // pogrešna kontrolna cifra
        ).forEach { broj ->
            assertFalse("Očekivano je da $broj bude odbijen", s10BrojValjan(broj))
        }
    }

    @Test
    fun kontrolnaCifra_valjanaZaIspravanBroj() {
        // serijski 12345678 -> kontrolna 5
        assertTrue(s10KontrolnaCifraValjana("RR123456785BA"))
    }

    @Test
    fun kontrolnaCifra_hvataTipfeler() {
        // ista pošiljka sa pogrešnom kontrolnom cifrom (4 umjesto 5)
        assertFalse(s10KontrolnaCifraValjana("RR123456784BA"))
    }

    @Test
    fun kontrolnaCifra_rubniSlucaj_ostatak0_dajePet() {
        // suma deljiva sa 11 -> 11 - 0 = 11 -> kontrolna 5
        assertTrue(s10KontrolnaCifraValjana("AA000000005AA"))
    }

    @Test
    fun kontrolnaCifra_rubniSlucaj_ostatak1_dajeNulu() {
        // serijski 70000000 -> suma 56, 56 % 11 = 1 -> 11 - 1 = 10 -> kontrolna 0
        assertTrue(s10KontrolnaCifraValjana("ZZ700000000ZZ"))
    }
}
