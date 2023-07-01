package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder

object InterfaceDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val decoder = InterfaceDecoder(cache)
        for (i in listOf(729)) {//decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            println(def.components?.keys)
            for ((id, comp) in def.components ?: continue) {
//                if(comp.anObjectArray4758 != null) {
                    println("$id - $comp")
//                if (comp.containers != null) {
//                    println("${comp.id} ${def.id} ${Interface.getId(comp.id)} ${Interface.getComponentId(comp.id)} $id ${comp.anObjectArray4758?.toList()}")
//                }
            }
        }
    }
}