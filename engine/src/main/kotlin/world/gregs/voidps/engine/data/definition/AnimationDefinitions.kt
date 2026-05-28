package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.engine.timedLoad

object AnimationDefinitions : DefinitionsDecoder<AnimationDefinition> {

    override var definitions: Array<AnimationDefinition> = emptyArray()
    override var ids: Map<String, Int> = emptyMap()

    var loaded = false
        private set

    val size: Int
        get() = ItemDefinitions.definitions.size

    override fun empty() = AnimationDefinition.EMPTY

    fun init(definitions: Array<AnimationDefinition>): AnimationDefinitions {
        this.definitions = definitions
        loaded = true
        return this
    }

    @TestOnly
    fun set(definitions: Array<AnimationDefinition>, ids: Map<String, Int>) {
        this.definitions = definitions
        this.ids = ids
        loaded = true
    }

    fun clear() {
        this.definitions = emptyArray()
        this.ids = emptyMap()
        loaded = false
    }

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
