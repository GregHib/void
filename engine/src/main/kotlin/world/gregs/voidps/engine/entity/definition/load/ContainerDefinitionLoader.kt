package world.gregs.voidps.engine.entity.definition.load

import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.engine.TimedLoader
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.character.contain.StackMode
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions

class ContainerDefinitionLoader(private val loader: FileLoader, private val decoder: ContainerDecoder) : TimedLoader<ContainerDefinitions>("container definition") {

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