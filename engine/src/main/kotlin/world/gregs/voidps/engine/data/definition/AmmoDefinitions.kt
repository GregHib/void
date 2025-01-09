package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.AmmoDefinition
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * List of [AmmoDefinition.items] each weapons ammo_group can use ignoring
 * skill requirements and whether inside dungeoneering.
 */
class AmmoDefinitions : DefinitionsDecoder<AmmoDefinition> {

    override lateinit var definitions: Array<AmmoDefinition>
    override lateinit var ids: Map<String, Int>

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["ammoDefinitionsPath"]): AmmoDefinitions {
        timedLoad("ammo definition") {
            decode(yaml, path) { id, key, extras ->
                val items = extras?.get("items") as? List<String>
                AmmoDefinition(
                    id = id,
                    items = if (items != null) ObjectOpenHashSet(items) else emptySet(),
                    stringId = key
                )
            }
        }
        return this
    }

    override fun empty() = AmmoDefinition.EMPTY

}