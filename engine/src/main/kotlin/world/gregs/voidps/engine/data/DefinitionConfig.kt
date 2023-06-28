package world.gregs.voidps.engine.data

import world.gregs.voidps.cache.definition.Extra
import world.gregs.yaml.config.DefinitionIdsConfig

open class DefinitionConfig<T : Extra>(
    val ids: MutableMap<String, Int>,
    val definitions: Array<T>
) : DefinitionIdsConfig() {
    override fun set(map: MutableMap<String, Any>, key: String, id: Int, extras: Map<String, Any>?) {
        if (id < 0) {
            return
        }
        ids[key] = id
        definitions[id].stringId = key
        definitions[id].extras = extras
        super.set(map, key, id, extras)
    }
}