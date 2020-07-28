package rs.dusk.engine.model.world

import org.koin.dsl.module
import java.util.*

class InstancePool {

    val view = Instance(FREE_REGION_X, 0).area(MAX_REGION - FREE_REGION_X, MAX_REGION)
    var iterator = view.iterator()
    var pool: Deque<Instance> = LinkedList()

    fun obtain(): Instance {
        if(pool.isEmpty()) {
            if(iterator.hasNext()) {
                pool.add(iterator.next())
            } else {
                throw IllegalStateException("No free instances remaining.")
            }
        }
        return pool.pollFirst()
    }

    fun free(instance: Instance) {
        pool.addLast(instance)
    }

    companion object {
        private val FREE_REGION_X = 93
        private val MAX_REGION = 255
    }
}

val instancePoolModule = module {
    single { InstancePool() }
}