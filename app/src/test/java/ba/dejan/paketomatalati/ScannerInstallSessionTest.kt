package ba.dejan.paketomatalati

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScannerInstallSessionTest {

    @Test
    fun novaSesijaJeAktivna() {
        val sesija = ScannerInstallSession(onUnregister = {})

        assertTrue(sesija.isActive)
    }

    @Test
    fun terminalniCallbackDeaktiviraSesijuIOdjavljujeListenerSamoJednom() {
        var brojOdjava = 0
        var brojAkcija = 0
        val sesija = ScannerInstallSession(onUnregister = { brojOdjava++ })

        val prviCallbackJeObradjen = sesija.finish { brojAkcija++ }
        val zakasnjeliCallbackJeObradjen = sesija.finish { brojAkcija++ }

        assertTrue(prviCallbackJeObradjen)
        assertFalse(zakasnjeliCallbackJeObradjen)
        assertFalse(sesija.isActive)
        assertEquals(1, brojOdjava)
        assertEquals(1, brojAkcija)
    }

    @Test
    fun disposeDeaktiviraSesijuIOdjavljujeListenerSamoJednom() {
        var brojOdjava = 0
        val sesija = ScannerInstallSession(onUnregister = { brojOdjava++ })

        sesija.dispose()
        sesija.dispose()

        assertFalse(sesija.isActive)
        assertEquals(1, brojOdjava)
    }

    @Test
    fun callbackNakonDisposeNeMozePokrenutiAkciju() {
        var brojOdjava = 0
        var skenerPokrenut = false
        val sesija = ScannerInstallSession(onUnregister = { brojOdjava++ })

        sesija.dispose()
        val callbackJeObradjen = sesija.finish { skenerPokrenut = true }

        assertFalse(callbackJeObradjen)
        assertFalse(skenerPokrenut)
        assertEquals(1, brojOdjava)
    }
}
