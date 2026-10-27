package world.gregs.voidps.engine.entity.character.npc

import com.github.michaelbull.logging.InlineLogger
import kotlinx.serialization.builtins.ListSerializer
import world.gregs.config.Config
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.NPCLoader
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

private val logger = InlineLogger()

object NPCSpawnLoader : Loader<List<NPCSpawn>> {
    var spawns: List<NPCSpawn> = emptyList()
        private set
    override val name = "npc_spawns"
    override val label = "npc spawn"
    override val files = listOf("spawns.npcs")
    override val settings = listOf("world.members")
    override val dependsOn: List<Loader<*>> get() = listOf(NPCLoader)
    override val codec = SnapshotFormat.codec(ListSerializer(NPCSpawn.serializer()))

    override fun load(context: LoadContext): List<NPCSpawn> = parse(context.paths(this))

    override fun total(state: List<NPCSpawn>) = state.size

    override fun install(state: List<NPCSpawn>) {
        spawns = state
    }

    fun parse(paths: List<String>): List<NPCSpawn> {
        val spawns = ArrayList<NPCSpawn>(20_000)
        val membersWorld = World.members
        val hashes = mutableSetOf<Long>()
        for (path in paths) {
            Config.fileReader(path) {
                while (nextPair()) {
                    require(key() == "spawns")
                    while (nextElement()) {
                        var id = ""
                        var direction = Direction.SOUTH
                        var x = 0
                        var y = 0
                        var level = 0
                        var members = false
                        while (nextEntry()) {
                            when (val key = key()) {
                                "id" -> id = string()
                                "x" -> x = int()
                                "y" -> y = int()
                                "level" -> level = int()
                                "direction" -> direction = Direction.valueOf(string())
                                "members" -> members = boolean()
                                else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                            }
                        }
                        if (!membersWorld && members) {
                            continue
                        }
                        val tile = Tile(x, y, level)
                        val definition = NPCDefinitions.getOrNull(id)
                        if (definition == null) {
                            logger.warn { "Invalid npc spawn id '$id' in $path." }
                        } else {
                            val hash = (tile.id.toLong() shl 32) + definition.id.toLong()
                            if (hashes.contains(hash)) {
                                logger.warn { "Duplicate spawn id = \"$id\" x = ${tile.x}, y = ${tile.y}${if (tile.level != 0) ", level = ${tile.level}" else ""} in $path." }
                            }
                            hashes.add(hash)
                        }
                        spawns.add(NPCSpawn(id, tile, direction))
                    }
                }
            }
        }
        return spawns
    }
}
