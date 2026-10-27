package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.ClientScriptDefinition
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

class ClientScriptDefinitions : DefinitionsDecoder<ClientScriptDefinition> {

    override lateinit var definitions: Array<ClientScriptDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(paths: List<String>): ClientScriptDefinitions {
        val definitions = Array(4200) { ClientScriptDefinition.EMPTY }
        val ids = Object2IntOpenHashMap<String>(50, Hash.VERY_FAST_LOAD_FACTOR)
        for (path in paths) {
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
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()} in $path.")
                        }
                    }
                    require(id != -1) { "Missing id for client script '$stringId' at $path." }
                    require(!ids.containsKey(stringId)) { "Duplicate client script id found '$stringId' at $path." }
                    ids[stringId] = id
                    definitions[id] = ClientScriptDefinition(id = id, stringId = stringId)
                }
            }
        }
        this.definitions = definitions
        this.ids = ids
        return this
    }

    override fun empty() = ClientScriptDefinition.EMPTY

    internal fun copyFrom(other: ClientScriptDefinitions) {
        definitions = other.definitions
        ids = other.ids
    }
}

object ClientScriptLoader : Loader<ClientScriptDefinitions> {
    val instance = ClientScriptDefinitions()
    override val name = "client_scripts"
    override val files = listOf("definitions.clientScripts")

    override fun load(context: LoadContext): ClientScriptDefinitions = ClientScriptDefinitions().load(context.paths(this))

    override fun install(state: ClientScriptDefinitions) = instance.copyFrom(state)
}
