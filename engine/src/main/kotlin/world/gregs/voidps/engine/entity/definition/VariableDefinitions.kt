package world.gregs.voidps.engine.entity.definition

import org.yaml.snakeyaml.Yaml
import world.gregs.voidps.engine.data.file.FileStorage
import world.gregs.voidps.engine.entity.definition.DefinitionsDecoder.Companion.mapIds
import world.gregs.voidps.engine.entity.definition.config.VariableDefinition
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty
import java.io.File

class VariableDefinitions {

    private lateinit var definitions: Map<String, VariableDefinition>
    private lateinit var ids: Map<Int, String>

    fun getKey(id: Int) = ids[id]

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
        ids = definitions.map { it.value.id to it.key }.toMap()
        return definitions.size
    }

}