package world.gregs.voidps.engine.map.region

import org.koin.dsl.module

data class Xteas(
    val delegate: MutableMap<Int, IntArray> = mutableMapOf()
) : Map<Int, IntArray> by delegate {

    operator fun get(region: Region): IntArray? {
        return this[region.id]
    }

}

val xteaModule = module {
    single(createdAtStart = true) {
        Xteas(mutableMapOf()).apply {
            XteaLoader().load(this, getProperty("xteaPath"), getPropertyOrNull("xteaJsonKey"), getPropertyOrNull("xteaJsonValue"))
        }
    }
}