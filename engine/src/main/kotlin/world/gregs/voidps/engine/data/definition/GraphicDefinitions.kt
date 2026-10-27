package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.engine.data.Snapshot
import world.gregs.voidps.engine.timedLoad

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

    fun load(paths: List<String>): GraphicDefinitions {
        timedLoad("graphic config") {
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
            this.ids = ids
            ids.size
        }
        return this
    }

    fun saveDefinitions(snapshot: Snapshot) = snapshot.write {
        int(definitions.size)
        for (definition in definitions) {
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
        }
        ids(ids)
    }

    fun restoreDefinitions(snapshot: Snapshot): Boolean = snapshot.restore { reader ->
        val definitions = Array(reader.int()) {
            GraphicDefinition(
                id = reader.int(),
                modelId = reader.int(),
                animationId = reader.int(),
                sizeXY = reader.int(),
                sizeZ = reader.int(),
                rotation = reader.int(),
                ambience = reader.int(),
                contrast = reader.int(),
                aByte2381 = reader.byte().toByte(),
                anInt2385 = reader.int(),
                aBoolean2402 = reader.boolean(),
                originalColours = reader.nullableShorts(),
                modifiedColours = reader.nullableShorts(),
                originalTextureColours = reader.nullableShorts(),
                modifiedTextureColours = reader.nullableShorts(),
                stringId = reader.string(),
                params = reader.params(),
            )
        }
        val ids = reader.ids()
        init(definitions)
        this.ids = ids
    }
}
