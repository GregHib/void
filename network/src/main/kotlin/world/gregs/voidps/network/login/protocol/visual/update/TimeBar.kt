package world.gregs.voidps.network.login.protocol.visual.update

import world.gregs.voidps.network.login.protocol.Visual

data class TimeBar(
    var full: Boolean = false,
    var exponentialDelay: Int = 0,
    var delay: Int = 0,
    var increment: Int = 0,
) : Visual {
    override fun needsReset(): Boolean = full || exponentialDelay != 0 || delay != 0 || increment != 0

    override fun reset() {
        full = false
        exponentialDelay = 0
        delay = 0
        increment = 0
    }
}
