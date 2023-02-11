package world.gregs.voidps.engine.data.definition.extra

import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.data.definition.config.SoundDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad

class SoundDefinitions : DefinitionsDecoder<SoundDefinition> {

    override lateinit var definitions: Array<SoundDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(storage: FileStorage = get(), path: String = getProperty("soundDefinitionsPath")): SoundDefinitions {
        timedLoad("sound definition") {
            val data = storage.loadMapIds(path)
            definitions = Array(data.maxOf { it.value["id"] as Int }) { SoundDefinition(id = it, stringId = it.toString()) }
            decode(data)
        }
        return this
    }

    override fun empty() = SoundDefinition.EMPTY
}