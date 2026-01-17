package content.area.karamja.tzhaar_city

import com.github.michaelbull.logging.InlineLogger
import content.entity.combat.hit.damage
import content.entity.combat.killer
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.intEntry
import content.entity.player.dialogue.type.npc
import content.entity.player.inv.item.addOrDrop
import content.quest.clearInstance
import content.quest.instanceOffset
import content.quest.smallInstance
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AreaTypes
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.epochMilliseconds
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.*
import java.util.concurrent.TimeUnit

class TzhaarFightCave(
    val npcs: NPCs,
    val accountManager: AccountManager,
) : Script {

    val centre = Tile(2400, 5088)
    val entrance = Tile(2413, 5117)
    val outside = Tile(2436, 5170)
    val region = Region(9551)

    val waves = TzhaarFightCaveWaves()

    val logger = InlineLogger()

    init {
        worldSpawn {
            waves.load(it.find(Settings["spawns.fight.cave.waves"]))
        }

        /*
            Entrance/Exit
         */

        objectOperate("Enter", "cave_entrance_fight_cave") {
            if (!isAdmin() && hasClock("fight_cave_cooldown", epochSeconds())) {
                val seconds = remaining("fight_cave_cooldown", epochSeconds())
                val minutes = seconds / 60
                val remaining = if (minutes == 0) "remaining ${"second".plural(seconds)}" else "$minutes ${"minute".plural(minutes)}"
                npc<Angry>("tzhaar_mej_jal", "Hey, JalYt, you were in cave only a moment ago. You wait $remaining before going in again.")
                return@objectOperate
            }
            if (hasFamiliar(this)) {
                npc<Angry>("tzhaar_mej_jal", "No Kimit-Zil in the cave! This is a fight for YOU, not your friends!")
                return@objectOperate
            }
            var startWave = Settings["fightCave.startWave", 1]
            if (isAdmin()) {
                startWave = intEntry("What wave would you like to start on?").coerceIn(1..63)
            }
            smallInstance(region, 3)
            delay(1)
            val offset = instanceOffset()
            tele(entrance.add(offset))
            walkTo(centre.add(offset))
            startWave(this, startWave, start = true)
        }

        objectOperate("Enter", "cave_exit_fight_cave") {
            choice("Really leave?") {
                option("Yes - really leave.") { leave(wave) }
                option("No, I'll stay.")
            }
        }

        exited("tzhaar_fight_cave_multi_area") {
            close("tzhaar_fight_cave")
            clearInstance()
            if (get("logged_out", false)) {
                // Save the player's relative position in the original region
                val offset = tile.delta(tile.region.tile)
                tele(region.tile.add(offset))
            } else {
                clear("fight_cave_wave")
            }
        }

        /*
            Waves
         */

        npcDespawn("tz_kih,tz_kih_spawn_point,tz_kek_spawn,tok_xil,tok_xil_spawn_point,yt_mej_kot*,ket_zek*,tztok_jad") {
            val killer = killer as? Player ?: return@npcDespawn
            if (killer.dec("fight_cave_remaining") > 0) {
                return@npcDespawn
            }
            val wave = killer.wave
            if (wave == 63 && id == "tztok_jad") {
                killer.leave(wave, true)
            } else if (wave < 63) {
                startWave(killer, wave + 1, start = false)
            }
        }

        npcCombatDamage("tz_kek,tz_kek_spawn_point") { (source) ->
            source.damage(10)
        }

        npcDeath("tz_kek,tz_kek_spawn_point") {
            val killer = killer as? Player ?: return@npcDeath
            spawn("tz_kek_spawn", tile, killer)
            spawn("tz_kek_spawn", tile, killer)
        }

        npcLevelChanged(Skill.Constitution, "tztok_jad") { skill, from, to ->
            val max = levels.getMax(skill)
            if (from != max && to == max) {
                set("healed", true)
                return@npcLevelChanged
            }
            // Healers can only respawn if healed to full.
            if (!get("healed", false)) {
                return@npcLevelChanged
            }
            val half = max / 2
            if (half !in to..<from) {
                return@npcLevelChanged
            }
            val count = npcs[tile.regionLevel].count { it.id == "yt_hur_kot" }
            val block = CollisionFlag.BLOCK_PLAYERS or CollisionFlag.BLOCK_NPCS
            val directions = mutableSetOf(Direction.NORTH_WEST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.NONE)
            val offset = tile.region.tile.delta(region.tile)
            val def = NPCDefinitions.get("yt_hur_kot")
            for (i in 0 until 4 - count) {
                val dir = directions.random(random)
                var tile = randomTile(dir, offset, def, block) ?: continue
                val npc = npcs.add("yt_hur_kot", tile)
                npc["in_multi_combat"] = true
                npc.mode = Follow(npc, this)
                npc.softTimers.start("yt_hur_kot_heal")
            }
        }

        /*
            Restart
         */

        playerSpawn {
            if (wave == -1) {
                return@playerSpawn
            }
            val instance = smallInstance(region, 3)
            open("tzhaar_fight_cave")
            val delta = tile.delta(region.tile)
            val pos = instance.tile.add(delta)
            if (instance.tile.toCuboid(64, 64).contains(pos)) {
                tele(pos)
            } else {
                tele(centre.add(instanceOffset()))
            }
            strongQueue("fight_cave_start", TimeUnit.SECONDS.toTicks(2)) {
                startWave(player, wave, start = true)
                player.sendVariable("fight_cave_wave")
            }
        }

        playerLogout(::logoutChoice)

        playerDeath {
            if (wave == -1) {
                return@playerDeath
            }
            it.dropItems = false
            it.teleport = outside
            softQueue("fire_cave_death", 3) {
                leave(wave)
            }
        }
    }

    fun Player.leave(wave: Int, defeatedJad: Boolean = false) {
        clear("fight_cave_wave")
        start("fight_cave_cooldown", TimeUnit.MINUTES.toSeconds(2).toInt(), epochSeconds())
        tele(outside)
        var tokkul = wave * (wave + 1)
        if (wave == 63 && defeatedJad) {
            tokkul += 4000
            addOrDrop("fire_cape", revealTicks = FloorItems.NEVER)
        }
        addOrDrop("tokkul", tokkul, revealTicks = FloorItems.NEVER)
        AuditLog.event(this, "end_fight_cave", wave, tokkul, defeatedJad)
        queue("fight_cave_dialogue", 1) {
            if (wave == 63 && defeatedJad) {
                npc<Happy>("tzhaar_mej_jal", "You even defeated TzTok-Jad, I am most impressed! Please accept this gift as a reward.")
            } else if (tokkul > 0) {
                npc<Neutral>("tzhaar_mej_jal", "Well done in the cave, here take Tokkul as reward.")
            } else {
                npc<Angry>("tzhaar_mej_jal", "Well I suppose you tried... better luck next time.")
            }
        }
    }

    private fun logoutChoice(player: Player): Boolean {
        if (!player.contains("fight_cave_wave") || player["fight_caves_logout_warning", false]) {
            return true
        }
        player["fight_caves_logout_warning"] = true
        // https://youtu.be/Y5PqvTbC0C0?t=155
        player.message("<red>You will be logged out automatically at the end of this wave.")
        player.message("<red>If you log out sooner, you will have to repeat this wave.")
        return false
    }

    fun startWave(player: Player, wave: Int, start: Boolean) {
        player.close("tzhaar_fight_cave")
        player.open("tzhaar_fight_cave")
        if (wave != 1) {
            player.jingle("fight_cave_wave_complete")
        }
        player["fight_cave_wave"] = wave
        if (player["fight_caves_logout_warning", false]) {
            Script.launch { accountManager.logout(player, false) }
            return
        }
        if (start && wave != 63) {
            player.queue("fight_cave_warning") {
                player.npc<Angry>("tzhaar_mej_jal", "You're on your own now JalYt, prepare to fight for your life!")
            }
        }
        if (wave == 1) {
            val rotation = (1..15).random(random)
            val start = epochMilliseconds()
            player["fight_cave_rotation"] = rotation
            player["fight_cave_start_time"] = start
            AuditLog.event(player, "start_fight_cave", start, wave, rotation)
        } else if (wave == 63) {
            player.queue("fight_cave_warning") {
                player.npc<Angry>("tzhaar_mej_jal", "Look out, here comes TzTok-Jad!")
            }
        }
        val ids = waves.npcs(wave)
        player["fight_cave_remaining"] = ids.sumOf { if (it == "tz_kek" || it == "tz_kek_spawn_point") 2 else 1 }
        val rotation = player["fight_cave_rotation", 1]
        val directions = waves.spawns(wave, rotation)
        val offset = player.instanceOffset()
        val block = CollisionFlag.BLOCK_PLAYERS or CollisionFlag.BLOCK_NPCS
        for (i in ids.indices) {
            val id = ids[i]
            val def = NPCDefinitions.get(id)
            val direction = directions[i]
            val tile = randomTile(direction, offset, def, block) ?: continue
            spawn(id, tile, player)
        }
    }

    fun randomTile(direction: Direction, offset: Delta, def: NPCDefinition, block: Int): Tile? {
        val area = when (direction) {
            Direction.NORTH_WEST -> AreaTypes["tzhaar_fight_cave_north_west"]
            Direction.SOUTH_EAST -> AreaTypes["tzhaar_fight_cave_south_east"]
            Direction.SOUTH -> AreaTypes["tzhaar_fight_cave_south"]
            Direction.SOUTH_WEST -> AreaTypes["tzhaar_fight_cave_south_west"]
            Direction.NONE -> AreaTypes["tzhaar_fight_cave_none"]
            else -> return null
        }
        var tile = area.offset(offset).random(CollisionStrategies.Normal, def.size, block)
        if (tile == null) {
            logger.warn { "Failed to find random tile for fight cave spawn $direction in $area with $offset" }
            tile = area.offset(offset).random()
        }
        return tile
    }

    private val Player.wave: Int
        get() = get("fight_cave_wave", -1)

    private fun spawn(id: String, tile: Tile, target: Player) {
        val npc = npcs.add(id, tile)
        npc["in_multi_combat"] = true
        npc.interactPlayer(target, "Attack")
        if (id == "tztok_jad") {
            npc["healed"] = true
        }
    }

    fun hasFamiliar(player: Player) = false
}
