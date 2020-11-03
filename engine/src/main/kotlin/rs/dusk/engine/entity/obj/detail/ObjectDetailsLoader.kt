package rs.dusk.engine.entity.obj.detail

import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader

class ObjectDetailsLoader(private val decoder: ObjectDecoder, private val loader: FileLoader) : TimedLoader<ObjectDetails>("object detail") {

    override fun load(args: Array<out Any?>): ObjectDetails {
        val path = args[0] as String
        val data: Map<String, Map<String, Any>> = loader.load(path)
        val names = data.map { it.value["id"] as Int to it.key }.toMap()
        count = names.size
        return ObjectDetails(decoder, data, names)
    }

}