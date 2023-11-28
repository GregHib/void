package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.Parameters
import world.gregs.voidps.engine.data.config.ParameterDefinition
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class ParameterDefinitions : DefinitionsDecoder<ParameterDefinition>, Parameters {

    override lateinit var definitions: Array<ParameterDefinition>
    override lateinit var ids: Map<String, Int>
    override lateinit var parameters: Map<Int, String>

    fun load(yaml: Yaml = get(), path: String = getProperty("parameterDefinitionsPath")): ParameterDefinitions {
        timedLoad("parameter definition") {
            val size = decode(yaml, path) { id, key, _ ->
                ParameterDefinition(id = id, stringId = key)
            }
            parameters = definitions.associate { it.id to it.stringId }
            size
        }
        return this
    }

    override fun empty() = ParameterDefinition.EMPTY

}