package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class EnumDefinitions(
    override var definitions: Array<EnumDefinition>,
    private val structs: StructDefinitions
) : DefinitionsDecoder<EnumDefinition> {

    override lateinit var ids: Map<String, Int>
    private lateinit var parameters: Map<String, Map<String, Int>>

    fun <T : Any> getStruct(id: String, index: Int, param: String): T {
        val enum = get(id)
        val map = parameters.getValue(enum.extras?.get("struct") as String)
        val key = map.getValue(param)
        return structs.get(enum.getInt(index)).getParam(key.toLong())
    }

    fun <T : Any> getStruct(id: String, index: Int, param: String, default: T): T {
        val enum = get(id)
        val struct = enum.extras?.get("struct") as? String ?: return default
        val map = parameters[struct] ?: return default
        val key = map[param] ?: return default
        return structs.get(enum.getInt(index)).getParam(key.toLong(), default)
    }

    override fun empty() = EnumDefinition.EMPTY

    fun load(
        yaml: Yaml = get(),
        path: String = getProperty("enumDefinitionsPath"),
        structPath: String = getProperty("structParamDefinitionsPath")
    ): EnumDefinitions {
        timedLoad("enum extra") {
            decode(yaml, path)
        }
        timedLoad("struct param") {
            parameters = yaml.load(structPath)
            parameters.size
        }
        return this
    }

}