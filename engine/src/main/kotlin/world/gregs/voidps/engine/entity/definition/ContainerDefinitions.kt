package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.character.contain.StackMode
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

class ContainerDefinitions(
    decoder: ContainerDecoder
) : DefinitionsDecoded<ContainerDefinition> {

    override val definitions: Array<ContainerDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("container definition", definitions.size, start)
    }

    override fun empty() = ContainerDefinition.EMPTY

    fun load(storage: FileStorage = get(), path: String = getProperty("containerDefinitionsPath")): ContainerDefinitions {
        timedLoad("container extra") {
            val data = storage.loadMapIds(path)
            val extras = data.mapValues { (_, value) ->
                value.mapValues { convert(it.key, it.value) }
            }.toMap()
            val names = data.map { it.value["id"] as Int to it.key }.toMap()
            ids = data.map { it.key to it.value["id"] as Int }.toMap()
            apply(names, extras)
            names.size
        }
        return this
    }

    fun convert(key: String, value: Any) : Any {
        return when(key) {
            "stack" -> StackMode.valueOf(value as String)
            else -> value
        }
    }
}

fun ContainerDefinition.items(): List<String> {
    val defs: ItemDefinitions = get()
    return ids?.map { defs.get(it).stringId } ?: emptyList()
}