package world.gregs.voidps.network.login.protocol.visual.update

import world.gregs.voidps.network.login.protocol.Visual

data class TimeBar(
    var full: Boolean = false,
    var duration: Int = 0,
    var delay: Int = 0,
    var increment: Int = 0
) : Visual {
    override fun needsReset(): Boolean {
        return full || duration != 0 || delay != 0 || increment != 0
    }

    override fun reset() {
        full = false
        duration = 0
        delay = 0
        increment = 0
    }
}