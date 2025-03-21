package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.ClientScriptDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.timedLoad

class ClientScriptDefinitions : DefinitionsDecoder<ClientScriptDefinition> {

    override lateinit var definitions: Array<ClientScriptDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(path: String = Settings["definitions.clientScripts"]): ClientScriptDefinitions {
        timedLoad("client script definition") {
            val definitions = Array(4200) { ClientScriptDefinition.EMPTY }
            val ids = Object2IntOpenHashMap<String>(50, Hash.VERY_FAST_LOAD_FACTOR)
            Config.fileReader(path) {
                while (nextSection()) {
                    val stringId = section()
                    var id = -1
                    while (nextPair()) {
                        when (val key = key()) {
                            "id" -> id = int()
                            "params" -> while (nextElement()) {
                                string() // We don't need these
                            }
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                    }
                    ids[stringId] = id
                    definitions[id] = ClientScriptDefinition(id = id, stringId = stringId)
                }
            }
            this.definitions = definitions
            this.ids = ids
            ids.size
        }
        return this
    }

    override fun empty() = ClientScriptDefinition.EMPTY

}