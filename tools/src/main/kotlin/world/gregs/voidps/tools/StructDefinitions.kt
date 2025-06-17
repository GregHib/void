package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.engine.data.Settings
import java.io.File

object StructDefinitions {

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val decoder = StructDecoder().load(cache)
        val set = mutableSetOf<Long>()
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
//            if (def.params != null) {
//                set.addAll(def.params!!.keys)
//            }
            println("$i $def")
        }

        val folder = File("C:\\Users\\Greg\\Documents\\Void\\decompiled-cs2-667\\")
        val map = mutableMapOf<Int, MutableSet<String>>()
        for (file in folder.listFiles()!!) {
            val pattern = "getAttributeMapValue\\(.*, ([0-9]+)\\)".toRegex().toPattern()
            val matcher = pattern.matcher(file.readText())
            while (matcher.find()) {
                val param = matcher.group(1).toInt()
                map.getOrPut(param) { mutableSetOf() }.add(file.nameWithoutExtension)
            }
        }

        for (param in set.sorted()) {
            val files = map[param.toInt()]
            if (files != null) {
                println("    const val PARAM_$param = ${param}L // ${files.joinToString(", ") { "$it.cs2" }}")
            } else {
                println("    const val PARAM_$param = ${param}L")
            }
        }
    }
}
