package rs.dusk.engine.path

import rs.dusk.engine.model.entity.Direction

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
class Movement(val steps: Steps, val obstruction: ObstructionStrategy) {
    fun addStep(direction: Direction, check: Boolean) {

        if (check) {
//            obstruction.obstructed(direction)
        }
        steps.add(direction)
    }
}