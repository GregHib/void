package content.area.wilderness.chaos_tunnels

import content.entity.effect.transform
import content.entity.obj.ObjectTeleports
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.clearInstance
import content.quest.instanceOffset
import content.quest.setInstanceLogout
import content.quest.smallInstance
import content.quest.startCutscene
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.clearMinimap
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals
import java.util.concurrent.TimeUnit

class HuntForSurok(val teleports: ObjectTeleports) : Script {
    init {
        objectOperate("Enter", "bork_exit_portal") { (target) ->
            tele(3142, 5545)
        }

        exited("bork_cavern") {
            clearInstance()
            clearCamera()
            clearMinimap()
        }

        objTeleportTakeOff("Enter", "chaos_tunnels_portal") { target, _ ->
            if (target.tile.equals(3142, 5545)) {
                if (hasClock("bork_cooldown", epochSeconds())) {
                    message("The portal appears to have stopped working for now.")
                    val remaining = remaining("bork_cooldown", epochSeconds())
                    val hours = TimeUnit.SECONDS.toHours(remaining.toLong())
                    message("Perhaps you should return ${if (hours == 0L) "later" else "in $hours ${"hour".plural(hours)}"}?")
                } else {
                    val instance = smallInstance(Region(12374))
                    setInstanceLogout(Tile(3142, 5545))
                    gfx("curse_impact")
                    queue("enter_bork_lair") {
                        val cutscene = startCutscene("bork", instance, instanceOffset())
                        cutscene.onEnd(destroyInstance = false) {
                            tele(3142, 5545)
                            clearCamera()
                        }
                        if (get("bork_kill_count", 0) == 0) {
                            cutscene()
                        } else {
                            repeat()
                        }
                        cutscene.end(destroyInstance = false, invokeEnd = false)
                    }
                }
                Teleport.CANCEL
            } else {
                gfx("curse_impact")
                Teleport.CONTINUE
            }
        }
    }

    private suspend fun Player.repeat() {
        val offset = instanceOffset()
        open("fade_out")
        delay(3)
        tele(offset.tile(3107, 5537))
        val elite = NPCs.add("dagonhai_elite_attack", offset.tile(3107, 5547))
        val bork = NPCs.add("bork", offset.tile(3095, 5533))
        bork.mode = PauseMode
        val player = this@repeat
        delay(1)
        close("fade_out")
        talkWith(elite)
        npc<Angry>("Our Lord Zamorak has power over life and death, $name! He has seen fit to resurrect Bork to continue his great work...and now you will fall before him!")
        player<Shock>("Uh-oh! Here we go again.")
        bork.interactPlayer(player, "Attack")
        elite.interactPlayer(player, "Attack")
    }

    private suspend fun Player.cutscene() {
        val offset = instanceOffset()
        open("fade_out")
        delay(3)
        val player = this@cutscene
        tele(offset.tile(3106, 5538))
        clearCamera()
        moveCamera(offset.tile(3099, 5534), height = 500)
        turnCamera(offset.tile(3101, 5538), height = 500)
        val surok = NPCs.add("surok_magis_dagon_hai", offset.tile(3101, 5541), Direction.EAST)
        val bork = NPCs.add("bork_surok", offset.tile(3095, 5533))
        bork.mode = PauseMode
        bork.face(surok)
        delay(1)
        close("fade_out")
        walkToDelay(offset.tile(3105, 5539), forceWalk = true)
        walkToDelay(offset.tile(3102, 5539), forceWalk = true)
        face(surok)
        surok.face(player)
        delay(1)
        talkWith(surok)
        player<Angry>("It's a dead end, Surok. There's nowhere left to run.")
        npc<Angry>("You're wrong. $name. I am right where I need to be.")
        player<Angry>("What do you mean? You won't escape.")
        npc<Angry>("You cannot stop me, $name. But just in case you try, allow me to introduce you to someone...")
        surok.face(Direction.SOUTH_WEST)
        surok.say("Bork! Kill the meddler!")
        delay(1)
        face(Direction.SOUTH_WEST)
        moveCamera(offset.tile(3093, 5532), height = 600, speed = 10, acceleration = 20)
        turnCamera(offset.tile(3102, 5539), height = 400, speed = 10, acceleration = 20)
        delay(3)
        anim("emote_panic")
        delay(2)
        open("bork_display")
        delay(14)
        close("bork_display")
        player<Shock>("Oh boy...")
        clearCamera()
        surok.walkTo(offset.tile(3099, 5547))
        surok.transform("surok_magis_attack")
        // TODO replace with fighting surok
        bork.transform("bork")
        bork.interactPlayer(player, "Attack")
        surok.interactPlayer(player, "Attack")
    }
}
