package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.WeaponStyleDefinition
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class WeaponStyleDefinitions : DefinitionsDecoder<WeaponStyleDefinition> {

    override lateinit var definitions: Array<WeaponStyleDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(yaml: Yaml = get(), path: String = Settings["definitions.weapons.styles"]): WeaponStyleDefinitions {
        timedLoad("weapon style definition") {
            decode(yaml, path) { id, key, extras ->
                WeaponStyleDefinition.fromMap(id, key, extras!!)
            }
        }
        return this
    }

    override fun empty() = WeaponStyleDefinition.EMPTY
}