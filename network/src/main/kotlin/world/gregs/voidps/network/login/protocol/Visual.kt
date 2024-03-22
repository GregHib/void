package world.gregs.voidps.network.login.protocol

interface Visual {
    fun needsReset(): Boolean {
        return false
    }

    /**
     * Optional reset to be performed at the end of an update
     */
    fun reset() {
    }

    fun clear() {
        if (needsReset()) {
            reset()
        }
    }
}