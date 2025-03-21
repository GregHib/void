package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.timedLoad
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.pathString

class AnimationDefinitions(
    override var definitions: Array<AnimationDefinition>
) : DefinitionsDecoder<AnimationDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = AnimationDefinition.EMPTY

    fun load(dir: String = Settings["definitions.animations"]): AnimationDefinitions {
        timedLoad("animation extra") {
            val ids = Object2IntOpenHashMap<String>(definitions.size, Hash.VERY_FAST_LOAD_FACTOR)
            for (path in Files.list(Path.of(dir))) {
                if (path.extension != "toml") {
                    continue
                }
                Config.fileReader(path.pathString) {
                    while (nextSection()) {
                        val stringId = section()
                        var id = -1
                        val extras = Object2ObjectOpenHashMap<String, Any>(2, Hash.VERY_FAST_LOAD_FACTOR)
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> id = int()
                                "ticks" -> extras[key] = int()
                                else -> extras[key] = value()
                            }
                        }
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