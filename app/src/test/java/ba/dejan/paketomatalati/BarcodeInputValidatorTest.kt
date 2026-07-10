package ba.dejan.paketomatalati

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BarcodeInputValidatorTest {

    @Test
    fun generickiUnos_prihvataJedanDoTridesetDvaAsciiSlovaICifre() {
        assertTrue(generickiBarkodUnosValjan("A"))
        assertTrue(generickiBarkodUnosValjan("aB12"))
        assertTrue(generickiBarkodUnosValjan("Ab12".repeat(8)))
    }

    @Test
    fun generickiUnos_odbijaPrazanIPredugacakUnos() {
        assertFalse(generickiBarkodUnosValjan(""))
        assertFalse(generickiBarkodUnosValjan("A".repeat(33)))
    }

    @Test
    fun generickiUnos_odbijaRazmakeInterpunkcijuIUnicode() {
        listOf("AB 12", "AB-12", "AB_12", "ČĆ12", "AB١٢").forEach { unos ->
            assertFalse("Očekivano je da $unos bude odbijen", generickiBarkodUnosValjan(unos))
        }
    }

    @Test
    fun obradaGenerickogUnosa_cuvaMalaSlovaBezNormalizacije() {
        assertEquals(
            StanjeGenerickogUnosa("aBc123", neispravanPokusaj = false),
            obradiGenerickiBarkodUnos(trenutniUnos = "", noviUnos = "aBc123")
        )
    }

    @Test
    fun obradaGenerickogUnosa_neMijenjaPosljednjuVrijednostNakonNeispravnogPokusaja() {
        assertEquals(
            StanjeGenerickogUnosa("ABC123", neispravanPokusaj = true),
            obradiGenerickiBarkodUnos(trenutniUnos = "ABC123", noviUnos = "ABC123-")
        )
        assertEquals(
            StanjeGenerickogUnosa("A".repeat(32), neispravanPokusaj = true),
            obradiGenerickiBarkodUnos(
                trenutniUnos = "A".repeat(32),
                noviUnos = "A".repeat(33)
            )
        )
    }

    @Test
    fun izborValidacije_zadrzavaS10KaoStrogiFinalniGate() {
        assertTrue(barkodUnosValjan("RR123456785BA", s10ValidacijaUkljucena = true))
        assertFalse(barkodUnosValjan("ABC123", s10ValidacijaUkljucena = true))
        assertTrue(barkodUnosValjan("ABC123", s10ValidacijaUkljucena = false))
    }
}
