package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.engine.timedLoad

class AnimationDefinitions(
    override var definitions: Array<AnimationDefinition>,
) : DefinitionsDecoder<AnimationDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = AnimationDefinition.EMPTY

    fun load(paths: List<String>): AnimationDefinitions {
        timedLoad("animation config") {
            val ids = Object2IntOpenHashMap<String>(definitions.size, Hash.VERY_FAST_LOAD_FACTOR)
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextSection()) {
                        val stringId = section()
                        var id = -1
                        val params = Object2ObjectOpenHashMap<Int, Any>(2, Hash.VERY_FAST_LOAD_FACTOR)
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> id = int()
                                "ticks" -> params[Params.TICKS] = int()
                                "infinite" -> params[Params.INFINITE] = boolean()
                                "walk" -> params[Params.WALK] = boolean()
                                "run" -> params[Params.RUN] = boolean()
                                else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                            }
                        }
                        require(!ids.containsKey(stringId)) { "Duplicate animation id found '$stringId' at $path." }
                        require(id != -1) { "Missing id for animation '$stringId' at $path." }
                        ids[stringId] = id
                        definitions[id].stringId = stringId
                        if (params.isNotEmpty()) {
                            definitions[id].params = params
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
