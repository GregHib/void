package world.gregs.void.engine.entity.definition.load

import world.gregs.void.cache.definition.decoder.ObjectDecoder
import world.gregs.void.engine.TimedLoader
import world.gregs.void.engine.data.file.FileLoader
import world.gregs.void.engine.entity.definition.ObjectDefinitions

class ObjectDefinitionLoader(private val decoder: ObjectDecoder, private val loader: FileLoader) : TimedLoader<ObjectDefinitions>("object definition") {

    override fun load(args: Array<out Any?>): ObjectDefinitions {
        val path = args[0] as String
        val data: Map<String, Map<String, Any>> = loader.load(path)
        val names = data.map { it.value["id"] as Int to it.key }.toMap()
        count = names.size
        return ObjectDefinitions(decoder, data, names)
    }

}