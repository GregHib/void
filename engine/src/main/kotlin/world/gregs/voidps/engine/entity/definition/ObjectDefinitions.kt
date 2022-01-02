package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.data.file.FileStorage
import world.gregs.voidps.engine.entity.definition.data.MiningRock
import world.gregs.voidps.engine.entity.definition.data.Tree
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

class ObjectDefinitions(
    override val decoder: ObjectDecoder
) : DefinitionsDecoder<ObjectDefinition, ObjectDecoder>() {

    override lateinit var extras: Map<String, Map<String, Any>>
    override lateinit var names: Map<Int, String>

    init {
        modifications["woodcutting"] = { Tree(it as Map<String, Any>) }
        modifications["mining"] = { MiningRock(it as Map<String, Any>) }
    }

    fun load(storage: FileStorage = get(), path: String = getProperty("objectDefinitionsPath")): ObjectDefinitions {
        timedLoad("object definition") {
            decoder.clear()
            load(storage.load<Map<String, Any>>(path).mapIds())
        }
        return this
    }

    fun load(data: Map<String, Map<String, Any>>): Int {
        extras = data.mapModifications()
        names = extras.map { it.value["id"] as Int to it.key }.toMap()
        return names.size
    }
}