package ba.dejan.paketomatalati

const val MAKSIMALNA_DUZINA_GENERICKOG_BARKODA = 32

data class StanjeGenerickogUnosa(
    val vrijednost: String,
    val neispravanPokusaj: Boolean
)

private fun generickiBarkodUnosZaUredjivanjeValjan(unos: String): Boolean =
    unos.length <= MAKSIMALNA_DUZINA_GENERICKOG_BARKODA && unos.all { znak ->
        znak in 'A'..'Z' || znak in 'a'..'z' || znak in '0'..'9'
    }

fun generickiBarkodUnosValjan(unos: String): Boolean =
    unos.isNotEmpty() && generickiBarkodUnosZaUredjivanjeValjan(unos)

fun obradiGenerickiBarkodUnos(
    trenutniUnos: String,
    noviUnos: String
): StanjeGenerickogUnosa =
    if (generickiBarkodUnosZaUredjivanjeValjan(noviUnos)) {
        StanjeGenerickogUnosa(noviUnos, neispravanPokusaj = false)
    } else {
        StanjeGenerickogUnosa(trenutniUnos, neispravanPokusaj = true)
    }

fun barkodUnosValjan(unos: String, s10ValidacijaUkljucena: Boolean): Boolean =
    if (s10ValidacijaUkljucena) {
        s10BrojValjan(unos)
    } else {
        generickiBarkodUnosValjan(unos)
    }
