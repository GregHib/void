package world.gregs.voidps.engine.data.definition.extra

import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.data.definition.config.RenderEmoteDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad

class RenderEmoteDefinitions : DefinitionsDecoder<RenderEmoteDefinition> {

    override lateinit var definitions: Array<RenderEmoteDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(storage: FileStorage = get(), path: String = getProperty("renderEmoteDefinitionsPath")): RenderEmoteDefinitions {
        timedLoad("render emote definition") {
            val data = storage.loadMapIds(path)
            definitions = Array(data.maxOf { it.value["id"] as Int }) { RenderEmoteDefinition(id = it, stringId = it.toString()) }
            decode(data)
        }
        return this
    }

    override fun empty() = RenderEmoteDefinition.EMPTY
}