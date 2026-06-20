package ba.dejan.paketomatalati
/**
 * Validacija broja pošiljke po UPU S10 standardu.
 * S10 format ima 13 znakova: 2 slova (oznaka usluge) + 8 cifara serijskog broja
 * + 1 kontrolna cifra + 2 slova (kod zemlje), npr. "EE123456785BA".
 * Kontrolna (9.) cifra se računa iz prethodnih 8 cifara, pa hvata greške u kucanju.
 */
private val S10_REGEX = Regex("^[A-Z]{2}[0-9]{9}[A-Z]{2}$")
/** Dali unos izgleda kao S10 broj pošiljke (2 slova + 9 cifara + 2 slova). */
fun izgledaKaoS10(broj: String): Boolean = S10_REGEX.matches(broj)
/** Potpuna provjera S10 formata i kontrolne cifre. */
fun s10BrojValjan(broj: String): Boolean =
    izgledaKaoS10(broj) && s10KontrolnaCifraValjana(broj)
/**
 * Provjerava kontrolnu cifru po UPU S10 standardu. Vraća true ako se 9. cifra
 * poklapa sa vrijednošću izračunatom iz prethodnih 8 cifara serijskog broja.
 */
fun s10KontrolnaCifraValjana(broj: String): Boolean {
    val cifre = broj.filter { it.isDigit() }
    if (cifre.length != 9) return false
    val tezine = intArrayOf(8, 6, 4, 2, 3, 5, 9, 7)
    var suma = 0
    for (i in 0 until 8) {
        suma += (cifre[i] - '0') * tezine[i]
    }
    val izracunata = when (val c = 11 - (suma % 11)) {
        10 -> 0
        11 -> 5
        else -> c
    }
    return izracunata == (cifre[8] - '0')
}
