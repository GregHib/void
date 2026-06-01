package content.area.misthalin.lumbridge.catacomb

import content.entity.combat.killer
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Frustrated
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.Teary
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.quest.exitInstance
import content.quest.instance
import content.quest.instanceOffset
import content.quest.quest
import content.quest.setInstanceLogout
import content.quest.smallInstance
import content.quest.startCutscene
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.longQueue
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.RegionLevel
import world.gregs.voidps.type.Tile

class LumbridgeCatacombs : Script {

    init {
        objTeleportTakeOff("Climb-down", "lumbridge_catacomb_stairs") { _, _ ->
            when (quest("blood_pact")) {
                "unstarted" -> {
                    val xenia = NPCs.find(RegionLevel(12849), "xenia")
                    queue("xenia_greet") {
                        talkWith(xenia) {
                            npc<Neutral>("Hey! I want to talk to you!")
                        }
                    }
                    Teleport.CANCEL
                }

                "completed" -> Teleport.CONTINUE
                else -> {
                    setInstanceLogout(Tile(3246, 3198, 0))

                    if (quest("blood_pact") == "started") {
                        longQueue("blood_pact_intro") {
                            open("fade_out")
                            cutscene()
                        }
                    } else {
                        tele(instanceOffset().tile(3877, 5528, 1))

                        face(Direction.NORTH)
                    }
                    Teleport.CANCEL
                }
            }
        }

        objectOperate("Climb-up", "lumbridge_catacomb_exit_stairs") {
            if (instance() != null) {
                exitInstance()
            } else {
                tele(3246, 3198,0)
            }
        }

        objectOperate("Take", "*_demon_statuette") { (target) ->
            val def = target.def(this)
            if (get(def.stringId, "take") != "take") {
                message("You've already taken this statuette.")
                return@objectOperate
            }
            statement("The air grows tense as you approach the statuette. You sense a hostile presence nearby...")

            choice {
                option("Take the statuette.") {
                    if (inventory.add(def.stringId)) {
                        set(def.stringId, "plinth")
                        message("You carefully take the ${def.stringId}")
                    } else {
                        inventoryFull()
                    }
                }
                option("Leave it alone.")
            }
        }

        objectOperate("Take", "diamond_demon_statuette") {
            if (get("diamond_demon_statuette", "take_shield") != "take") {
                return@objectOperate
            }
            if (inventory.add("diamond_demon_statuette")) {
                set("diamond_demon_statuette", "touch")
            }
        }

        npcDeath("dragith_nurn") {
            val killer = killer
            if (killer is Player) {
                killer.message("With Dragith Nurn defeated, the diamond statuette is now within your grasp.")
                killer["diamond_demon_statuette"] = "take"
            }
        }

        destroyed("*_demon_statuette") { item ->
            set(item.id, "take")
        }
    }

