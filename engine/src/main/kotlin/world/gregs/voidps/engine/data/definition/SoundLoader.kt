package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.data.config.SoundDefinition
import world.gregs.voidps.engine.data.load.DefinitionsState
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat

object SoundLoader : Loader<DefinitionsState<SoundDefinition>> {
    val instance = SoundDefinitions()
    override val name = "sounds"
    override val label = "sound definition"
    override val files = listOf("definitions.sounds")
    override val codec = SnapshotFormat.codec(SoundStateSerializer)

    override fun load(context: LoadContext): DefinitionsState<SoundDefinition> = parse(context.paths(this))

    override fun total(state: DefinitionsState<SoundDefinition>) = state.ids.size

    override fun install(state: DefinitionsState<SoundDefinition>) = instance.install(state)

    fun parse(paths: List<String>): DefinitionsState<SoundDefinition> {
        val definitions = Array(10_000) { SoundDefinition.EMPTY }
        val ids = Object2IntOpenHashMap<String>(250, Hash.VERY_FAST_LOAD_FACTOR)
        for (path in paths) {
            Config.fileReader(path) {
                while (nextSection()) {
                    val stringId = section()
                    require(!ids.containsKey(stringId)) { "Duplicate sound id found '$stringId' at $path." }
                    var id = 0
                    var area = false
                    while (nextPair()) {
                        when (val key = key()) {
                            "id" -> id = int()
                            "area" -> area = boolean()
                            else -> throw IllegalArgumentException("Unknown sound key: $key")
                        }
                    }
                    ids[stringId] = id
                    if (area) {
                        definitions[id] = SoundDefinition(id = id, stringId = stringId, params = mapOf(Params.AREA to true))
                    } else {
                        definitions[id] = SoundDefinition(id = id, stringId = stringId)
                    }
                }
            }
        }
        return DefinitionsState(definitions, ids)
    }
}
