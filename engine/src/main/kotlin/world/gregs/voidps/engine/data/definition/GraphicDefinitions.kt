package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.engine.data.load.DefinitionsCodec
import world.gregs.voidps.engine.data.load.DefinitionsState
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object GraphicDefinitions : DefinitionsDecoder<GraphicDefinition> {

    override var definitions: Array<GraphicDefinition> = emptyArray()
    override var ids: Map<String, Int> = emptyMap()

    var loaded = false
        private set

    val size: Int
        get() = definitions.size

    override fun empty() = GraphicDefinition.EMPTY

    fun init(definitions: Array<GraphicDefinition>): GraphicDefinitions {
        this.definitions = definitions
        loaded = true
        return this
    }

    @TestOnly
    fun set(definitions: Array<GraphicDefinition>, ids: Map<String, Int>) {
        this.definitions = definitions
        this.ids = ids
        loaded = true
    }

    fun clear() {
        this.definitions = emptyArray()
        this.ids = emptyMap()
        loaded = false
    }

    fun install(state: DefinitionsState<GraphicDefinition>) {
        definitions = state.definitions
        ids = state.ids
        loaded = true
    }

    /**
     * Applies the configs in [paths] to the already decoded [definitions].
     */
    fun load(paths: List<String>): GraphicDefinitions {
        ids = GraphicLoader.parse(definitions, paths)
        return this
    }
}

object GraphicLoader : Loader<DefinitionsState<GraphicDefinition>> {
    override val name = "graphics"
    override val files = listOf("definitions.graphics")
    override val usesCache = true
    override val codec = DefinitionsCodec(
        GraphicDefinition::class.java,
        default = null,
        writeDefinition = { definition ->
            int(definition.id)
            int(definition.modelId)
            int(definition.animationId)
            int(definition.sizeXY)
            int(definition.sizeZ)
            int(definition.rotation)
            int(definition.ambience)
            int(definition.contrast)
            byte(definition.aByte2381.toInt())
            int(definition.anInt2385)
            boolean(definition.aBoolean2402)
            nullableShorts(definition.originalColours)
            nullableShorts(definition.modifiedColours)
            nullableShorts(definition.originalTextureColours)
            nullableShorts(definition.modifiedTextureColours)
            string(definition.stringId)
            params(definition.params)
        },
        readDefinition = {
            GraphicDefinition(
                id = int(),
                modelId = int(),
                animationId = int(),
                sizeXY = int(),
                sizeZ = int(),
                rotation = int(),
                ambience = int(),
                contrast = int(),
                aByte2381 = byte().toByte(),
                anInt2385 = int(),
                aBoolean2402 = boolean(),
                originalColours = nullableShorts(),
                modifiedColours = nullableShorts(),
                originalTextureColours = nullableShorts(),
                modifiedTextureColours = nullableShorts(),
                stringId = string(),
                params = params(),
            )
        },
    )

    override fun load(context: LoadContext): DefinitionsState<GraphicDefinition> {
        val definitions = GraphicDecoder().load(context.requireCache())
        return DefinitionsState(definitions, parse(definitions, context.paths(this)))
    }

    override fun install(state: DefinitionsState<GraphicDefinition>) = GraphicDefinitions.install(state)

    fun parse(definitions: Array<GraphicDefinition>, paths: List<String>): Map<String, Int> {
        val ids = Object2IntOpenHashMap<String>(definitions.size, Hash.VERY_FAST_LOAD_FACTOR)
        for (path in paths) {
            Config.fileReader(path) {
                while (nextSection()) {
                    val stringId = section()
                    var id = -1
                    val params = Int2ObjectOpenHashMap<Any>(0)
                    while (nextPair()) {
                        when (val key = key()) {
                            "id" -> id = int()
                            "angle" -> throw IllegalArgumentException("Unknown key 'angle' use 'curve' instead. ${exception()}")
                            else -> params[Params.id(key)] = value()
                        }
                    }
                    require(!ids.containsKey(stringId)) { "Duplicate graphics id found '$stringId' at $path." }
                    require(id != -1) { "Missing id for graphic '$stringId' at $path." }
                    ids[stringId] = id
                    definitions[id].stringId = stringId
                    definitions[id].params = params.ifEmpty { null }
                }
            }
        }
        return ids
    }
}
