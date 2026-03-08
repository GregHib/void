package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.ObjectDefinition
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

    fun load(paths: List<String>): ObjectDefinitions {
        timedLoad("object config") {
            val ids = Object2IntOpenHashMap<String>()
            val refs = Object2IntOpenHashMap<String>()
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
                                    require(obj >= 0) { "Cannot find object to clone with id '$name' in ${path}. Make sure it's in the same file." }
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
            ids.size
        }
        return this
    }
}
