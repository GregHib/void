package content.skill.dungeoneering

import com.github.michaelbull.logging.InlineLogger
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle
import world.gregs.voidps.type.random
import kotlin.random.nextInt

object DungeonNPCs {

    private val logger = InlineLogger()

    private fun spawnBoss(dungeon: DungeonMap, room: DungeonRoom) {
        val members = dungeon.members.sortedBy { it.combatLevel }.take(dungeon.playerCount)
        val average = members.sumOf { it.combatLevel } / members.size

        val row = Rows.getOrNull("boss_spawns.${room.name}") ?: return

        val ids = row.npcList("ids")
        val id = ids.filter { NPCDefinitions.get(it).combat <= average }.maxBy { NPCDefinitions.get(it).combat }

        val tile = row.tile("tile")
        val direction = Direction.SOUTH
        val actual = Tile(tile.x.rem(16), tile.y.rem(16))
        val clientRotation = (4 - room.rotation) % 4
        val boss = NPCs.add(id, dungeon.tile(room, actual.x, actual.y), direction.rotate(clientRotation * 2))
        boss["in_multi_combat"] = true
    }

    fun spawn(dungeon: DungeonMap, room: DungeonRoom, floor: Int, complexity: Int) {
        when (room.type) {
            DungeonRoomType.Base -> NPCs.add("smuggler_dungeoneering", dungeon.startTile().addX(-1))
            DungeonRoomType.Boss -> spawnBoss(dungeon, room)
            DungeonRoomType.Puzzle -> TODO()
            DungeonRoomType.Normal -> spawnMonsters(room, dungeon, floor, complexity)
        }
    }

    private fun spawnMonsters(room: DungeonRoom, dungeon: DungeonMap, floor: Int, complexity: Int) {
        val npcSpawns = room.findObjects(dungeon, setOf("rand_npc_spawn_1x1", "rand_npc_spawn_2x2", "rand_npc_spawn_3x3", "rand_npc_spawn_4x4", "rand_npc_spawn_5x5"))
        if (npcSpawns.isEmpty()) {
            return
        }
        for (spawn in npcSpawns) {
            spawn.remove()
        }
        // https://runescape.wiki/w/Dungeoneering/Monsters#Overview
        if (random.nextInt(5) == 0) {
            return
        }
        val spawnCount = random.nextInt(1..dungeon.playerCount + 3)
        val members = dungeon.members.sortedBy { it.combatLevel }.take(dungeon.playerCount)
        val average = members.sumOf { it.combatLevel } / members.size

        // TODO replace with accurate algorithm
        val combatLevel = dungeon.members.maxOf { it.combatLevel }
        var total = (average * (Interpolation.lerp(floor, 1..60, 50..100) / 100.0)).toInt()
        total += complexityBonus(combatLevel, complexity)

        val npcs = mutableListOf<String>()
        npcs.addAll(Tables.npcList("dungeon_monsters.base.npcs"))
        val list = Tables.npcListOrNull("dungeon_monsters.${dungeon.theme}.npcs")
        if (list != null) {
            npcs.addAll(list)
        }
        for (i in 0 until spawnCount) {
            total = total.coerceAtLeast(1)
            val id = npcs.filter { NPCDefinitions.get(it).combat <= total }.randomOrNull(random)
            if (id == null) {
                logger.warn { "Failed to find dungeoneering monster with combat=$total" }
                break
            }
            val def = NPCDefinitions.get(id)
            val spawn = npcSpawns.random(random)
            val rect = when (spawn.id) {
                "rand_npc_spawn_1x1" -> Rectangle(spawn.tile, 1, 1)
                "rand_npc_spawn_2x2" -> Rectangle(spawn.tile, 2, 2)
                "rand_npc_spawn_3x3" -> Rectangle(spawn.tile, 3, 3)
                "rand_npc_spawn_4x4" -> Rectangle(spawn.tile, 4, 4)
                "rand_npc_spawn_5x5" -> Rectangle(spawn.tile, 5, 5)
                else -> continue
            }
            val tile = rect.random(CollisionStrategyProvider.get(def), def.size, CollisionFlag.BLOCK_NPCS)
            if (tile == null) {
                logger.warn { "Failed to find dungeoneering spawn combat=$total, zone=${room.zone} spawns=$npcSpawns" }
                continue
            }
            val npc = NPCs.add(id, tile)
            npc["in_multi_combat"] = true
            total -= def.combat
        }
    }

    /**
     * Complexity bonus based off a sample of rs3 low (c1) and high (c6) dungeons
     * Max combat level of monsters increases by approx: +40 @ 125 combat, +8 @ 32 combat, +0 @ 3 combat
     */
    private fun complexityBonus(combatLevel: Int, complexity: Int): Int = Interpolation.lerp(
        value = combatLevel,
        inRange = 3..138,
        result = 0..when (complexity) {
            2 -> 5
            3 -> 15
            4 -> 25
            5 -> 35
            6 -> 45
            else -> 0
        },
    )
}
