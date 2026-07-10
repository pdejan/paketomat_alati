package ba.dejan.paketomatalati

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinValidatorTest {

    @Test
    fun `prihvata jednu do dvadeset ASCII cifara bez izmjene`() {
        assertTrue(pinJeValjan("0"))
        assertTrue(pinJeValjan("012345"))
        assertTrue(pinJeValjan("01234567890123456789"))
    }

    @Test
    fun `odbija prazan PIN`() {
        assertFalse(pinJeValjan(""))
    }

    @Test
    fun `odbija PIN duzi od dvadeset znakova`() {
        assertFalse(pinJeValjan("012345678901234567890"))
    }

    @Test
    fun `odbija slova bez tihog uklanjanja iz unosa`() {
        assertFalse(pinJeValjan("12O4"))
    }

    @Test
    fun `odbija interpunkciju bez tihog uklanjanja iz unosa`() {
        assertFalse(pinJeValjan("12-4"))
    }

    @Test
    fun `odbija razmake i kontrolne whitespace znakove`() {
        assertFalse(pinJeValjan("12 4"))
        assertFalse(pinJeValjan("12\t4"))
        assertFalse(pinJeValjan("12\n4"))
    }

    @Test
    fun `odbija arapsko indijske cifre iako ih Kotlin smatra ciframa`() {
        assertFalse(pinJeValjan("\u0661\u0662\u0663\u0664"))
    }
}
