package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.ClientScriptDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class ClientScriptDefinitions : DefinitionsDecoder<ClientScriptDefinition> {

    override lateinit var definitions: Array<ClientScriptDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(yaml: Yaml = get(), path: String = Settings["definitions.clientScripts"]): ClientScriptDefinitions {
        timedLoad("client script definition") {
            decode(yaml, path) { id, key, _ ->
                ClientScriptDefinition(id = id, stringId = key)
            }
        }
        return this
    }

    override fun empty() = ClientScriptDefinition.EMPTY

}