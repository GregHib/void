package world.gregs.voidps.engine.data.definition.extra

import org.yaml.snakeyaml.Yaml
import world.gregs.voidps.engine.client.variable.VariableType
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.mapIds
import world.gregs.voidps.engine.data.definition.config.VariableDefinition
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty
import java.io.File

class VariableDefinitions {

    private lateinit var definitions: Map<String, VariableDefinition>
    private lateinit var ids: Map<VariableType, Map<Int, String>>

    fun getKey(type: VariableType, id: Int) = ids[type]?.get(id)

    fun get(key: String) = definitions[key]

    fun getValue(key: String) = definitions.getValue(key)

    fun load(storage: FileStorage = get(), path: String = getProperty("variableDefinitionsPath")): VariableDefinitions {
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