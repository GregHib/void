package world.gregs.voidps.engine.data.definition.extra

import world.gregs.voidps.engine.data.decode
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.data.definition.config.SoundDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.YamlParser

class SoundDefinitions : DefinitionsDecoder<SoundDefinition> {

    override lateinit var definitions: Array<SoundDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(parser: YamlParser = get(), path: String = getProperty("soundDefinitionsPath")): SoundDefinitions {
        timedLoad("sound definition") {
            decode(parser, path) { id, key, _ ->
                SoundDefinition(id = id, stringId = key)
            }
        }
        return this
    }

    override fun empty() = SoundDefinition.EMPTY
}