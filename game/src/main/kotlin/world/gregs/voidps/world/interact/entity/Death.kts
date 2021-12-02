import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Death
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.clearAnimation
import world.gregs.voidps.engine.entity.character.update.visual.npc.turn
import world.gregs.voidps.engine.entity.character.update.visual.player.combatLevel
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.player.move
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.utility.getIntProperty
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.toUnderscoreCase
import world.gregs.voidps.world.interact.entity.combat.attackers
import world.gregs.voidps.world.interact.entity.sound.playJingle
import world.gregs.voidps.world.interact.entity.sound.playSound

val npcs: NPCs by inject()
val floorItems: FloorItems by inject()
val tables: DropTables by inject()

on<Registered> { character: Character ->
    character["damage_dealers"] = mutableMapOf<Character, Int>()
    character["attackers"] = mutableListOf<Character>()
}

on<Death> { npc: NPC ->
    npc.action(ActionType.Dying) {
        withContext(NonCancellable) {
            val damageDealers: MutableMap<Character, Int> = npc["damage_dealers"]
            val dealer = damageDealers.maxByOrNull { it.value }
            val killer = dealer?.key
            val tile = npc.tile
            npc["death_tile"] = tile
            val name = npc.def.name.toUnderscoreCase()
            npc.setAnimation("${name}_death")
            (killer as? Player)?.playSound("${name}_death", delay = 40)
            delay(4)
            var table = tables.get("${name}_drop_table")
            if (table == null) {
                table = tables.get("${npc.def["race", ""]}_drop_table")
            }
            val combatLevel = if (killer is Player) killer.combatLevel else if (killer is NPC) killer.def.combat else -1
            val list = table?.role(maximumRoll = if (combatLevel > 0) combatLevel * 10 else -1)
            list?.reversed()?.forEach {
                if (it.id != "nothing" && !it.id.contains("clue_scroll")) {
                    floorItems.add(it.id, it.amount.random(), tile, revealTicks = 60, disappearTicks = 120, owner = if (killer is Player) killer else null)
                }
            }
            npc.attackers.clear()
            npc.stopAllEffects()
            npcs.remove(npc)
            val respawn = npc.getOrNull<Tile>("respawn_tile")
            if (respawn != null) {
                delay(npc["respawn_delay", 60])
                damageDealers.clear()
                npc.levels.clear()
                npc.move(respawn)
                npc.turn(npc["respawn_direction", Direction.NORTH], update = false)
                npcs.add(npc)
            } else {
                npc.events.emit(Unregistered)
                await(Suspension.Infinite)
            }
        }
    }
}
val x = getIntProperty("homeX", 0)
val y = getIntProperty("homeY", 0)
val plane = getIntProperty("homePlane", 0)
val respawnTile = Tile(x, y, plane)

on<Death> { player: Player ->
    player.action(ActionType.Dying) {
        withContext(NonCancellable) {
            player.instructions.resetReplayCache()
            val tile = player.tile
            player.setAnimation("player_death")
            delay(5)
            player.clearAnimation()
            player.attackers.clear()
            val damageDealers: MutableMap<Character, Int> = player["damage_dealers"]
            damageDealers.clear()
            player.playJingle("death")
            player.stopAllEffects()
            dropAll(player, player.equipment, tile)
            dropAll(player, player.inventory, tile)
            player.levels.clear()
            player.move(respawnTile)
            player.face(Direction.SOUTH, update = false)
        }
    }
}

fun dropAll(player: Player, container: Container, tile: Tile) {
    for (slot in 0 until container.capacity) {
        val item = container.getItem(slot)
        if (item.isNotEmpty()) {
            floorItems.add(item.id, item.amount, tile, revealTicks = 180, disappearTicks = 240, owner = player)
        }
    }
    container.clearAll()
}