package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.cache.definition.Extra

open class DefinitionConfig<T : Extra>(
    val ids: MutableMap<String, Int>,
    val definitions: Array<T>
) : DefinitionIdsConfig() {
    override fun set(map: MutableMap<String, Any>, key: String, id: Int, extras: Map<String, Any>?) {
        if (id !in definitions.indices) {
            return
        }
        ids[key] = id
        definitions[id].stringId = key
        definitions[id].extras = extras
        super.set(map, key, id, extras)
    }
}