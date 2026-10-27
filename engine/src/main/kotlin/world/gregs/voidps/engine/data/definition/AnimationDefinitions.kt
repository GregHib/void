package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder
import world.gregs.voidps.engine.data.load.DefinitionsState
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.DefinitionsStateSerializer
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat

object AnimationDefinitions : DefinitionsDecoder<AnimationDefinition> {

    override var definitions: Array<AnimationDefinition> = emptyArray()
    override var ids: Map<String, Int> = emptyMap()

    var loaded = false
        private set

    val size: Int
        get() = definitions.size

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

    fun install(state: DefinitionsState<AnimationDefinition>) {
        definitions = state.definitions
        ids = state.ids
        loaded = true
    }

    /**
     * Applies the configs in [paths] to the already decoded [definitions].
     */
    fun load(paths: List<String>): AnimationDefinitions {
        ids = AnimationLoader.parse(definitions, paths)
        return this
    }
}

object AnimationLoader : Loader<DefinitionsState<AnimationDefinition>> {
    override val name = "animations"
    override val files = listOf("definitions.animations")
    override val usesCache = true
    override val codec = SnapshotFormat.codec(DefinitionsStateSerializer(AnimationDefinition::class, AnimationDefinition.serializer(), default = null))

    override fun load(context: LoadContext): DefinitionsState<AnimationDefinition> {
        val definitions = AnimationDecoder().load(context.requireCache())
        return DefinitionsState(definitions, parse(definitions, context.paths(this)))
    }

    override fun install(state: DefinitionsState<AnimationDefinition>) = AnimationDefinitions.install(state)

    fun parse(definitions: Array<AnimationDefinition>, paths: List<String>): Map<String, Int> {
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
        return ids
    }
}
