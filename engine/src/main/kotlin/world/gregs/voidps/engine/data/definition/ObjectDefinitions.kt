package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.data.definition.data.Rock
import world.gregs.voidps.engine.data.definition.data.Tree
import world.gregs.voidps.engine.timedLoad

object ObjectDefinitions : DefinitionsDecoder<ObjectDefinition> {

    override lateinit var definitions: Array<ObjectDefinition>
    override lateinit var ids: Map<String, Int>

    fun getValue(id: Int): ObjectDefinition = definitions[id]

    override fun empty() = ObjectDefinition.EMPTY

    fun init(definitions: Array<ObjectDefinition>): ObjectDefinitions {
        this.definitions = definitions
        return this
    }

    @TestOnly
    fun set(definitions: Array<ObjectDefinition>, ids: Map<String, Int>) {
        this.definitions = definitions
        this.ids = ids
    }

    fun load(paths: List<String>): ObjectDefinitions {
        timedLoad("object extra") {
            val ids = Object2IntOpenHashMap<String>()
            val refs = Object2IntOpenHashMap<String>()
            ids.defaultReturnValue(-1)
            for (path in paths) {
                refs.clear()
                Config.fileReader(path) {
                    while (nextSection()) {
                        val stringId = section()
                        var id = -1
                        val extras = Object2ObjectOpenHashMap<String, Any>(4, Hash.VERY_FAST_LOAD_FACTOR)
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> id = int()
                                "woodcutting" -> extras[key] = Tree(this)
                                "mining" -> extras[key] = Rock(this)
                                "clone" -> {
                                    val name = string()
                                    val obj = refs.getInt(name)
                                    require(obj >= 0) { "Cannot find object to clone with id '$name' in ${path}. Make sure it's in the same file." }
                                    val definition = definitions[obj]
                                    extras.putAll(definition.extras ?: continue)
                                }
                                "categories" -> {
                                    val categories = ObjectLinkedOpenHashSet<String>(2, Hash.VERY_FAST_LOAD_FACTOR)
                                    while (nextElement()) {
                                        categories.add(string())
                                    }
                                    extras["categories"] = categories
                                }
                                else -> extras[key] = value()
                            }
                        }
                        require(!ids.containsKey(stringId)) { "Duplicate object id found '$stringId' at $path." }
                        ids[stringId] = id
                        refs[stringId] = id
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
