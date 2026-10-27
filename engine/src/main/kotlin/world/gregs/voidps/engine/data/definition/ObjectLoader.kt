package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.load.DefinitionsState
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat
import world.gregs.voidps.engine.data.snapshot.serializer.DefinitionsStateSerializer

object ObjectLoader : Loader<DefinitionsState<ObjectDefinition>> {
    override val name = "objects"
    override val files = listOf("definitions.objects")
    override val settings = listOf("world.members")
    override val usesCache = true
    override val codec = SnapshotFormat.codec(DefinitionsStateSerializer(ObjectDefinition::class, ObjectDefinition.serializer(), default = -1))

    override fun load(context: LoadContext): DefinitionsState<ObjectDefinition> {
        val definitions = ObjectDecoder(Settings["world.members", false], lowDetail = false).load(context.requireCache())
        return DefinitionsState(definitions, parse(definitions, context.paths(this)))
    }

    override fun install(state: DefinitionsState<ObjectDefinition>) = ObjectDefinitions.install(state)

    fun parse(definitions: Array<ObjectDefinition>, paths: List<String>): Map<String, Int> {
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
        return ids
    }
}
