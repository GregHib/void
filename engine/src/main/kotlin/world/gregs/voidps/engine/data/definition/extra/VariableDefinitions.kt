package world.gregs.voidps.engine.data.definition.extra

import org.yaml.snakeyaml.Yaml
import world.gregs.voidps.engine.client.variable.VariableType
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.mapIds
import world.gregs.voidps.engine.data.definition.config.VariableDefinition
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import java.io.File
import kotlin.collections.Map
import kotlin.collections.associateWith
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.emptyMap
import kotlin.collections.filter
import kotlin.collections.getValue
import kotlin.collections.iterator
import kotlin.collections.map
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.collections.toMap

class VariableDefinitions {

    private var definitions: Map<String, VariableDefinition> = emptyMap()
    private var ids: Map<VariableType, Map<Int, String>> = emptyMap()

    fun getKey(type: VariableType, id: Int) = ids[type]?.get(id)

    fun get(key: String) = definitions[key]

    fun getValue(key: String) = definitions.getValue(key)

    fun load(path: String = getProperty("variableDefinitionsPath")): VariableDefinitions {
        timedLoad("variable definition") {
            // Jackson yaml doesn't support anchors - https://github.com/FasterXML/jackson-dataformats-text/issues/98
            val yaml = Yaml()
            val data: Map<String, Any> = yaml.load(File(path).readText(Charsets.UTF_8))
            load(data.filter { it.key != "anchors" }.mapIds())
        }
        return this
    }

    fun load(data: Map<String, Map<String, Any>>): Int {
        definitions = data.map { (key, value) -> key to VariableDefinition(value) }.toMap()
        val maps = VariableType.values().associateWith { mutableMapOf<Int, String>() }
        for (def in definitions) {
            maps.getValue(def.value.type)[def.value.id] = def.key
        }
        this.ids = maps
        return definitions.size
    }

}