package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * Also known as AttributeMaps in cs2
 */
class StructDefinitions(
    override var definitions: Array<StructDefinition>
) : DefinitionsDecoder<StructDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = StructDefinition.EMPTY

    fun load(yaml: Yaml = get(), path: String = Settings["structDefinitionsPath"]): StructDefinitions {
        timedLoad("struct extra") {
            decode(yaml, path)
        }
        return this
    }

}