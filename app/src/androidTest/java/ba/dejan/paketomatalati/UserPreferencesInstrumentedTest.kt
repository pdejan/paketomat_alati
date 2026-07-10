package ba.dejan.paketomatalati

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserPreferencesInstrumentedTest {

    @Test
    fun ostecenePreferenceSeOporavljajuUPraznoOdjavljenoStanje() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val datoteka = File(
            context.filesDir,
            "datastore/test-${UUID.randomUUID()}.preferences_pb"
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        try {
            datoteka.parentFile?.mkdirs()
            // Truncated length-delimited protobuf field: guaranteed malformed preferences data.
            datoteka.writeBytes(byteArrayOf(0x0A, 0x7F))
            val dataStore = PreferenceDataStoreFactory.create(
                corruptionHandler = radnikPreferencesCorruptionHandler,
                scope = scope,
                produceFile = { datoteka }
            )

            assertTrue(dataStore.data.first().asMap().isEmpty())
        } finally {
            scope.cancel()
            datoteka.delete()
        }
    }
}
