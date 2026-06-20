package ba.dejan.paketomatalati

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptoManagerInstrumentedTest {

    @Test
    fun sifrovanje_roundTripCuvaOriginalniPayload() {
        val payload = "QR-payload-!@#-1234567890"

        assertEquals(payload, CryptoManager.decrypt(CryptoManager.encrypt(payload)))
    }

    @Test
    fun sifrovanje_istiPayloadSvakiPutDajeDrugiCiphertext() {
        val payload = "isti-payload"

        assertNotEquals(CryptoManager.encrypt(payload), CryptoManager.encrypt(payload))
    }
}
