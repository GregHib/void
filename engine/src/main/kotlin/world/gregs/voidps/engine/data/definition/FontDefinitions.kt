package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.FontDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class FontDefinitions(
    override var definitions: Array<FontDefinition>
) : DefinitionsDecoder<FontDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = FontDefinition.EMPTY

    fun load(yaml: Yaml = get(), path: String = Settings["definitions.fonts"]): FontDefinitions {
        timedLoad("font extra") {
            decode(yaml, path)
        }
        return this
    }

}