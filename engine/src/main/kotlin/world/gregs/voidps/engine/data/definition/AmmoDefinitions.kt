package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.config.AmmoDefinition
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class AmmoDefinitions : DefinitionsDecoder<AmmoDefinition> {

    override lateinit var definitions: Array<AmmoDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(yaml: Yaml = get(), path: String = getProperty("ammoDefinitionsPath")): AmmoDefinitions {
        timedLoad("ammo definition") {
            decode(yaml, path) { id, key, _ ->
                AmmoDefinition(id = id, stringId = key)
            }
        }
        return this
    }

    override fun empty() = AmmoDefinition.EMPTY

}