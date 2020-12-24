package rs.dusk.tools

import org.koin.core.context.startKoin
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule

object ObjectDefinitions {

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = ObjectDecoder(koin.get(), member = false, lowDetail = false, configReplace = false)
        val objects = decoder.indices.mapNotNull { decoder.get(it) }
        val trapdoors = objects.mapNotNull { def ->
            val name = def.name.replace(" ", "")
            if((name.equals("trapdoor", true) || name.contains("trapdoor", true)) && def.options.first() != null) {
                def
            } else {
                null
            }
        }

        val openable = trapdoors.filter { it.options.first().equals("Open") }

        val others = objects.filter { !it.options.first().equals("Open") }

        var count = 0
        openable.forEach {
            if(!decoder.get(it.id + 1).name.contains("trap", true)) {
                println("${it.id} ${it.name} ${it.options.toList()} ${it.modelIds?.contentDeepToString()} ${it.originalColours?.toList()} ${it.originalColours?.toList()}")
            } else {
                count++
            }
        }

        println(count)
        println()
        println()
        trapdoors.filter { !it.options.first().equals("Open") }.forEach {
            if(!decoder.get(it.id -1).name.contains("trap", true)) {
                println("${it.id} ${it.name} ${it.options.toList()} ${it.modelIds?.contentDeepToString()} ${it.originalColours?.toList()} ${it.originalColours?.toList()}")
            }
        }

//        openable.forEach { def ->
//            val models = def.modelIds!!.flatMap{ it.toSet() }.toSet()
//            val options = others.filter { it.modelIds != null && it.modelIds!!.any { it.any { models.contains(it) } } }
//            println("${def.id} ${def.name} ${options.map { "${it.id} ${it.name} ${it.options.first()}" }}")
//        }


    }

    fun ObjectDecoder.findMatchingName(name: String) {
        for (i in 0 until size) {
            val def = getOrNull(i) ?: continue
            if (def.modelIds != null && def.name.contains(name, true)) {
                println("Found $i ${def.options.get(0)} ${def.modelIds?.contentDeepToString()}")
            }
        }
    }

    fun ObjectDecoder.findMatchingSize(width: Int, height: Int) {
        for (i in 0 until size) {
            val def = getOrNull(i) ?: continue
            if (def.modelIds != null && def.sizeX == width && def.sizeY == height) {
                println("Found $i ${def.options.get(0)} ${def.modelIds?.contentDeepToString()}")
            }
        }
    }

    fun ObjectDecoder.findMatchingModels(id: Int) {
        val original = getOrNull(id)!!
        for (i in 0 until size) {
            val def = getOrNull(i) ?: continue
            if (def.modelIds != null && def.modelIds!!.contentDeepEquals(original.modelIds!!)) {
                println("Found $i ${def.options.get(0)}")
            }
        }
    }
}