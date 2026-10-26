package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.data.Snapshot
import world.gregs.voidps.engine.timedLoad

object ObjectDefinitions : DefinitionsDecoder<ObjectDefinition> {

    override lateinit var definitions: Array<ObjectDefinition>
    override lateinit var ids: Map<String, Int>

    fun getValue(id: Int): ObjectDefinition = definitions[id]

    override fun empty() = ObjectDefinition.EMPTY

    var loaded = false
        private set

    fun init(definitions: Array<ObjectDefinition>): ObjectDefinitions {
        this.definitions = definitions
        loaded = true
        return this
    }

    @TestOnly
    fun set(definitions: Array<ObjectDefinition>, ids: Map<String, Int>) {
        this.definitions = definitions
        this.ids = ids
        loaded = true
    }

    fun clear() {
        definitions = emptyArray()
        ids = emptyMap()
        loaded = false
    }

    fun load(paths: List<String>, snapshot: Snapshot? = null): ObjectDefinitions {
        timedLoad("object config") {
            if (snapshot != null && restore(snapshot)) {
                return@timedLoad ids.size
            }
            val ids = Object2IntOpenHashMap<String>()
            val refs = Object2IntOpenHashMap<String>()
            val intIds = IntOpenHashSet()
            ids.defaultReturnValue(-1)
            for (path in paths) {
                refs.clear()
                Config.fileReader(path) {
                    while (nextSection()) {
                        val stringId = section()
                        var id = -1
                        val params = Int2ObjectOpenHashMap<Any>(4, Hash.VERY_FAST_LOAD_FACTOR)
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> id = int()
                                "clone" -> {
                                    val name = string()
                                    val obj = refs.getInt(name)
                                    require(obj >= 0) { "Cannot find object to clone with id '$name' in $path. Make sure it's in the same file." }
                                    val definition = definitions[obj]
                                    params.putAll(definition.params ?: continue)
                                }
                                "categories" -> {
                                    val categories = ObjectLinkedOpenHashSet<String>(2, Hash.VERY_FAST_LOAD_FACTOR)
                                    while (nextElement()) {
                                        categories.add(string())
                                    }
                                    params[Params.CATEGORIES] = categories
                                }
                                else -> params[Params.id(key)] = value()
                            }
                        }
                        require(!ids.containsKey(stringId)) { "Duplicate object id found '$stringId' at $path." }
                        require(intIds.add(id)) { "Duplicate object id $id found '$stringId' at $path, already defined as '${definitions[id].stringId}'." }
                        ids[stringId] = id
                        refs[stringId] = id
                        definitions[id].stringId = stringId
                        if (params.isNotEmpty()) {
                            definitions[id].params = params
                        }
                    }
                }
            }
            this.ids = ids
            snapshot?.write {
                int(ids.size)
                for ((stringId, id) in ids) {
                    string(stringId)
                    int(id)
                    params(definitions[id].params)
                }
            }
            ids.size
        }
        return this
    }

    private fun restore(snapshot: Snapshot): Boolean = snapshot.restore { reader ->
        val size = reader.int()
        val ids = Object2IntOpenHashMap<String>(size)
        ids.defaultReturnValue(-1)
        val strings = arrayOfNulls<String>(size)
        val ints = IntArray(size)
        val params = arrayOfNulls<Map<Int, Any>>(size)
        for (i in 0 until size) {
            strings[i] = reader.string()
            ints[i] = reader.int()
            params[i] = reader.params()
        }
        for (i in 0 until size) {
            val id = ints[i]
            ids[strings[i]] = id
            definitions[id].stringId = strings[i]!!
            val map = params[i]
            if (map != null) {
                definitions[id].params = map
            }
        }
        this.ids = ids
    }

    /**
     * Saves every definition as it is after loading, so that decoding the cache and parsing configs can both be skipped.
     */
    fun saveDefinitions(snapshot: Snapshot) = snapshot.write {
        int(definitions.size)
        for (definition in definitions) {
            int(definition.id)
            string(definition.name)
            int(definition.sizeX)
            int(definition.sizeY)
            int(definition.solid)
            int(definition.interactive)
            nullableStrings(definition.options)
            boolean(definition.mirrored)
            int(definition.blockFlag)
            int(definition.varbit)
            int(definition.varp)
            nullableInts(definition.transforms)
            string(definition.stringId)
            params(definition.params)
            int(definition.block)
        }
        int(ids.size)
        for ((stringId, id) in ids) {
            string(stringId)
            int(id)
        }
    }

    fun restoreDefinitions(snapshot: Snapshot): Boolean = snapshot.restore { reader ->
        val definitions = Array(reader.int()) {
            val definition = ObjectDefinition(
                id = reader.int(),
                name = reader.string(),
                sizeX = reader.int(),
                sizeY = reader.int(),
                solid = reader.int(),
                interactive = reader.int(),
                options = reader.nullableStrings(),
                mirrored = reader.boolean(),
                blockFlag = reader.int(),
                varbit = reader.int(),
                varp = reader.int(),
                transforms = reader.nullableInts(),
                stringId = reader.string(),
                params = reader.params(),
            )
            definition.block = reader.int()
            definition
        }
        val ids = Object2IntOpenHashMap<String>()
        ids.defaultReturnValue(-1)
        repeat(reader.int()) {
            val stringId = reader.string()
            ids[stringId] = reader.int()
        }
        init(definitions)
        this.ids = ids
    }
}
