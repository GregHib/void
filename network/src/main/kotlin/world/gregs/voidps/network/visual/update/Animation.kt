package world.gregs.voidps.network.visual.update

import world.gregs.voidps.network.Visual

/**
 * @param stand animate only while stationary (or during force movement)
 * @param force animate after force movement
 * @param walk can animate while walking
 * @param run can animate while running
 */
data class Animation(
    var stand: Int = -1,
    var force: Int = -1,
    var walk: Int = -1,
    var run: Int = -1,
    var speed: Int = 0
) : Visual {
    var priority: Int = -1

    override fun needsReset(): Boolean {
        return stand != -1 || force != -1 || walk != -1 || run != -1
    }

    override fun reset() {
        stand = -1
        force = -1
        walk = -1
        run = -1
        speed = 0
        priority = -1
    }
}