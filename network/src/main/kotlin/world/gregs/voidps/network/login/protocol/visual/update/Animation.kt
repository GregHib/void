package world.gregs.voidps.network.login.protocol.visual.update

import world.gregs.voidps.network.login.protocol.Visual

/**
 * @param stand animate only while stationary (or during exact movement)
 * @param force animate after exact movement
 * @param walk can animate while walking
 * @param run can animate while running
 */
data class Animation(
    var stand: Int = -1,
    var force: Int = -1,
    var walk: Int = -1,
    var run: Int = -1,
    var delay: Int = 0,
) : Visual {
    var infinite: Boolean = false
    var priority: Int = -1

    override fun needsReset(): Boolean {
        if (infinite) {
            return false
        }
        return stand != -1 || force != -1 || walk != -1 || run != -1
    }

    override fun reset() {
        stand = -1
        force = -1
        walk = -1
        run = -1
        delay = 0
        priority = -1
        infinite = false
    }
}
