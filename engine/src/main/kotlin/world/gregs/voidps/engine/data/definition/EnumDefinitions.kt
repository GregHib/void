package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

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

    fun load(yaml: Yaml = get(), path: String = Settings["definitions.enums"]): EnumDefinitions {
        timedLoad("enum extra") {
            decode(yaml, path)
        }
        return this
    }

    override fun empty() = EnumDefinition.EMPTY

}