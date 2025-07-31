package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.engine.timedLoad

class AnimationDefinitions(
    override var definitions: Array<AnimationDefinition>,
) : DefinitionsDecoder<AnimationDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = AnimationDefinition.EMPTY

    fun load(paths: List<String>): AnimationDefinitions {
        timedLoad("animation extra") {
            val ids = Object2IntOpenHashMap<String>(definitions.size, Hash.VERY_FAST_LOAD_FACTOR)
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextSection()) {
                        val stringId = section()
                        var id = -1
                        val extras = Object2ObjectOpenHashMap<String, Any>(2, Hash.VERY_FAST_LOAD_FACTOR)
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> id = int()
                                "ticks" -> extras[key] = int()
                                "infinite" -> extras[key] = boolean()
                                "walk" -> extras[key] = boolean()
                                "run" -> extras[key] = boolean()
                                else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                            }
                        }
                        require(!ids.containsKey(stringId)) { "Duplicate animation id found '$stringId' at $path." }
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
