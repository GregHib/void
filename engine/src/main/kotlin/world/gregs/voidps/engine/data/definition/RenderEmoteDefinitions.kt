package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.RenderEmoteDefinition
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class RenderEmoteDefinitions : DefinitionsDecoder<RenderEmoteDefinition> {

    override lateinit var definitions: Array<RenderEmoteDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(yaml: Yaml = get(), path: String = Settings["renderEmoteDefinitionsPath"]): RenderEmoteDefinitions {
        timedLoad("render emote definition") {
            decode<RenderEmoteDefinition>(yaml, path) { id, key, _ ->
                RenderEmoteDefinition(id = id, stringId = key)
            }
        }
        return this
    }

    override fun empty() = RenderEmoteDefinition.EMPTY
}