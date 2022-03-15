package world.gregs.voidps.engine.map.region

data class Xteas(
    val delegate: MutableMap<Int, IntArray> = mutableMapOf()
) : Map<Int, IntArray> by delegate {

    operator fun get(region: Region): IntArray? {
        return this[region.id]
    }

}