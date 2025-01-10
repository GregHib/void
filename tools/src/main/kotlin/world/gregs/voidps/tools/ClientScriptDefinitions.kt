package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.decoder.ClientScriptDecoder

object ClientScriptDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("storage.cache.path"))
        val decoder = ClientScriptDecoder().load(cache)
        for (i in decoder.indices) {
//            if (i != 1142) {
//                continue
//            }
            val def = decoder.getOrNull(i) ?: continue
//            println(def)
            for (index in def.instructions.indices) {
                if (def.instructions[index] == 3627) {
                    println(def)
//                    println("Found ${def.instructions[index]} $index ${def.intOperands!![index + 1]}")
//                    for (idx in 0 until def.instructions.lastIndex) {
//                        val instruction = def.instructions[idx]
//                        println("$idx $instruction ${def.stringOperands!![idx]} ${def.intOperands!![idx]}")
//                    }
                }
            }
        }
    }

    fun getScriptId(cache: Cache, id: Int, context: Int): Int {
        var scriptId = cache.archiveId(Index.CLIENT_SCRIPTS, context or (id shl 10))
        if (scriptId != -1) {
            return scriptId
        }
        scriptId = cache.archiveId(Index.CLIENT_SCRIPTS, (65536 + id shl 10) or context)
        if (scriptId != -1) {
            return scriptId
        }
        scriptId = cache.archiveId(Index.CLIENT_SCRIPTS, context or 0x3fffc00)
        return scriptId
    }

}