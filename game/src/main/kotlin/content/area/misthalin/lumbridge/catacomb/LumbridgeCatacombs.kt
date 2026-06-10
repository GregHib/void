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
import content.entity.world.music.unlockTrack
import content.quest.exitInstance
import content.quest.instance
import content.quest.instanceOffset
import content.quest.quest
import content.quest.questComplete
import content.quest.refreshQuestJournal
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
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.mode.ModeType
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
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
        moved {
            val tiles = listOf(instanceOffset().tile(3871, 5543, 1), instanceOffset().tile(3872, 5543, 1))
            if (tile in tiles && get("blood_pact_kayle") !in listOf("spared", "killed")) {
                queue("stop_player_from_moving") {
                    walkTo(instanceOffset().tile(3873, 5543, 1))
                    statement("You should deal with the first Cultist before advancing further.")
                }
            }
        }
        objTeleportTakeOff("Climb-down", "lumbridge_catacomb_stairs") { _, _ ->
            when (quest("blood_pact")) {
                "unstarted" -> {
                    val xenia = NPCs.findOrNull(RegionLevel(12849), "xenia")
                        ?: NPCs.findOrNull(RegionLevel(12850), "xenia")
                    if (xenia != null) {
                        queue("xenia_greet") {
                            talkWith(xenia) {
                                npc<Neutral>("Hey! I want to talk to you!")
                            }
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
                            delay(1)
                            cutscene()
                        }
                    } else {
                        longQueue("blood_pact_reenter") {
                            open("fade_out")
                            delay(1)
                            smallInstance(Region(15446))
                            val offset = instanceOffset()
                            val destination = offset.tile(3877, 5528, 1)
                            tele(destination)
                            face(Direction.NORTH)
                            respawnInstance(offset)
                            open("fade_in")
                        }
                    }
                    Teleport.CANCEL
                }
            }
        }

        objectOperate("Climb-up", "lumbridge_catacomb_exit_stairs") {
            if (instance() != null) {
                exitInstance()
            } else {
                tele(3246, 3198, 0)
            }
        }

        // Winch in Caitlin's gallery — removes the gates blocking the stairs once Caitlin is dealt with
        objectOperate("Operate", "blood_pact_winch") {
            if (get<String>("blood_pact_caitlin") != "defeated") {
                statement("You should deal with the second Cultist, before trying to operate the winch.")
                return@objectOperate
            }
            if (quest("blood_pact") != "caitlin") {
                message("There's no reason to operate this.")
                return@objectOperate
            }

            statement("You operate the winch. The gates creak open.")
            val gate1 = GameObjects.findOrNull(instanceOffset().tile(3870, 5531, 1), "blood_pact_caitlin_gate")
            val gate2 = GameObjects.findOrNull(instanceOffset().tile(3862, 5531, 1), "blood_pact_caitlin_gate")
            set("blood_pact", "winch_activated")
            if (gate1 != null) GameObjects.remove(gate1)
            if (gate2 != null) GameObjects.remove(gate2)
        }

        // Stairs down from Caitlin's room (level 1) to Reese's chamber (level 2)
        objectOperate("Climb-down", "blood_pact_stairs_down_south") {
            when (quest("blood_pact")) {
                "reese", "untied_ilona" -> {
                    tele(instanceOffset().tile(3861, 5533, 0))
                    face(Direction.NORTH)
                }
                "completed" -> {
                    tele(Tile(3861, 5533, 0))
                    face(Direction.NORTH)
                }
                else -> statement("You should deal with the second Cultist first.")
            }
        }

        // Stairs back up from Reese's chamber (level 2) to Caitlin's room (level 1)
        objectOperate("Climb-up", "blood_pact_stairs_up_south") {
            when (quest("blood_pact")) {
                "completed" -> {
                    tele(Tile(3857, 5533, 1))
                    face(Direction.SOUTH)
                }
                "reese" -> {
                    statement("You should deal with the third Cultist first.")
                }
                else -> {
                    tele(instanceOffset().tile(3857, 5533, 1))
                    face(Direction.SOUTH)
                }
            }
        }

        // Stairs down from Caitlin's room (level 1) to Reese's chamber (level 2)
        objectOperate("Climb-down", "blood_pact_stairs_down_north") {
            when (quest("blood_pact")) {
                "reese", "untied_ilona" -> {
                    tele(instanceOffset().tile(3861, 5543, 0))
                    face(Direction.NORTH)
                }
                "completed" -> {
                    tele(Tile(3861, 5543, 0))
                    face(Direction.NORTH)
                }
                else -> statement("You should deal with the second Cultist first.")
            }
        }

        // Stairs back up from Reese's chamber (level 2) to Caitlin's room (level 1)
        objectOperate("Climb-up", "blood_pact_stairs_up_north") {
            when (quest("blood_pact")) {
                "completed" -> {
                    tele(Tile(3857, 5543, 1))
                    face(Direction.SOUTH)
                }
                "reese" -> statement("You should deal with the third Cultist first.")
                else -> {
                    tele(instanceOffset().tile(3857, 5543, 1))
                    face(Direction.SOUTH)
                }
            }
        }

        objectOperate("Climb-down", "blood_pact_tomb_stairs_down") {
            if (quest("blood_pact") != "completed") {
                statement("You should take the prisoner to the surface and speak to Xenia before you venture deeper into the dungeon.")
            } else {
                tele(Tile(3972, 5565, 0))
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
        delay(1)
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

        queue.clear("blood_pact_intro_cutscene_end")
        longQueue("blood_pact_intro_cutscene_end", 6000) {
            cutscene.end(destroyInstance = false)
        }

        val kayle = NPCs.add("kayle_cutscene", offset.tile(3876, 5532, 1), Direction.NORTH)
        val reese = NPCs.add("reese_cutscene", offset.tile(3877, 5532, 1), Direction.NORTH)
        val caitlin = NPCs.add("caitlin_cutscene", offset.tile(3878, 5532, 1), Direction.NORTH)
        val ilona = NPCs.add("ilona_cutscene", offset.tile(3877, 5533, 1), Direction.NORTH)

        delay(2)

        clearCamera()
        moveCamera(offset.tile(3876, 5546, 1), 350)
        turnCamera(offset.tile(3877, 5531, 1), 220)

        open("fade_in")

        delay(1)

        kayle.walkTo(offset.tile(3876, 5535, 1))
        caitlin.walkTo(offset.tile(3878, 5541, 1))
        ilona.walkTo(offset.tile(3877, 5542, 1))
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

        reese.walkTo(offset.tile(3877, 5544, 1))
        caitlin.walkTo(offset.tile(3878, 5544, 1))
        ilona.walkTo(offset.tile(3877, 5545, 1))

        delay(1)
        open("fade_out")
        delay(2)
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
        NPCs.add("reese_attackable", offset.tile(3865, 5525, 0), Direction.SOUTH)
        NPCs.add("ilona_tied", offset.tile(3865, 5523, 0), Direction.NORTH)

        NPCDefinitions.get("kayle_attackable").walkMode = ModeType.EMPTY_MOVEABLE.toByte()
        NPCDefinitions.get("caitlin_attackable").walkMode = ModeType.EMPTY_MOVEABLE.toByte()
        NPCDefinitions.get("reese_attackable").walkMode = ModeType.EMPTY_MOVEABLE.toByte()

        // Gates in front of Caitlin's gallery (opened by winch after Caitlin is defeated)
        GameObjects.add("blood_pact_caitlin_gate", offset.tile(3870, 5531, 1), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)
        GameObjects.add("blood_pact_caitlin_gate", offset.tile(3862, 5531, 1), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)

        // Reese's chamber door (level 2)
        GameObjects.add("blood_pact_tomb_door", offset.tile(3866, 5527, 0), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)

        // Winch that opens the Caitlin gallery gates
        GameObjects.add("blood_pact_winch", offset.tile(3871, 5534, 1), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)

        // Stairs down from Caitlin's gallery (level 1) to Reese's chamber (level 2)
        spawnStairs(offset)

        // Altar in Reese's chamber
        GameObjects.add("blood_pact_altar", offset.tile(3865, 5524, 0), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)
    }

    fun Player.respawnInstance(offset: Delta) {
        val stage = quest("blood_pact")
        val kayleStatus = get<String>("blood_pact_kayle")
        val caitlinStatus = get<String>("blood_pact_caitlin")
        val reeseStatus = get<String>("blood_pact_reese")

        // Xenia — position advances through the dungeon as stages progress
        when (stage) {
            "watched_cutscene" ->
                NPCs.add("xenia_after_cutscene", offset.tile(3877, 5526, 1), Direction.NORTH)
            "xenia_wounded", "kayle" ->
                NPCs.add("xenia_wounded", offset.tile(3875, 5529, 1), Direction.SOUTH)
            "caitlin", "winch_activated" ->
                NPCs.add("xenia_wounded", offset.tile(3877, 5538, 1), Direction.SOUTH)
            "reese", "untied_ilona" ->
                NPCs.add("xenia_wounded", offset.tile(3864, 5536, 1), Direction.NORTH)
        }

        // Kayle — attackable until beaten, defeated NPC until player decides, then gone
        when (stage) {
            in listOf("watched_cutscene", "xenia_wounded") -> {
                NPCs.add("kayle_attackable", offset.tile(3877, 5543, 1), Direction.SOUTH)
                NPCDefinitions.get("kayle_attackable").walkMode = ModeType.EMPTY_MOVEABLE.toByte()
            }

            "kayle" if kayleStatus == "defeated" -> {
                val kayleTile = offset.tile(3877, 5543, 1)
                NPCs.add("kayle_defeated", kayleTile, Direction.SOUTH)
            }

            "kayle" if kayleStatus !in listOf("killed", "spared") ->
                NPCs.add("kayle_attackable", offset.tile(3877, 5543, 1), Direction.SOUTH)
        }

        // Caitlin — present in kayle/caitlin stages; gone once player moves to Reese
        when (stage) {
            in listOf("watched_cutscene", "xenia_wounded", "kayle") -> {
                NPCs.add("caitlin_attackable", offset.tile(3864, 5538, 1), Direction.EAST)
                NPCDefinitions.get("caitlin_attackable").walkMode = ModeType.EMPTY_MOVEABLE.toByte()
            }
            in listOf("caitlin", "winch_activated") if caitlinStatus == "defeated" -> {
                val caitlinTile = offset.tile(3864, 5538, 1)
                NPCs.add("caitlin_defeated", caitlinTile, Direction.EAST)
            }

            in listOf("caitlin", "winch_activated") if caitlinStatus !in listOf("killed", "spared") ->
                NPCs.add("caitlin_attackable", offset.tile(3864, 5538, 1), Direction.EAST)
        }

        // Reese — present until killed
        when (stage) {
            in listOf("watched_cutscene", "xenia_wounded", "kayle", "caitlin", "winch_activated") -> {
                NPCs.add("reese_attackable", offset.tile(3865, 5525, 0), Direction.SOUTH)
                NPCDefinitions.get("reese_attackable").walkMode = ModeType.EMPTY_MOVEABLE.toByte()
            }
            "reese" if reeseStatus == "defeated" -> {
                val reeseTile = offset.tile(3865, 5525, 0)
                NPCs.add("reese_defeated", reeseTile, Direction.SOUTH)
            }

            "reese" if reeseStatus !in listOf("killed", "spared") ->
                NPCs.add("reese_attackable", offset.tile(3865, 5525, 0), Direction.SOUTH)
        }

        // Ilona — tied until untied; once "untied_ilona" is reached, the player exits immediately
        when (stage) {
            !in listOf("untied_ilona") ->
                NPCs.add("ilona_tied", offset.tile(3865, 5523, 0), Direction.NORTH)
        }

        // Caitlin's gates: only closed if Caitlin has not yet been dealt with

        if (quest("blood_pact") !in listOf("winch_activated", "reese", "untied_ilona")) {
            GameObjects.add("blood_pact_caitlin_gate", offset.tile(3870, 5531, 1), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)
            GameObjects.add("blood_pact_caitlin_gate", offset.tile(3862, 5531, 1), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)
        }
        // Winch — present until Caitlin's gates are opened
        if (quest("blood_pact") !in listOf("winch_activated", "reese", "untied_ilona")) {
            GameObjects.add("blood_pact_winch", offset.tile(3871, 5534, 1), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)
        }
        // Reese's chamber door — always present when player could be on level 2, removed during fight trigger
        if (stage in listOf("watched_cutscene", "xenia_wounded", "kayle", "caitlin", "winch_activated", "reese")) {
            GameObjects.add("blood_pact_tomb_door", offset.tile(3866, 5527, 0), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)
        }

        // Stairs down from Caitlin's gallery (level 1) to Reese's chamber (level 2)
        spawnStairs(offset)

        // Altar — present until Reese is killed
        if (stage in listOf("watched_cutscene", "xenia_wounded", "kayle", "caitlin", "winch_activated") ||
            (stage == "reese" && reeseStatus != "killed")
        ) {
            GameObjects.add("blood_pact_altar", offset.tile(3865, 5524, 0), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)
        }
    }
}

