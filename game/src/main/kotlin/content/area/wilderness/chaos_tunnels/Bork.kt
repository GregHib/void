package content.area.wilderness.chaos_tunnels

import content.entity.combat.attacker
import content.entity.combat.hit.directHit
import content.entity.combat.killer
import content.entity.effect.clearTransform
import content.entity.gfx.areaGfx
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.player
import content.quest.closeTabs
import content.quest.instanceOffset
import content.quest.openTabs
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.Minimap
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.clearMinimap
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.minimap
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.shakeCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.RegionLevel
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class Bork : Script {
    init {
        npcLevelChanged(Skill.Constitution, "bork,bork_surok") { _, _, to ->
            if (to > (levels.getMax(Skill.Constitution) * 0.6).toInt()) {
                return@npcLevelChanged
            }
            if (contains("legion")) {
                return@npcLevelChanged
            }
            val attacker = attacker as? Player ?: return@npcLevelChanged
            val elite = findElite(tile.regionLevel)
            elite.mode = PauseMode
            mode = PauseMode
            attacker.mode = PauseMode
            val bork = this
            attacker.queue("bork_summon") {
                delay(2)
                bork.say("Come to my aid, brothers!")
                bork.anim("bork_summon")
                bork.gfx("bork_minion_aid")
                attacker.message("Bork strikes the ground with his axe.")
                bork["legion"] = true
                delay(3)
                attacker.open("bork_minion_spawn")
                delay(4)
                attacker.close("bork_minion_spawn")
                delay(2)
                val offset = attacker.instanceOffset()
                repeat(random.nextInt(1, 4)) {
                    val tile = offset.tile(Areas["bork_legion_spawn"].random())
                    areaGfx("bork_minion_spawn", tile)
                    val legion = NPCs.add("ork_legion", tile)
                    set("legion_$it", legion)
                    legion.anim("ork_legion_spawn")
                    legion.gfx("summon_familiar_size_1")
                    legion.say(if (random.nextBoolean()) "Die, human!" else "For Bork!")
                    legion.interactPlayer(attacker, "Attack")
                    legion.softTimers.start("legion_war_cries")
                }
                bork.say("Destroy the intruder, my legions!")
                bork.interactPlayer(attacker, "Attack")
                elite.interactPlayer(attacker, "Attack")
            }
        }

        npcDeath("bork,bork_surok") {
            for (i in 0 until 4) {
                val npc = get<NPC>("legion_$i") ?: break
                npc.mode = PauseMode
                NPCs.remove(npc)
            }
            val killer = killer as? Player ?: return@npcDeath
            killer["delay"] = 2
            killer.queue.clear()
            killer.visuals.hits.clear()
            killer.queue("bork_defeat") {
                killer.escape()
            }
        }

        npcTimerStart("legion_war_cries") {
            TimeUnit.SECONDS.toTicks(15)
        }

        npcTimerTick("legion_war_cries") {
            say(
                when (random.nextInt(5)) {
                    0 -> "We are the collective!"
                    1 -> "Form a triangle!!"
                    2 -> "Steady lads!"
                    3 -> "Hup! 2... 3... 4!!"
                    4 -> "To the attack!"
                    else -> "Resistance is futile!"
                },
            )
            Timer.CONTINUE
        }

        timerStart("bork_cavern_collapse") {
            TimeUnit.SECONDS.toTicks(8)
        }

        timerTick("bork_cavern_collapse") {
            if (inc("falling_rocks") == 5) {
                message("You quickly make your escape as the cavern collapses behind you!") // TODO proper message
                tele(3142, 5545)
                return@timerTick Timer.CANCEL
            }
            directHit(20)
            message("You are hit by falling rocks! Look out!")
            TimeUnit.SECONDS.toTicks(20)
        }
    }

    private suspend fun Player.escape() {
        clearCamera()
        closeTabs()
        minimap(Minimap.HideMap)
        val day = TimeUnit.DAYS.toSeconds(1)
        val cooldown = (day - epochSeconds().rem(day)).toInt()
        start("bork_cooldown", cooldown, epochSeconds())
        inc("bork_kill_count")
        open("bork_defeated")
        val offset = instanceOffset()
        val elite = findElite(tile.regionLevel)
        elite.mode = PauseMode
        elite.clearTransform()
        delay(1)
        elite.tele(offset.tile(3100, 5538))
        elite.face(Direction.SOUTH_WEST)
        delay(14)
        // TODO sfx
        delay(2)
        close("bork_defeated")
        moveCamera(offset.tile(3096, 5534), height = 400)
        turnCamera(offset.tile(3100, 5538), height = 0)
        if (elite.id == "surok_magis_dagon_hai") {
            elite.say("Fear the power of Zamorak!")
        } else {
            elite.say("Zamorak! Avenge me!")
        }
        delay(2)
        elite.anim("teleport_modern")
        elite.gfx("teleport_modern")
        sound("teleport_modern")
        delay(6)
        NPCs.remove(elite)
        clearCamera()
        clearMinimap()
        if (elite.id == "surok_magis_dagon_hai") {
            player<Angry>("It looks like Surok slipped away when I was fighting. I wonder what he is up to now...?")
        } else {
            player<Angry>("That monk - he called to Zamorak for revenge!")
        }
        message("Something is shaking the whole cavern! You should get out of here quick!")
        shakeCamera(4, 0, 0, 5, 100)
        shakeCamera(4, 2, 0, 5, 100)
        player<Shock>("What th-? This power! It must be Zamorak! I can't fight something this strong! I better loot what I can and get out of here!")
        openTabs()
        softTimers.start("bork_cavern_collapse")
    }

    private fun findElite(regionLevel: RegionLevel): NPC = NPCs.findOrNull(regionLevel, "dagonhai_elite_attack") ?: NPCs.find(regionLevel, "surok_magis_dagon_hai")
}
