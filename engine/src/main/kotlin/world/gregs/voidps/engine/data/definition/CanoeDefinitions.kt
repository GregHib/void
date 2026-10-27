package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.CanoeDefinition
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.type.Tile

class CanoeDefinitions {

    lateinit var definitions: Map<String, CanoeDefinition>

    fun get(id: String): CanoeDefinition = getOrNull(id) ?: CanoeDefinition.EMPTY

    fun getOrNull(id: String): CanoeDefinition? = definitions[id]

    fun empty() = CanoeDefinition.EMPTY

    fun load(path: String): CanoeDefinitions {
        val canoes = Object2ObjectOpenHashMap<String, CanoeDefinition>()
        Config.fileReader(path, 165) {
            while (nextSection()) {
                val stringId = section()
                var destination = Tile.EMPTY
                var sink = Tile.EMPTY
                var message = ""
                while (nextPair()) {
                    val key = key()
                    when (key) {
                        "destination" -> {
                            nextElement()
                            val x = int()
                            nextElement()
                            val y = int()
                            var level = 0
                            if (nextElement()) {
                                level = int()
                            }
                            destination = Tile(x, y, level)
                        }
                        "sink" -> {
                            nextElement()
                            val x = int()
                            nextElement()
                            val y = int()
                            var level = 0
                            if (nextElement()) {
                                level = int()
                            }
                            sink = Tile(x, y, level)
                        }
                        "message" -> message = string()
                    }
                }
                canoes[stringId] = CanoeDefinition(stringId = stringId, destination = destination, sink = sink, message = message)
            }
        }
        this.definitions = canoes
        return this
    }

    internal fun copyFrom(other: CanoeDefinitions) {
        definitions = other.definitions
    }
}

object CanoeLoader : Loader<CanoeDefinitions> {
    val instance = CanoeDefinitions()
    override val name = "canoes"
    override val file = listOf("map.canoes")

    override fun load(context: LoadContext): CanoeDefinitions = CanoeDefinitions().load(context.paths(this).single())

    override fun install(state: CanoeDefinitions) = instance.copyFrom(state)
}
