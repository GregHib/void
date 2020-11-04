package rs.dusk.engine.entity.character.contain.detail

import rs.dusk.cache.config.decoder.ItemContainerDecoder
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.character.contain.StackMode

class ContainerDefinitionLoader(private val loader: FileLoader, private val decoder: ItemContainerDecoder) : TimedLoader<ContainerDefinitions>("container definition") {

    override fun load(args: Array<out Any?>): ContainerDefinitions {
        val path = args[0] as String
        val data: Map<String, Map<String, Any>> = loader.load(path)
        val map = data.mapValues { entry -> entry.value.mapValues { convert(it.key, it.value) } }.toMap()
        val names = data.map { it.value["id"] as Int to it.key }.toMap()
        count = names.size
        return ContainerDefinitions(decoder, map, names)
    }

    fun convert(key: String, value: Any) : Any {
        return when(key) {
            "stack" -> StackMode.valueOf(value as String)
            else -> value
        }
    }
}