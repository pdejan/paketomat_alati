package ba.dejan.paketomatalati

private const val MAKSIMALNA_DUZINA_PINA = 20

/** Prihvata samo neprazan PIN sastavljen od ASCII cifara 0-9. */
fun pinJeValjan(unos: String): Boolean =
    unos.length in 1..MAKSIMALNA_DUZINA_PINA && unos.all { it in '0'..'9' }
