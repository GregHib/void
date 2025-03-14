package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.timedLoad

/**
 * Also known as DataMap in cs2
 */
class EnumDefinitions(
    override var definitions: Array<EnumDefinition>,
    private val structs: StructDefinitions
) : DefinitionsDecoder<EnumDefinition> {

    override lateinit var ids: Map<String, Int>

    fun <T : Any> getStruct(id: String, index: Int, param: String): T {
        val enum = get(id)
        val struct = enum.getInt(index)
        return structs.get(struct)[param]
    }

    fun <T : Any?> getStructOrNull(id: String, index: Int, param: String): T? {
        val enum = get(id)
        val struct = enum.getInt(index)
        return structs.getOrNull(struct)?.getOrNull(param)
    }

    fun <T : Any> getStruct(id: String, index: Int, param: String, default: T): T {
        val enum = get(id)
        val struct = enum.getInt(index)
        return structs.get(struct)[param, default]
    }

    fun load(path: String = Settings["definitions.enums"]): EnumDefinitions {
        timedLoad("enum extra") {
            val ids = Object2IntOpenHashMap<String>(definitions.size, Hash.VERY_FAST_LOAD_FACTOR)
            val reader = object : ConfigReader(50) {
                override fun set(section: String, key: String, value: Any) {
                    if (section == "enums") {
                        val id = (value as Long).toInt()
                        ids[key] = id
                        definitions[id].stringId = key
                    } else {
                        val stringId = section.removePrefix("enums.")
                        when (key) {
                            "id" -> {
                                val id = (value as Long).toInt()
                                ids[stringId] = id
                                definitions[id].stringId = stringId
                            }
                            else -> {
                                require(ids.containsKey(stringId)) { "Cannot find definition for id '$stringId'. Make sure the id value is first in the section." }
                                val definition = definitions[ids.getInt(stringId)]
                                var extras = definition.extras
                                if (extras == null) {
                                    extras = Object2ObjectOpenHashMap(2, Hash.VERY_FAST_LOAD_FACTOR)
                                    definition.extras = extras
                                }
                                (extras as MutableMap<String, Any>)[key] = value
                            }
                        }
                    }
                }
            }
            Config.decodeFromFile(path, reader)
            this.ids = ids
            ids.size
        }
        return this
    }

    override fun empty() = EnumDefinition.EMPTY

}