    suspend fun Player.cutscene() {
        delay(3)
        val instance = smallInstance(Region(15446))
        val offset = instanceOffset()
        val cutscene = startCutscene("blood_pact_intro", instance, offset)

        tele(offset.tile(3878, 5548, 1), clearInterfaces = false)
        face(Direction.SOUTH)

        cutscene.onEnd(destroyInstance = false) {
            open("fade_out")
            tele(instanceOffset().tile(3246, 3198, 0))
            face(Direction.NORTH)
            clearCamera()
        }

        //added, so we have can use delays between dialogs, without ending the cutscene, but still
        //having the saftey net, if the player dc'ed in the middle
        queue.clear("blood_pact_intro_cutscene_end")
        longQueue("blood_pact_intro_cutscene_end", 6000) {
            cutscene.end(destroyInstance = false)
        }

        // spawn cutscene Xenia NPC, move camera, play dialogue...
        //spawn kayle 3876, 5531, 1
        val kayle = NPCs.add("kayle_cutscene", offset.tile(3876, 5532, 1), Direction.NORTH)
        //spawn resee 3877, 5532, 1
        val reese = NPCs.add("reese_cutscene", offset.tile(3877, 5532, 1), Direction.NORTH)
        //spawn caitlin 3878, 5532, 1
        val caitlin = NPCs.add("caitlin_cutscene", offset.tile(3878, 5532, 1), Direction.NORTH)
        //spawn prison 3877, 5533, 1
        val ilona = NPCs.add("ilona_cutscene", offset.tile(3877, 5533, 1), Direction.NORTH)

        delay(2)

        clearCamera()
        //camera at 3876, 5545, 0 looking at 3877, 5531, 1
        moveCamera(offset.tile(3876, 5546, 1), 350)
        turnCamera(offset.tile(3877, 5531, 1), 220)

        open("fade_in")

        delay(1)
        //move all until

        //kyle 3876, 5537, 1
        kayle.walkTo(offset.tile(3876, 5535, 1))
        //cait 3878, 5540, 1
        caitlin.walkTo(offset.tile(3878, 5541, 1))
        //ilona 3877, 5541, 1
        ilona.walkTo(offset.tile(3877, 5542, 1))
        //reese 3877, 5538, 1
        delay(1)
        reese.walkTo(offset.tile(3877, 5540, 1))

        delay(10)
        reese.face(kayle)
        npc<Frustrated>("reese_cutscene", "Come on, Kayle! We don't have forever.")
        kayle.walkTo(offset.tile(3876, 5539, 1))
        delay(4)
        kayle.face(reese)

        npc<Teary>("kayle_cutscene", "Look, Reese; are you sure about this? There must be some other way we can...")

        reese.face(kayle)
        npc<Frustrated>("reese_cutscene", "We made a blood pact, Kayle! The three of us are in this all the way.")
        npc<Teary>("kayle_cutscene", "Yes, but...")

        caitlin.face(reese)
        npc<Frustrated>("caitlin_cutscene", "Do we have to take this idiot?")
        reese.face(caitlin)
        npc<Angry>("reese_cutscene", "Yes! The blood pact! You read the book!")

        npc<Scared>("ilona_cutscene", "Let me go! I didn't make any blood pact with-")
        reese.face(ilona)
        npc<Angry>("reese_cutscene", "Shut up!")

        reese.face(kayle)
        npc<Frustrated>("reese_cutscene", "Kayle, you stay here. Guard the door.")
        reese.face(ilona)
        npc<Frustrated>("reese_cutscene", "You, come on.")
        reese.face(Direction.NORTH)

        //kayle stays, rest walks towards the end - fade out
        //resee 3877, 5544, 1
        reese.walkTo(offset.tile(3877, 5544, 1))
        // cait 3878, 5544, 1
        caitlin.walkTo(offset.tile(3878, 5544, 1))
        // prison 3877, 5545, 1
        ilona.walkTo(offset.tile(3877, 5545, 1))

        //fade out + removal of npcs
        delay(1)
        open("fade_out")
        delay(4)
        NPCs.remove(ilona)
        NPCs.remove(kayle)
        NPCs.remove(caitlin)
        NPCs.remove(reese)

        cutscene.end(destroyInstance = false)
        if (instance() != null) {
            set("blood_pact", "watched_cutscene")
            queue.clear("blood_pact_intro_cutscene_end")
            tele(offset.tile(3877, 5527, 1))
            face(Direction.SOUTH)
            spawnInstance(offset)
            clearCamera()
            clearAnim()
            open("fade_in")
            delay(1)
            npc<Neutral>("xenia_after_cutscene", "Looks like there's a guard ahead. We should take him together.")
            val xenia = NPCs.findOrNull(instance()!!.toLevel(1), "xenia_after_cutscene") ?: return
            xenia.running = true
            xenia.mode = Follow(xenia, this)
        }
    }

    fun spawnInstance(offset: Delta) {
        NPCs.add("xenia_after_cutscene", offset.tile(3877, 5526, 1), Direction.NORTH)
        NPCs.add("kayle_attackable", offset.tile(3877, 5543, 1), Direction.SOUTH)
        NPCs.add("caitlin_attackable", offset.tile(3864, 5538, 1), Direction.EAST)
        NPCs.add("ilona_tied", offset.tile(3865, 5523, 2), Direction.NORTH)
        NPCs.add("reese_attackable", offset.tile(3865, 5523, 2), Direction.SOUTH)
    }
}
