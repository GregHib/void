package world.gregs.voidps.engine.map.instance

import world.gregs.voidps.type.Region
import java.util.*

object Instances {

    private var small: Deque<Region> = LinkedList()
    private var large: Deque<Region> = LinkedList()

    /**
     * How many players hold each allocated region, so a shared instance isn't handed out again
     * while some of its party are still inside
     */
    private var used: MutableMap<Region, Int> = mutableMapOf()

    init {
        reset()
    }

    /**
     * Allocates an empty 128x128 (2x2 region) area
     */
    fun small(): Region = allocate(small)

    /**
     * Allocates an empty 320x320 (5x5 region) area
     */
    fun large(): Region = allocate(large)

    private fun allocate(pool: Deque<Region>): Region {
        val region = pool.pollFirst()
        used[region] = 1
        return region
    }

    /**
     * Records another holder of [instance], so it survives until they've all [free]d it
     */
    fun claim(instance: Region) {
        val holders = used[instance] ?: return
        used[instance] = holders + 1
    }

    fun isInstance(region: Region): Boolean = used.containsKey(region)

    fun reserved(region: Region): Boolean = region.x > FREE_REGION_X

    /**
     * Releases one hold on [instance], returning true once the last holder has let go and the
     * region is back in the pool
     */
    fun free(instance: Region): Boolean {
        val holders = used[instance] ?: return false
        if (holders > 1) {
            used[instance] = holders - 1
            return false
        }
        used.remove(instance)
        if (instance.y >= MID_POINT) {
            large.add(instance)
        } else {
            small.add(instance)
        }
        return true
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
