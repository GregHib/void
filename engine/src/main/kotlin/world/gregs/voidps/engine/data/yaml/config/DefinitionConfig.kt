package world.gregs.voidps.engine.data.yaml.config

import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.engine.data.yaml.parse.Parser

open class DefinitionConfig(
    val ids: MutableMap<String, Int>,
    val definitions: Array<out Extra>
): FastUtilConfiguration() {

    @Suppress("UNCHECKED_CAST")
    override fun setMapValue(parser: Parser, map: MutableMap<String, Any>, key: String, indent: Int, indentOffset: Int, withinMap: Boolean) {
        val value = parser.value(indentOffset, withinMap)
        if (value is Int && indent == 0) {
            ids[key] = value
            definitions[value].stringId = key
        } else if (indent == 0) {
            value as MutableMap<String, Any>
            val id = value.remove("id") as Int
            ids[key] = id
            definitions[id].stringId = key
            definitions[id].extras = value
        } else {
            map[key] = value
            return
        }
    }
}