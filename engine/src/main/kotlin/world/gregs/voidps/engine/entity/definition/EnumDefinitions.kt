package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

class EnumDefinitions(
    decoder: EnumDecoder,
    private val structs: StructDefinitions
) : DefinitionsDecoder<EnumDefinition> {

    override val definitions: Array<EnumDefinition>
    override lateinit var ids: Map<String, Int>
    private lateinit var parameters: Map<String, Map<String, Int>>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("enum definition", definitions.size, start)
    }

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

    fun load(storage: FileStorage = get(),
             path: String = getProperty("enumDefinitionsPath"),
             structPath: String = getProperty("structParamDefinitionsPath")
    ): EnumDefinitions {
        timedLoad("enum extra") {
            decode(storage, path)
        }
        timedLoad("struct param") {
            parameters = storage.load(structPath)
            parameters.size
        }
        return this
    }

}