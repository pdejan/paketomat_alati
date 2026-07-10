package ba.dejan.paketomatalati
class ScannerInstallSession(
    private val onUnregister: () -> Unit
) {
    var isActive: Boolean = true
        private set

    fun finish(action: () -> Unit): Boolean {
        if (!isActive) return false
        isActive = false
        onUnregister()
        action()
        return true
    }

    fun dispose() {
        if (!isActive) return
        isActive = false
        onUnregister()
    }
}
