package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.character.contain.StackMode
import world.gregs.voidps.engine.entity.definition.DefinitionsDecoder.Companion.mapIds
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

class ContainerDefinitions(
    override val decoder: ContainerDecoder
) : DefinitionsDecoder<ContainerDefinition, ContainerDecoder> {

    override lateinit var extras: Map<String, Map<String, Any>>
    override lateinit var names: Map<Int, String>

    fun load(loader: FileLoader = get(), path: String = getProperty("containerDefinitionsPath")): ContainerDefinitions {
        timedLoad("container definition") {
            decoder.clear()
            load(loader.load<Map<String, Any>>(path).mapIds())
        }
        return this
    }

    fun load(data: Map<String, Map<String, Any>>): Int {
        extras = data.mapValues { (_, value) ->
            value.mapValues { convert(it.key, it.value) }
        }.toMap()
        names = data.map { it.value["id"] as Int to it.key }.toMap()
        return names.size
    }

    fun convert(key: String, value: Any) : Any {
        return when(key) {
            "stack" -> StackMode.valueOf(value as String)
            else -> value
        }
    }
}

fun ContainerDefinition.items(): Array<String> {
    val defs: ItemDefinitions = get()
    return ids?.map { defs.getName(it) }?.toTypedArray() ?: emptyArray()
}