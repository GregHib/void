package world.gregs.voidps.tools

import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.data.ObjectDefinitionFull
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.ObjectDefinitions

object ObjectDefinitions {

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache = CacheDelegate(Settings["storage.cache.path"])
        ObjectDefinitions.init(ObjectDecoder(member = true, lowDetail = false).load(cache))
            .load(configFiles().getValue(Settings["definitions.objects"]))
        for (def in ObjectDefinitions.definitions) {
            if(def.stringId.startsWith("slayer_tower_chain")) {
                println("${def.id} ${def.name} ${def.options?.toList()}")
            }
        }
    }

    fun Array<ObjectDefinitionFull>.findMatchingName(name: String): List<ObjectDefinitionFull> {
        return indices.mapNotNull {
            val def = getOrNull(it) ?: return@mapNotNull null
            if (def.modelIds != null && def.name.contains(name, true)) {
                println("Found $it ${def.options?.get(0)} ${def.modelIds?.contentDeepToString()}")
                return@mapNotNull def
            } else {
                return@mapNotNull null
            }
        }
    }

    fun Array<ObjectDefinitionFull>.findMatchingSize(width: Int, height: Int) {
        for (i in indices) {
            val def = getOrNull(i) ?: continue
            if (def.modelIds != null && def.sizeX == width && def.sizeY == height) {
                println("Found $i ${def.options?.get(0)} ${def.modelIds?.contentDeepToString()}")
            }
        }
    }

    fun Array<ObjectDefinitionFull>.findMatchingModels(id: Int) {
        val original = getOrNull(id)!!
        val models = original.modelIds!!.map { it.toSet() }.flatten().toSet()
        for (i in indices) {
            val def = getOrNull(i) ?: continue
            if (def.modelIds != null && def.modelIds!!.any { arr -> arr.any { models.contains(it) } }) {
                println("Found $i ${def.options?.get(0)}")
            }
        }
    }

    fun Array<ObjectDefinition>.findTransforms(id: Int): List<ObjectDefinition> {
        return indices.mapNotNull {
            val def = getOrNull(it) ?: return@mapNotNull null
            if (def.transforms?.contains(id) == true) {
                println("Found $it ${def.transforms?.contentToString()}")
                return@mapNotNull def
            }
            return@mapNotNull null
        }
    }

    fun Array<ObjectDefinitionFull>.findVarbit(id: Int): List<ObjectDefinitionFull> {
        return indices.mapNotNull {
            val def = getOrNull(it) ?: return@mapNotNull null
            if (def.varbit == id) {
                println("Found $it ${def.varbit}")
                return@mapNotNull def
            }
            return@mapNotNull null
        }
    }
}
