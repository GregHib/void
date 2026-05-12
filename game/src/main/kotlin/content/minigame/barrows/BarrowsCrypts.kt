package content.minigame.barrows

import content.entity.combat.killer
import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.*
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class BarrowsCrypts : Script {
    init {
        objectOperate("Search", "dharok_sarcophagus,verac_sarcophagus,ahrim_sarcophagus,guthan_sarcophagus,karil_sarcophagus,torag_sarcophagus") { (target) ->
            if (!contains("barrows_selected_brother") || get("barrows_looted", false)) {
                clear("barrows_looted")
                val brother = Tables.get("barrows_brothers").rows().random(random)
                set("barrows_selected_brother", brother.rowId)
                shufflePuzzle()
            }
            val brother = target.id.substringBefore("_sarcophagus")
            if (get("barrows_selected_brother", "") == brother) {
                statement("You've found a hidden tunnel, do you want to enter?")
                choice {
                    option("Yeah I'm fearless!") {
                        val corner = Direction.ordinal.first { get("barrows_rope_${it.name.lowercase()}", false) }
                        tele(Tables.tile("barrows_doors.${corner.name.lowercase()}.tile"))
                    }
                    option("No way, that looks scary!")
                }
            } else if (!contains("${brother}_spawn") && !get("${brother}_killed", false)) {
                val tile = Tables.tile("barrows_brothers.$brother.spawn").toCuboid(2).random(CollisionStrategies.Normal)
                spawnBrother(this, brother, tile)
            } else {
                message("You don't find anything.", type = ChatType.Filter)
            }
        }

        npcSpawn("ahrim_the_blighted,dharok_the_wretched,guthan_the_infested,karil_the_tainted,torag_the_corrupted,verac_the_defiled") {
            val brother = id.substringBefore("_the_")
            set("${brother}_set_effect", true)
            when (brother) {
                "dharok" -> set("weapon", Item("dharok_greataxe"))
                "karil" -> set("weapon", Item("karils_crossbow"))
                "torag" -> set("weapon", Item("torags_hammers"))
            }
        }

        npcDeath("ahrim_the_blighted,dharok_the_wretched,guthan_the_infested,karil_the_tainted,torag_the_corrupted,verac_the_defiled") {
            val player = killer as? Player ?: return@npcDeath
            val brother = id.substringBefore("_the_")
            player["${brother}_killed"] = true
            player.clear("${brother}_spawn")
            player.inc("barrows_kills")
            player.inc("barrows_killed_monsters")
            player.inc("barrows_kill_levels", def.combat)
        }

        npcDeath("bloodworm,crypt_rat_barrows,giant_crypt_rat_chaos_tunnels,crypt_spider,giant_crypt_spider,skeleton_barrows,skeleton_barrows_2,giant_crypt_rat_chaos_tunnels_2,giant_crypt_rat_chaos_tunnels_3,skeleton_barrows_3,skeleton_barrows_4") {
            val player = killer as? Player ?: return@npcDeath
            if (tile !in Areas["barrows_tunnels"]) {
                return@npcDeath
            }
            player.inc("barrows_killed_monsters")
            player.inc("barrows_kill_levels", def.combat)
        }

        objectOperate("Climb-up", "dharok_stairs,verac_stairs,ahrim_stairs,guthan_stairs,karil_stairs,torag_stairs") {
            val brother = it.target.id.substringBefore("_stairs")
            removeBrother(brother)
            tele(Areas["${brother}_hill"])
        }

        entered("barrows_crypts") {
            if (!interfaces.contains("barrows_overlay")) {
                open("barrows_overlay")
            }
            softTimers.start("barrows_prayer_drain")
            send()
            minimap(Minimap.HideMap)
        }

        exited("barrows_crypts") {
            softTimers.stop("barrows_prayer_drain")
            softTimers.stop("barrows_cave_shake")
            if (tile !in Areas["barrows"]) {
                close("barrows_overlay")
            }
            removeAll()
            clearMinimap()
            set("barrows_in_tunnel", false)
        }

        timerStart("barrows_prayer_drain") { TimeUnit.SECONDS.toTicks(18) }

        timerTick("barrows_prayer_drain") {
            // Drain 8 prayer points every 18s increasing by 1 each time
            val row = Tables.get("barrows_brothers").rows().random(random)
            val brother = row.rowId
            set("barrows_brother_head", if (tile.level == 0) "${brother}_tunnels" else brother)
            inc("barrows_brother_drain")
            val drain = (8 + get("barrows_brother_drain", 0)).coerceAtMost(13)
            levels.drain(Skill.Prayer, drain)
            Timer.CONTINUE
        }

        exited("barrows") {
            if (tile in Areas["barrows_crypts"]) {
                return@exited
            }
            close("barrows_overlay")
        }

        interfaceOpened("barrows_overlay") {
            sendVariable("ahrim_killed")
            sendVariable("dharok_killed")
            sendVariable("guthan_killed")
            sendVariable("karil_killed")
            sendVariable("torag_killed")
            sendVariable("verac_killed")
        }

        objectOperate("Climb-up", "barrows_rope") {
            val brother = get("barrows_selected_brother", "dharok")
            val tile = Tables.tile("barrows_brothers.$brother.spawn")
            anim("climb_up")
            delay(2)
            tele(tile)
        }

        objectOperate("Open", "barrows_door_*_closed") { (target) ->
            if (target.id.startsWith("barrows_puzzle_door") && tile !in Areas["barrows_inner_room"]) {
                if (!puzzle()) {
                    shufflePuzzle(true)
                    message("You got the puzzle wrong! You can hear the catacombs moving around you.")
                    return@objectOperate
                } else {
                    sound("barrows_door_unlock")
                    message("You hear the doors' locking mechanism grind open.")
                }
            }
            sound("barrows_door_close")
            val start = tile
            enterDoor(target, target.def(this))
            toggle("barrows_in_tunnel")
            val direction = tile.delta(start).toDirection()
            val spawn = tile.add(if (target.rotation == 0 || target.rotation == 2) direction.horizontal() else direction.vertical())
            // Spawn npc
            val random = random.nextInt(128)
            if (random < 12) { // 12/128
                for (row in Tables.get("barrows_brothers").rows()) {
                    val brother = row.rowId
                    if (get("${brother}_killed", false) || contains("${brother}_spawn")) {
                        continue
                    }
                    spawnBrother(this, brother, spawn)
                    break
                }
                return@objectOperate
            }
            val id = when {
                random < 44 -> "giant_crypt_rat_chaos_tunnels" // 32/128
                random < 76 -> "bloodworm" // 32/128
                else -> "skeleton_barrows" // 52/128
            }
            val npc = NPCs.add(id, spawn)
            npc.interactPlayer(this, "Attack")
            npc.queue("despawn", TimeUnit.MINUTES.toTicks(2)) {
                NPCs.remove(npc)
            }
        }
    }

    private fun Player.removeAll() {
        for (row in Tables.get("barrows_brothers").rows()) {
            removeBrother(row.rowId)
        }
    }

    fun Player.shufflePuzzle(incorrect: Boolean = false) {
        val puzzles = Tables.stringList("barrows_doors.puzzles.vars")
        val puzzleDoors = puzzles.toMutableList()
        if (incorrect) {
            // Incorrect answers can't pick the same door again
            puzzleDoors.remove(puzzles.firstOrNull { get(it, false) })
        }
        reset()
        // Pick an exit rope corner
        val corner = Direction.ordinal.random(random).name.lowercase()
        set("barrows_rope_$corner", true)

        // Pick a single valid door in the exit room
        val doors = Tables.stringList("barrows_doors.$corner.vars")
        val valid = doors.random(random)
        for (door in doors) {
            set(door, door != valid)
        }

        // Pick a puzzle door
        val puzzle = puzzleDoors.random(random)
        set(puzzle, true)
        for (door in puzzles) {
            set(door, door != puzzle)
        }
    }

    private fun Player.reset() {
        for (direction in Direction.ordinal) {
            set("barrows_rope_${direction.name.lowercase()}", false)
        }
        for (row in Tables.get("barrows_doors").rows()) {
            for (variable in row.stringList("vars")) {
                set(variable, false)
            }
        }
    }

    private fun Player.send() {
        for (direction in Direction.ordinal) {
            sendVariable("barrows_rope_${direction.name.lowercase()}")
        }
        for (row in Tables.get("barrows_doors").rows()) {
            for (variable in row.stringList("vars")) {
                sendVariable(variable)
            }
        }
        sendVariable("barrows_in_tunnel")
        sendVariable("barrows_killed_monsters")
    }

    private fun Player.removeBrother(brother: String) {
        if (!contains("${brother}_spawn")) {
            return
        }
        val index = remove<Int>("${brother}_spawn") ?: return
        NPCs.remove(NPCs.indexed(index))
    }

    companion object {
        internal fun spawnBrother(player: Player, brother: String, tile: Tile?) {
            val id = Tables.npc("barrows_brothers.$brother.npc")
            val npc = NPCs.add(id, tile ?: player.tile)
            npc.say(if (npc.tile.level == 3) "You dare disturb my rest!" else "You dare steal from us!")
            npc.interactPlayer(player, "Attack")
            player["${brother}_spawn"] = npc.index
            player.hint(npc)
        }
    }
}
