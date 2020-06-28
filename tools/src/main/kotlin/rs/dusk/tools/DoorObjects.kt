package rs.dusk.tools

import org.koin.core.context.startKoin
import rs.dusk.cache.cacheDefinitionModule
import rs.dusk.cache.cacheModule
import rs.dusk.cache.definition.data.ObjectDefinition
import rs.dusk.cache.definition.decoder.ObjectDecoder
import kotlin.math.abs

object DoorObjects {
    @JvmStatic
    fun main(args: Array<String>) {
        startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }
        val decoder = ObjectDecoder(false, false)
//        dumpFences(decoder)
        dumpDoors(decoder)
    }

    val fences = listOf(
        47, 48, 166, 167, 883, 1551, 1553, 1598, 1599, 2050, 2051, 2261, 2262, 2306, 2320, 2394, 2438, 2439, 2489, 2495, 3015, 3016, 3725,
        3726, 4311, 4312, 7049, 7050, 8810, 8811, 12986, 12987, 15510, 15512, 15514, 15516, 23917, 24560, 24561, 27846, 27848, 27852, 27854,
        34779, 34780, 36913, 36915, 36917, 36919, 37352, 37354, 45206, 45208, 45210, 45212
    )

    fun dumpFences(decoder: ObjectDecoder) {
        var total = 0
        var count = 0
        fences.forEach { id ->
            val def = decoder.get(id) ?: return@forEach
            total++
            if(match(decoder, def)) {
                count++
            }
        }
        println("Matched $count fences out of $total.")
    }

    fun dumpDoors(decoder: ObjectDecoder) {
        var total = 0
        var count = 0
        for (id in 0 until decoder.size) {
            val def = decoder.get(id) ?: continue
            if (def.name.isDoor() && !fences.contains(id)) {
                val options = def.options
                if (options != null) {
                    val option = options[0]
                    if (option.equals("open", true)) {
                        total++
                        if(match(decoder, def)) {
                            count++
                        }
                    }
                }
            }
        }
        println("11620: 11624")
        println("11621: 11625")
        println("Matched $count doors out of $total.")
    }

    fun match(decoder: ObjectDecoder, def: ObjectDefinition): Boolean {
        var matches = decoder.findMatchingModels(def)
        if (matches.isNotEmpty()) {
            var filtered = matches.filterByAppearance(def)
            if (filtered.isNotEmpty()) {
                matches = filtered
            }
            filtered = matches.filterByMirrored(def)
            if (filtered.isNotEmpty()) {
                matches = filtered
            }
        }
        if (matches.isNotEmpty()) {
            val first = if(matches.size == 1) {
                matches.first()
            } else {
                matches.minBy { abs(it.id - def.id) }!!
            }
            println("${def.id}: ${first.id}")
            return true
        }
        return false
    }
    fun ObjectDecoder.findMatchingModels(definition: ObjectDefinition): List<ObjectDefinition> {
        return (0 until size).mapNotNull {
            val def = get(it) ?: return@mapNotNull null
            if(definition.id == it) {
                return@mapNotNull null
            }
            if (def.modelIds == null || !def.modelIds!!.contentDeepEquals(definition.modelIds!!)) {
                return@mapNotNull null
            }
            val options = def.options
            if(options != null) {
                val option = options[0]
                if(option.isNullOrEmpty() || option == "null" || option.equals("close", true)) {
                    return@mapNotNull def
                } else {
                    return@mapNotNull null
                }
            }
            def
        }
    }

    fun List<ObjectDefinition>.filterByMirrored(definition: ObjectDefinition) =
        filter { it.mirrored == definition.mirrored }

    fun List<ObjectDefinition>.filterByAppearance(definition: ObjectDefinition) = filter {
                it.brightness == definition.brightness &&
                it.contrast == definition.contrast &&
                it.contouredGround == definition.contouredGround &&
                it.castsShadow == definition.castsShadow &&
                it.modelSizeX == definition.modelSizeX &&
                it.modelSizeY == definition.modelSizeY &&
                it.modelSizeZ == definition.modelSizeZ &&
                it.offsetX == definition.offsetX &&
                it.offsetY == definition.offsetY &&
                it.offsetZ == definition.offsetZ
    }

    fun String.isDoor() = contains("door", true) || contains("gate", true)
}