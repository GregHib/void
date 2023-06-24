package world.gregs.voidps.engine.data.definition.extra

import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.data.yaml.YamlParser
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad

class ContainerDefinitions(
    decoder: ContainerDecoder
) : DefinitionsDecoder<ContainerDefinition> {

    override lateinit var definitions: Array<ContainerDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("container definition", definitions.size, start)
    }

    override fun empty() = ContainerDefinition.EMPTY

    fun load(parser: YamlParser = get(), path: String = getProperty("containerDefinitionsPath"), itemDefs: ItemDefinitions = get()): ContainerDefinitions {
        timedLoad("container extra") {
            decode(parser, path)
            for (def in definitions) {
                if (def.has("defaults") && def.length > 0) {
                    val list = def.get<List<Map<String, Int>>>("defaults")
                    def.ids = IntArray(def.length) { itemDefs.get(list[it].keys.first()).id }
                    def.amounts = IntArray(def.length) { list[it].values.first() }
                }
            }
            definitions.size
        }
        return this
    }
}

fun ContainerDefinition.items(): List<String> {
    val defs: ItemDefinitions = get()
    return ids?.map { defs.get(it).stringId } ?: emptyList()
}