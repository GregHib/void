package world.gregs.voidps.tools.search.screen.view.detail

import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.tools.search.getProperties
import kotlin.collections.get

object ParamLookup {
    val paramLookup = mutableMapOf<Int, String>()

    fun load() {
        val params = getProperties(Params::class.java)
        params.filter { it.isConst }.forEach {
            paramLookup[it.getter.call() as Int] = it.name.lowercase()
        }
    }
    fun of(id: Int?): String? = paramLookup[id]
}