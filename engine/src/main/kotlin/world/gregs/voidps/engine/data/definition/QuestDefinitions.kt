package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.config.data.QuestDefinition
import world.gregs.voidps.engine.timedLoad

class QuestDefinitions : DefinitionsDecoder<QuestDefinition> {

    override lateinit var definitions: Array<QuestDefinition>
    override lateinit var ids: Map<String, Int>

    fun load(path: String): QuestDefinitions {
        timedLoad("quest definition") {
            val definitions = Array(300) { QuestDefinition.EMPTY }
            val ids = Object2IntOpenHashMap<String>(256, Hash.VERY_FAST_LOAD_FACTOR)
            Config.fileReader(path, 600) {
                while (nextSection()) {
                    val stringId = section()
                    var id = -1
                    val extras = Object2ObjectOpenHashMap<String, Any>(16, Hash.VERY_FAST_LOAD_FACTOR)
                    while (nextPair()) {
                        when (val key = key()) {
                            "id" -> id = int()
                            else -> extras[key] = value()
                        }
                    }
                    require(!ids.containsKey(stringId)) { "Duplicate quest id found '$stringId' at $path." }
                    ids[stringId] = id
                    if (extras.isNotEmpty()) {
                        definitions[id] = QuestDefinition(id = id, stringId = stringId, extras = extras)
                    } else {
                        definitions[id] = QuestDefinition(id = id, stringId = stringId)
                    }
                }
            }
            this.definitions = definitions
            this.ids = ids
            ids.size
        }
        return this
    }

    override fun empty() = QuestDefinition.EMPTY

}