package world.gregs.voidps.engine.map.instance

import org.koin.dsl.module
import world.gregs.voidps.engine.map.region.Region
import java.util.*

class InstancePool {

    val view = Region(FREE_REGION_X, 0)
        .toRectangle(
            width = MAX_REGION - FREE_REGION_X,
            height = MAX_REGION
        )
    var iterator = view.toRegions().iterator()
    private var pool: Deque<Region> = LinkedList()

    fun obtain(): Region {
        if(pool.isEmpty()) {
            if(iterator.hasNext()) {
                pool.add(iterator.next())
            } else {
                throw IllegalStateException("No free instances remaining.")
            }
        }
        return pool.pollFirst()
    }

    fun free(instance: Region) {
        pool.addLast(instance)
    }

    companion object {
        private const val FREE_REGION_X = 93
        private const val MAX_REGION = 255
    }
}

val instancePoolModule = module {
    single { InstancePool() }
}