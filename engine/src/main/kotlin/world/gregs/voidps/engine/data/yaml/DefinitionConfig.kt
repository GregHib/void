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
        val def = definitions[id]
        def.stringId = key
        val existing = def.extras
        if (existing != null && extras != null) {
            existing as MutableMap<String, Any>
            existing.putAll(extras)
        } else if (extras != null) {
            def.extras = extras
        }
    }
}