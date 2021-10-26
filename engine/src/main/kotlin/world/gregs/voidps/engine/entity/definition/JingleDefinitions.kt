package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.engine.data.file.FileStorage
import world.gregs.voidps.engine.entity.definition.DefinitionsDecoder.Companion.mapIds
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

class JingleDefinitions : Definitions<JingleDefinition> {

    override lateinit var extras: Map<String, Map<String, Any>>
    override lateinit var names: Map<Int, String>

    fun load(storage: FileStorage = get(), path: String = getProperty("jingleDefinitionsPath")): JingleDefinitions {
        timedLoad("jingle definition") {
            load(storage.load<Map<String, Any>>(path).mapIds())
        }
        return this
    }

    fun load(data: Map<String, Map<String, Any>>): Int {
        names = data.map { it.value["id"] as Int to it.key }.toMap()
        this.extras = data
        return names.size
    }

    override fun decodeOrNull(name: String, id: Int) = if (extras.containsKey(name)) JingleDefinition(id, stringId = names.getValue(id)) else null

    override fun decode(name: String, id: Int) = decodeOrNull(name, id) ?: JingleDefinition(id, stringId = name)

}