package world.gregs.voidps.engine.data.definition.extra

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.data.definition.data.Pickable
import world.gregs.voidps.engine.data.definition.data.Rock
import world.gregs.voidps.engine.data.definition.data.Tree
import world.gregs.voidps.engine.data.yaml.YamlParser
import world.gregs.voidps.engine.data.yaml.config.DefinitionConfig
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad

class ObjectDefinitions(
    decoder: ObjectDecoder
) : DefinitionsDecoder<ObjectDefinition> {

    override lateinit var definitions: Array<ObjectDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("object definition", definitions.size, start)
    }

    override fun empty() = ObjectDefinition.EMPTY

    @Suppress("UNCHECKED_CAST")
    fun load(parser: YamlParser = get(), path: String = getProperty("objectDefinitionsPath"), itemDefinitions: ItemDefinitions? = get()): ObjectDefinitions {
        timedLoad("object extra") {
            val ids = Object2IntOpenHashMap<String>()
            this.ids = ids
            val config = object : DefinitionConfig<ObjectDefinition>(ids, definitions) {
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) { 
                    if(indent == 1) {
                        super.set(map, key,
                            when (key) {
                                "pickable" -> Pickable(value as Map<String, Any>, itemDefinitions!!)
                                "woodcutting" -> Tree(value as Map<String, Any>, itemDefinitions!!)
                                "mining" -> Rock(value as Map<String, Any>, itemDefinitions!!)
                                else -> value
                            }, indent, parentMap)
                    } else {
                        super.set(map, key, value, indent, parentMap)
                    }
                }
            }
            parser.load<Any>(path, config)
            for (def in definitions) {
                def.transforms = def.transformIds?.map { if (it == -1) null else get(it).stringId }?.toTypedArray()
            }
            ids.size
        }
        return this
    }
}