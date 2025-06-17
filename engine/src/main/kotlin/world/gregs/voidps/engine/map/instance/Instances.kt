package world.gregs.voidps.engine.map.instance

import world.gregs.voidps.type.Region
import java.util.*

object Instances {

    private var small: Deque<Region> = LinkedList()
    private var large: Deque<Region> = LinkedList()
    private var used: MutableSet<Region> = mutableSetOf()

    init {
        reset()
    }

    /**
     * Allocates an empty 128x128 (2x2 region) area
     */
    fun small(): Region {
        val region = small.pollFirst()
        used.add(region)
        return region
    }

    /**
     * Allocates an empty 320x320 (5x5 region) area
     */
    fun large(): Region {
        val region = large.pollFirst()
        used.add(region)
        return region
    }

    fun isInstance(region: Region): Boolean = used.contains(region)

    fun free(instance: Region) {
        if (!used.remove(instance)) {
            return
        }
        if (instance.y >= MID_POINT) {
            large.add(instance)
        } else {
            small.add(instance)
        }
    }

    fun reset() {
        used.clear()
        small.clear()
        large.clear()
        for (x in FREE_REGION_X until MAX_REGION - SMALL_SIZE step SMALL_SIZE) {
            for (y in FREE_REGION_Y until MID_POINT - SMALL_SIZE step SMALL_SIZE) {
                small.add(Region(x + 1, y + 1))
            }
        }
        for (x in FREE_REGION_X until MAX_REGION - LARGE_SIZE step LARGE_SIZE) {
            for (y in MID_POINT until MAX_REGION - LARGE_SIZE step LARGE_SIZE) {
                large.add(Region(x + 1, y + 1))
            }
        }
    }

    private const val SMALL_SIZE = 3
    private const val LARGE_SIZE = 6
    private const val FREE_REGION_X = 100
    private const val FREE_REGION_Y = 0
    private const val MAX_REGION = 255
    private const val MID_POINT = 82
}
