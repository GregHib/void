package world.gregs.voidps.engine.data.yaml.config

import world.gregs.voidps.cache.definition.Extra

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
    }
}