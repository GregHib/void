package world.gregs.voidps.engine.data.definition.extra

import world.gregs.voidps.engine.data.decode
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.data.definition.config.RenderEmoteDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.YamlParser

class RenderEmoteDefinitions : DefinitionsDecoder<RenderEmoteDefinition> {

    override lateinit var definitions: Array<RenderEmoteDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(parser: YamlParser = get(), path: String = getProperty("renderEmoteDefinitionsPath")): RenderEmoteDefinitions {
        timedLoad("render emote definition") {
            decode<RenderEmoteDefinition>(parser, path) { id, key, _ ->
                RenderEmoteDefinition(id = id, stringId = key)
            }
        }
        return this
    }

    override fun empty() = RenderEmoteDefinition.EMPTY
}