package world.gregs.voidps.engine.data.yaml

import world.gregs.yaml.read.YamlReaderConfiguration

open class DefinitionIdsConfig : YamlReaderConfiguration(2, 2) {

    @Suppress("UNCHECKED_CAST")
    override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
        if (indent == 0 && value is Int) {
            set(map, key, value, null)
        } else if (indent == 0) {
            value as MutableMap<String, Any>
            val id = value.remove("id") as Int
            set(map, key, id, value)
        } else {
            super.set(map, key, value, indent, parentMap)
        }
    }

    open fun set(map: MutableMap<String, Any>, key: String, id: Int, extras: Map<String, Any>?) {
    }
}