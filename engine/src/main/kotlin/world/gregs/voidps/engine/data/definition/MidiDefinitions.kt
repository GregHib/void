package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.data.config.MidiDefinition
import world.gregs.voidps.engine.timedLoad

class MidiDefinitions : DefinitionsDecoder<MidiDefinition> {

    override lateinit var definitions: Array<MidiDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(paths: List<String>): MidiDefinitions {
        timedLoad("midi definition") {
            val ids = Object2IntOpenHashMap<String>(50, Hash.VERY_FAST_LOAD_FACTOR)
            val definitions = Array(4000) { MidiDefinition.EMPTY }
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextSection()) {
                        val stringId = section()
                        var id = -1
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> id = int()
                                else -> throw IllegalArgumentException("Unknown midi key: $key")
                            }
                        }
                        ids[stringId] = id
                        definitions[id] = MidiDefinition(id = id, stringId = stringId)
                    }
                }
            }
            this.definitions = definitions
            this.ids = ids
            ids.size
        }
        return this
    }

    override fun empty() = MidiDefinition.EMPTY

}