fun spawnStairs(offset: Delta) {
    GameObjects.add("blood_pact_stairs_down_south", offset.tile(3858, 5533, 1), ObjectShape.CENTRE_PIECE_STRAIGHT, 3)

    // Stairs down from Caitlin's gallery (level 1) to Reese's chamber (level 2)
    GameObjects.add("blood_pact_stairs_down_north", offset.tile(3858, 5543, 1), ObjectShape.CENTRE_PIECE_STRAIGHT, 3)

    // Stairs up from Reese's chamber (level 2) back to Caitlin's gallery
    GameObjects.add("blood_pact_stairs_up_south", offset.tile(3858, 5533, 0), ObjectShape.CENTRE_PIECE_STRAIGHT, 3)
    // Altar in Reese's chamber
    GameObjects.add("blood_pact_stairs_up_north", offset.tile(3858, 5543, 0), ObjectShape.CENTRE_PIECE_STRAIGHT, 3)
}

fun Player.completeBloodPact() {
    set("blood_pact", "completed")
    inc("quest_points", 1)
    jingle("quest_complete_1")
    unlockTrack("catacomb")
    unlockTrack("cursed_you_are")
    exp(Skill.Attack, 100.0)
    exp(Skill.Strength, 100.0)
    exp(Skill.Defence, 100.0)
    exp(Skill.Ranged, 100.0)
    exp(Skill.Magic, 100.0)
    message("Congratulations, you've completed a quest: <navy>The Blood Pact")
    refreshQuestJournal()
    questComplete(
        "The Blood Pact",
        "1 Quest Point",
        "Kayle's sling, Caitlin's staff",
        "and Reese's sword",
        "100 Attack, Strength,",
        "Defence, Ranged and Magic",
        "XP",
        "Access to the Lumbridge",
        "Catacombs dungeon",
        item = "reeses_sword"
    )

    val xenia = NPCs.find(Tile(3245, 3198, 0), "xenia_2")
    NPCs.remove(xenia)
}
