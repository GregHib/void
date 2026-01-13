package content.area.karamja.tzhaar_city

import content.entity.combat.killer
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.intEntry
import content.entity.player.dialogue.type.npc
import content.entity.player.inv.item.addOrDrop
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import org.rsmod.game.pathfinder.flag.CollisionFlag
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
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.epochMilliseconds
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class TzhaarFightCave(
    val areas: AreaDefinitions,
    val npcDefinitions: NPCDefinitions,
    val zones: DynamicZones,
    val npcs: NPCs,
    val accountManager: AccountManager,
) : Script {

    val centre = Tile(2400, 5088)
    val entrance = Tile(2413, 5117)
    val outside = Tile(2438, 5168)
    val safeTile = Tile(2413, 5117)
    val region = Region(9551)

    val waves = TzhaarFightCaveWaves()

    init {
        worldSpawn {
            waves.load(it.find(Settings["spawns.fight.cave.waves"]))
        }

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
            val instance = setupInstance()
            delay(1)
            val offset = instance.offset(region)
            set("fight_cave_instance", instance.id)
            tele(entrance.add(offset))
            walkTo(centre.add(offset))
            open("tzhaar_fight_cave")
            startWave(this, startWave, start = true)
        }

        objectOperate("Enter", "cave_exit_fight_cave") {
            choice("Really leave?") {
                option("Yes - really leave.") { leave() }
                option("No, I'll stay.")
            }
        }

        npcDeath("tz_kek,tz_kek_spawn_point") {
            val killer = killer as? Player ?: return@npcDeath
            for (i in 0 until 2) {
                val npc = npcs.add("tz_kek_spawn", tile.addX(i))
                npc.interactPlayer(killer, "Attack")
            }
        }

        npcDeath("tz_kih,tz_kih_spawn_point,tz_kek_spawn,tok_xil,tok_xil_spawn_point,yt_mej_kot*,ket_zek*,tztok_jad") {
            val killer = killer as? Player ?: return@npcDeath
            if (killer.dec("fight_cave_remaining") != 0) {
                return@npcDeath
            }
            val wave = killer["fight_cave_wave", -1]
            if (wave == 63 && id == "tztok_jad") {
                killer.strongQueue("fight_cave_end") {
                    killer.leave(true)
                }
            } else {
                startWave(killer, wave + 1, start = false)
            }
        }

        playerSpawn {
            val wave = get("fight_cave_wave", -1)
            if (wave == -1) {
                return@playerSpawn
            }
            val instance = setupInstance()
            set("fight_cave_instance", instance.id)
            val offset = instance.offset(region)
            open("tzhaar_fight_cave")
            val x = remove<Int>("fight_cave_x")
            val y = remove<Int>("fight_cave_y")
            if (x != null && y != null) {
                tele(offset.add(x, y))
            } else {
                tele(centre.add(offset))
            }
            sendVariable("fight_cave_wave")
            startWave(this, wave, start = true)
        }

        playerLogout(::logoutChoice)

        playerDespawn {
            val wave = get("fight_cave_wave", -1)
            if (wave == -1) {
                return@playerDespawn
            }
            val delta = tile.delta(region.tile)
            set("fight_cave_x", delta.x)
            set("fight_cave_y", delta.y)
            tele(safeTile)
            val instance = get("fight_cave_instance", -1)
            if (instance != -1) {
                Instances.free(Region(instance))
            }
        }

        // TODO handle player death
    }

    suspend fun Player.leave(defeatedJad: Boolean = false) {
        start("fight_cave_cooldown", TimeUnit.MINUTES.toSeconds(2).toInt(), epochSeconds())
        close("tzhaar_fight_cave")
        clear("fight_cave_wave")
        tele(outside)
        val instance = get("fight_cave_instance", -1)
        if (instance != -1) {
            Instances.free(Region(instance))
        }
        val wave = get("fight_cave_wave", 1)
        var tokkul = wave * (wave + 1)
        if (wave == 63 && defeatedJad) {
            tokkul += 4000
            addOrDrop("fire_cape", revealTicks = FloorItems.NEVER)
        }
        addOrDrop("tokkul", tokkul, revealTicks = FloorItems.NEVER)
        AuditLog.event(this, "end_fight_cave", wave, tokkul, defeatedJad)
        if (wave == 63 && defeatedJad) {
            npc<Angry>("tzhaar_mej_jal", "You even defeated TzTok-Jad, I am most impressed! Please accept this gift as a reward.")
        } else if (tokkul > 0) {
            npc<Neutral>("tzhaar_mej_jal", "Well done in the cave, here take Tokkul as reward.")
        } else {
            npc<Angry>("tzhaar_mej_jal", "Well I suppose you tried... better luck next time.")
        }
    }

    private fun logoutChoice(player: Player): Boolean {
        if (player["fight_caves_logout_warning", false]) {
            return true
        }
        player["fight_caves_logout_warning"] = true
        // https://youtu.be/Y5PqvTbC0C0?t=155
        player.message("<red>You will be logged out automatically at the end of this wave.")
        player.message("<red>If you log out sooner, you will have to repeat this wave.")
        return false
    }

    private fun setupInstance(): Region {
        val instance = Instances.small()
        zones.copy(region, instance)
        return instance
    }

    fun startWave(player: Player, wave: Int, start: Boolean) {
        player["fight_cave_wave"] = wave
        if (player["fight_caves_logout_warning", false]) {
            player.strongQueue("logout", onCancel = null) {
                accountManager.logout(player, false)
            }
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
        player["fight_cave_remaining"] = ids.sumOf { if (it == "tz_kek") 2 else 1 }
        val rotation = player["fight_cave_rotation", 1]
        val directions = waves.spawns(wave, rotation)
        val instance: Int = player["fight_cave_instance"]!!
        val offset = Region(instance).offset(region)
        val block = CollisionFlag.BLOCK_PLAYERS or CollisionFlag.BLOCK_NPCS
        for (i in ids.indices) {
            val id = ids[i]
            val def = npcDefinitions.get(id)
            val direction = directions[i]
            val area = when (direction) {
                Direction.NORTH_WEST -> areas["tzhaar_fight_cave_north_west"]
                Direction.NORTH_EAST -> areas["tzhaar_fight_cave_north_east"]
                Direction.SOUTH_EAST -> areas["tzhaar_fight_cave_south_east"]
                Direction.SOUTH -> areas["tzhaar_fight_cave_south"]
                Direction.SOUTH_WEST -> areas["tzhaar_fight_cave_south_west"]
                Direction.NONE -> areas["tzhaar_fight_cave_north"]
                else -> continue
            }
            val tile = area.offset(offset).random(CollisionStrategies.Normal, def.size, block) ?: continue
            val npc = npcs.add(id, tile)
            npc.interactPlayer(player, "Attack")
        }
    }

    fun hasFamiliar(player: Player) = false
}