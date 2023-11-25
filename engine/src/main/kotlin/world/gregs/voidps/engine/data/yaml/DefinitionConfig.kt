package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.engine.data.definition.parameterNames

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
        setExtras(def, extras)
        super.set(map, key, id, extras)
    }

    private fun setExtras(definition: Extra, extras: Map<String, Any>?) {
        definition.extras = extras
        if (definition !is Parameterized) {
            return
        }
        val parameters = definition.params ?: return
        val params = extras as? MutableMap<String, Any> ?: createMap()
        definition as Definition
        for (pair in parameters) {
            setParam(pair.key, pair.value, params, parameters)
        }
        definition.extras = params
//        definition.params = null
    }

    open fun setParam(key: Long, value: Any, extras: MutableMap<String, Any>, parameters: Map<Long, Any>) {
        val name = parameterNames.getOrDefault(key, key.toString())
        extras[name] = value
    }

}