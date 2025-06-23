package world.gregs.voidps.tools.definition.obj.pipe

import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.ObjectDefinitionFull
import world.gregs.voidps.cache.definition.decoder.ObjectDecoderFull
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.definition.item.Extras
import kotlin.math.abs

fun ObjectDefinitionFull.isDoor() = (name.contains("door", true) && !name.contains("trap", true)) || name.contains("gate", true)

class ObjectDoorsGates(private val decoder: Array<ObjectDefinitionFull>) : Pipeline.Modifier<MutableMap<Int, Extras>> {

    override fun modify(content: MutableMap<Int, Extras>): MutableMap<Int, Extras> {
        var fence = 0
        var door = 0
        content.forEach { (id, content) ->
            val (_, extras) = content
            val def = decoder[id]
            if (fences.contains(id) || def.isDoor() && def.options?.first().equals("open", true)) {
                val match = match(def)
                if (match != -1) {
                    extras["open"] = match
                    if (fences.contains(id)) {
                        fence++
                    } else {
                        door++
                    }
                }
            }
            if (id == 11620) {
                extras["open"] = 11624
            } else if (id == 11621) {
                extras["open"] = 11625
            }
        }
        println("Matched $fence fences.")
        println("Matched $door doors.")
        return content
    }

    private val fences = setOf(
        47, 48, 166, 167, 883, 1551, 1553, 1598, 1599, 2050, 2051, 2261, 2262, 2306, 2320, 2394, 2438, 2439, 2489, 2495, 3015, 3016, 3725,
        3726, 4311, 4312, 7049, 7050, 8810, 8811, 12986, 12987, 15510, 15512, 15514, 15516, 23917, 24560, 24561, 27846, 27848, 27852, 27854,
        34779, 34780, 36913, 36915, 36917, 36919, 37352, 37354, 45206, 45208, 45210, 45212,
    )

    private fun match(def: ObjectDefinitionFull): Int {
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
            val first = if (matches.size == 1) {
                matches.first()
            } else {
                matches.minByOrNull { abs(it.id - def.id) }!!
            }
            return first.id
        }
        return -1
    }

    private fun Array<ObjectDefinitionFull>.findMatchingModels(definition: ObjectDefinitionFull): List<ObjectDefinitionFull> {
        return indices.mapNotNull {
            val def = getOrNull(it) ?: return@mapNotNull null
            if (definition.id == it) {
                return@mapNotNull null
            }
            if (def.modelIds == null || !def.modelIds!!.contentDeepEquals(definition.modelIds!!)) {
                return@mapNotNull null
            }
            val options = def.options ?: return@mapNotNull def
            val option = options[0]
            if (option.isNullOrEmpty() || option == "null" || option.equals("close", true)) {
                return@mapNotNull def
            } else {
                return@mapNotNull null
            }
        }
    }

    private fun List<ObjectDefinitionFull>.filterByMirrored(definition: ObjectDefinitionFull) = filter { it.mirrored == definition.mirrored }

    private fun List<ObjectDefinitionFull>.filterByAppearance(definition: ObjectDefinitionFull) = filter {
        it.brightness == definition.brightness &&
            it.contrast == definition.contrast &&
            it.contouredGround == definition.contouredGround &&
            it.castsShadow == definition.castsShadow &&
            it.modelSizeX == definition.modelSizeX &&
            it.modelSizeZ == definition.modelSizeZ &&
            it.modelSizeY == definition.modelSizeY &&
            it.offsetX == definition.offsetX &&
            it.offsetZ == definition.offsetZ &&
            it.offsetY == definition.offsetY
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Settings.load()
            val cache = CacheDelegate(Settings["storage.cache.path"])
            val decoder = ObjectDecoderFull(members = false, lowDetail = true).load(cache)
            val gates = ObjectDoorsGates(decoder)
            val match = gates.match(decoder[45849])
            println("Match $match")
        }
    }
}
