package world.gregs.voidps.engine.map.region

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

/**
 * Resend region load when FinishRegionLoad wasn't received
 */
object RegionRetry : Event {
    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "region_retry"
        else -> null
    }
}
