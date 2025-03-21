package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.timedLoad
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.pathString

class GraphicDefinitions(
    override var definitions: Array<GraphicDefinition>
) : DefinitionsDecoder<GraphicDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = GraphicDefinition.EMPTY

    fun load(dir: String = Settings["definitions.graphics"]): GraphicDefinitions {
        timedLoad("graphic extra") {
            val ids = Object2IntOpenHashMap<String>(definitions.size, Hash.VERY_FAST_LOAD_FACTOR)
            for (path in Files.list(Path.of(dir))) {
                if (path.extension != "toml") {
                    continue
                }
                Config.fileReader(path.pathString) {
                    while (nextSection()) {
                        val stringId = section()
                        var id = 0
                        val extras = Object2ObjectOpenHashMap<String, Any>(0)
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> id = int()
                                else -> extras[key] = value()
                            }
                        }
                        ids[stringId] = id
                        definitions[id].stringId = stringId
                        definitions[id].extras = extras.ifEmpty { null }
                    }
                }
            }
            this.ids = ids
            ids.size
        }
        return this
    }

}