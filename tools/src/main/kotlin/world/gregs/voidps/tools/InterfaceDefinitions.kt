package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule

object InterfaceDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = InterfaceDecoder(koin.get())
        for (i in listOf(519)) {//decoder.indices) {
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