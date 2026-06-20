package ba.dejan.paketomatalati

import org.junit.Assert.assertEquals
import org.junit.Test

class InputNormalizerTest {

    @Test
    fun qrPayload_uklanjaSveVrsteRazmakaIAliCuvaOstaleZnakove() {
        val payload = " A b\tC\n1\r\n2-_/+= "

        assertEquals("AbC12-_/+=", normalizujQrPayload(payload))
    }

    @Test
    fun qrPayload_neMijenjaDugacakPayloadBezRazmaka() {
        val payload = "aB9!@#${'$'}%^&*()_+-=[]{};:,.?/|~".repeat(20)

        assertEquals(payload, normalizujQrPayload(payload))
    }

    @Test
    fun s10Unos_uklanjaRazmakeIPretvaraSlovaUVelika() {
        assertEquals("RR123456785BA", normalizujS10Unos(" rr 123 456 785 ba\n"))
    }
}
