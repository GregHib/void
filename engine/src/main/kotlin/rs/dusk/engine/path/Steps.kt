package rs.dusk.engine.path

import rs.dusk.engine.model.entity.Direction
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
class Steps(val steps: Deque<Direction> = LinkedList()) : Deque<Direction> by steps