package world.gregs.void.engine.map.region

import org.koin.dsl.module

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
data class Xteas(val delegate: Map<Int, IntArray>) : Map<Int, IntArray> by delegate {

    operator fun get(region: Region): IntArray? {
        return this[region.id]
    }

}

val xteaModule = module {
    single(createdAtStart = true) {
        XteaLoader().run(getProperty("xteaPath"), getPropertyOrNull("xteaJsonKey"), getPropertyOrNull("xteaJsonValue"))
    }
}