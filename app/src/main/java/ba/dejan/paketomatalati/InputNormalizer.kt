package ba.dejan.paketomatalati

import java.util.Locale

/** Uklanja razmake iz QR sadržaja bez ograničavanja ostalih znakova. */
fun normalizujQrPayload(payload: String): String =
    payload.filterNot { it.isWhitespace() }

/** Normalizuje ručni S10 unos prije provjere dužine, formata i kontrolne cifre. */
fun normalizujS10Unos(unos: String): String =
    unos.filterNot { it.isWhitespace() }.uppercase(Locale.ROOT)
