package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.data.definition.data.Pickable
import world.gregs.voidps.engine.data.definition.data.Rock
import world.gregs.voidps.engine.data.definition.data.Tree
import world.gregs.voidps.engine.timedLoad
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.pathString

class ObjectDefinitions(
    override var definitions: Array<ObjectDefinition>
) : DefinitionsDecoder<ObjectDefinition> {

    override lateinit var ids: Map<String, Int>

    fun getValue(id: Int): ObjectDefinition {
        return definitions[id]
    }

    override fun empty() = ObjectDefinition.EMPTY

    fun load(paths: List<String>): ObjectDefinitions {
        timedLoad("object extra") {
            val ids = Object2IntOpenHashMap<String>()
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextSection()) {
                        val stringId = section()
                        var id = -1
                        val extras = Object2ObjectOpenHashMap<String, Any>(4, Hash.VERY_FAST_LOAD_FACTOR)
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> id = int()
                                "pickable" -> extras[key] = Pickable(this)
                                "woodcutting" -> extras[key] = Tree(this)
                                "mining" -> extras[key] = Rock(this)
                                "clone" -> {
                                    val name = string()
                                    val npc = ids.getInt(name)
                                    require(npc >= 0) { "Cannot find object id to clone '$name'" }
                                    val definition = definitions[npc]
                                    extras.putAll(definition.extras ?: continue)
                                }
                                else -> extras[key] = value()
                            }
                        }
                        require(!ids.containsKey(stringId)) { "Duplicate object id found '$stringId' at $path." }
                        ids[stringId] = id
                        definitions[id].stringId = stringId
                        if (extras.isNotEmpty()) {
                            definitions[id].extras = extras
                        }
                    }
                }
            }
            this.ids = ids
            ids.size
        }
        return this
    }
}