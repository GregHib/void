package world.gregs.voidps.engine.map.region

import org.koin.dsl.module

/**
 * @author GregHib <greg@gregs.world>
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