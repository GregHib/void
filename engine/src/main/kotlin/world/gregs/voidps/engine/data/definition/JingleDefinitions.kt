package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.data.config.JingleDefinition
import world.gregs.voidps.engine.timedLoad

class JingleDefinitions : DefinitionsDecoder<JingleDefinition> {

    override lateinit var definitions: Array<JingleDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(paths: List<String>): JingleDefinitions {
        timedLoad("jingle definition") {
            val ids = Object2IntOpenHashMap<String>(200, Hash.VERY_FAST_LOAD_FACTOR)
            val definitions = Array(500) { JingleDefinition.EMPTY }
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextPair()) {
                        val key = key()
                        val id = int()
                        ids[key] = id
                        definitions[id] = JingleDefinition(id = id, stringId = key)
                    }
                }
            }
            this.definitions = definitions
            this.ids = ids
            ids.size
        }
        return this
    }

    override fun empty() = JingleDefinition.EMPTY

}