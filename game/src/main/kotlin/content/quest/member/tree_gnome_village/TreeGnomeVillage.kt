package content.quest.member.tree_gnome_village

import content.entity.combat.Combat
import content.entity.combat.killer
import content.entity.obj.door.enterDoor
import content.entity.obj.door.openDoor
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.quest
import content.quest.questJournal
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class TreeGnomeVillage : Script {

    init {
        npcOperate("Follow", "elkoy_tree_gnome_village,elkoy_tree_gnome_village_2,elkoy_tree_gnome_village_4") { (target) ->
            if (questStage("tree_gnome_village") == 0) {
                return@npcOperate
            }
            statement("Elkoy escorts you through the maze...", clickToContinue = false)
            open("fade_out")
            delay(3)
            tele(if (target.tile.y >= 3180) Tile(2514, 3160) else Tile(2504, 3192))
            delay(1)
            open("fade_in")
            delay(3)
            close("dialogue_message_np1")
        }

        objectOperate("Open", "door_39_closed") { (target) ->
            if (target.tile == Tile(2502, 3250)) {
                if (tile.y > target.tile.y) {
                    enterDoor(target)
                } else {
                    statement("The door seems to be locked from the inside. I'll need to find another way to get in.")
                }
            } else {
                openDoor(target)
            }
        }

        itemOnNPCOperate("logs", "commander_montai_battlefield") {
            if (TreeGnomeVillageProgress.deliverLogs(this)) {
                message("You give Commander Montai six logs.")
            } else if (quest("tree_gnome_village") == "started") {
                message("You need six normal logs to repair the defences.")
            }
        }

        objectOperate("Fire", "tree_gnome_village_ballista") { (target) ->
            if (questStage("tree_gnome_village") >= 4) {
                statement("The Khazard stronghold has already been breached.")
                return@objectOperate
            }
            if (questStage("tree_gnome_village") < 2) {
                statement("The ballista is damaged. It cannot be used until the gnomes have finished their repairs.")
                return@objectOperate
            }
            if (quest("tree_gnome_village") != "coordinates_obtained") {
                message("You need the trackers' coordinates before firing the ballista.")
                return@objectOperate
            }
            player<Neutral>("That tracker gnome was a bit vague about the x coordinate! What could it be?")
            choice("Enter the x-coordinate of the stronghold") {
                for (coordinate in 1..4) {
                    option(coordinate.toString().padStart(4, '0')) {
                        statement("You enter the height and y coordinates you got from the tracker gnomes.")
                        target.anim("tree_gnome_village_ballista_fire")
                        if (TreeGnomeVillageProgress.fire(this, coordinate)) {
                            statement("The huge spear flies through the air and screams down directly into the Khazard stronghold.")
                            statement("A deafening crash echoes over the battlefield as the front entrance is reduced to rubble.")
                        } else {
                            statement("The huge spear completely misses the Khazard stronghold!")
                            player<Neutral>("Evidently that wasn't the right x coordinate.")
                        }
                    }
                }
            }
        }

        objectOperate("Squeeze-through", "tree_gnome_village_railing") { (target) ->
            tele(if (tile.x < target.tile.x) target.tile else target.tile.add(Direction.WEST))
        }

        objectOperate("Climb-over", "tree_gnome_village_crumbled_wall") { (target) ->
            val entering = tile.y <= target.tile.y
            if (tile.y <= target.tile.y && questStage("tree_gnome_village") < 4) {
                message("You need to break through the wall with the ballista first.")
                return@objectOperate
            }
            cross(target, Direction.NORTH, "rocks_pile_climb")
            if (entering && !get("tree_gnome_village_commander_met", false)) {
                val commander = NPCs.at(tile.regionLevel).firstOrNull { it.id == "khazard_commander_battlefield" && it.tile.level == 0 && !it.dead }
                if (commander != null) {
                    commander.say("What? How did you manage to get in here.")
                    delay(2)
                    say("I've come for the orb.")
                    delay(2)
                    commander.say("I'll never let you take it.")
                    delay(2)
                    set("tree_gnome_village_commander_met", true)
                    Combat.combat(commander, this)
                }
            }
        }

        objectOperate("Open", "tree_gnome_village_chest") { (target) ->
            if (questStage("tree_gnome_village") < 4) {
                message("You have no reason to search this chest yet.")
                return@objectOperate
            }
            target.replace("tree_gnome_village_chest_open", ticks = 100)
            val commander = NPCs.at(tile.regionLevel).firstOrNull { it.id == "khazard_commander_battlefield" && it.tile.level == 1 && !it.dead }
            if (commander != null) {
                commander.say("Oi! You! Get out of there.")
                Combat.combat(commander, this)
            }
        }

        objectOperate("Search", "tree_gnome_village_chest_open") { (target) ->
            // The open model is also used by a chest near the warlord.
            if (target.tile != Tile(2506, 3259, 1)) {
                message("You find nothing of interest.")
                return@objectOperate
            }
            if (TreeGnomeVillageProgress.recoverOrb(this)) {
                message("You search the chest. Inside you find gnomes' stolen orb of protection.")
            } else {
                message("You have already retrieved the orb of protection.")
            }
        }

        objectOperate("Close", "tree_gnome_village_chest_open") { (target) ->
            if (target.tile == Tile(2506, 3259, 1)) {
                target.replace("tree_gnome_village_chest")
            }
        }

        npcDeath("khazard_warlord_underground_pass") {
            val player = killer as? Player ?: return@npcDeath
            if (TreeGnomeVillageProgress.defeatWarlord(player)) {
                Script.launch {
                    player.statement("As the warlord falls to the ground, a ghostly vapour floats upwards from his battle-worn armour. Out of sight you hear a shrill scream in the still air. You spot the orbs of protection among his remains.")
                }
            } else if (player.quest("tree_gnome_village") == "warlord_defeated") {
                player.message("You need a free inventory space to recover the orbs.")
            }
        }

        questJournalOpen("tree_gnome_village") {
            val lines = mutableListOf<String>()
            val stage = questStage("tree_gnome_village")
            if (stage == 0) {
                lines.add("<navy>I can start this quest by speaking to <maroon>Bolren<navy> at the center of the")
                lines.add("<navy><maroon>Tree Gnome maze<navy>, West of <maroon>Port Khazard<navy>.")
            } else if (stage >= 1) {
                lines.add("<str>I spoke to King Bolren who told me that one of their orbs of")
                lines.add("<str>protection has been stolen by Khazard troops.")
                lines.add("")
            }
            if (stage == 1) {
                lines.add("<maroon>Commander Montai<navy>, <maroon>North<navy> of the maze, can help me get it")
            } else if (stage >= 2) {
                lines.add("<str>I spoke to Montai. The orb was in the Khazard stronghold North of")
                lines.add("<str>the battlefield, but it was secure in there.")
                lines.add("")
            }
            if (stage == 2) {
                lines.add("<navy>I need to bring Montai <maroon>logs<navy> to fortify their defences")
            } else if (stage == 3) {
                lines.add("<str>I brought Montai logs to help fortify their defences.")
                lines.add("<navy>I should speak to <maroon>Montai<navy> again and see what to do next")
            } else if (stage >= 4) {
                lines.add("<str>I brought Montai logs to help fortify their defences.")
                lines.add("<str>Now their defences were secure, they were ready to use the")
                lines.add("<str>Gnome Ballista to break through the enemy defences.")
                lines.add("")
            }
            if (stage == 4) {
                lines.add("<navy>I need to head into the <maroon>battlefield<navy> and find the <maroon>three Gnome")
                lines.add("<maroon>trackers<navy> who have the <maroon>firing coordinates<navy> and then fire the <maroon>ballista!")
            } else if (stage == 5) {
                lines.add("<navy>I need to head into the <maroon>battlefield<navy> and find the <maroon>three Gnome")
                lines.add("<maroon>trackers<navy> who have the <maroon>firing coordinates<navy> and then fire the <maroon>ballista!")
                lines.add("<navy>Unfortunately the tracker who knew the <maroon>x<navy> coordinate has gone")
                lines.add("<navy>insane, so I'll have to guess that coordinate myself")
            } else if (stage == 6) {
                lines.add("<navy>I need to head into the <maroon>battlefield<navy> and find the <maroon>three Gnome")
                lines.add("<maroon>trackers<navy> who have the <maroon>firing coordinates<navy> and then fire the <maroon>ballista!")
                lines.add("<navy>I know the <maroon>height<navy> coordinate")
                lines.add("<navy>Unfortunately the tracker who knew the <maroon>x<navy> coordinate has gone")
                lines.add("<navy>insane, so I'll have to guess that coordinate myself")
            } else if (stage == 7) {
                lines.add("<navy>I need to head into the <maroon>battlefield<navy> and find the <maroon>three Gnome")
                lines.add("<maroon>trackers<navy> who have the <maroon>firing coordinates<navy> and then fire the <maroon>ballista!")
                lines.add("<navy>I know the <maroon>y<navy> coordinate")
                lines.add("<navy>I know the <maroon>height<navy> coordinate")
                lines.add("<navy>Unfortunately the tracker who knew the <maroon>x<navy> coordinate has gone")
                lines.add("<navy>insane, so I'll have to guess that coordinate myself")
            } else if (stage == 8) {
                lines.add("<str>I found the three trackers and used their coordinates to fire the")
                lines.add("<str>ballista straight into the Khazard stronghold.")
                lines.add("<navy>With the <maroon>Khazard stronghold<navy> exposed I should get inside and")
                lines.add("<navy>retrieve the gnomes' <maroon>orb of protection")
            } else if (stage >= 9) {
                lines.add("<str>I found the three trackers and used their coordinates to fire the")
                lines.add("<str>ballista straight into the Khazard stronghold.")
                lines.add("<str>With the stronghold breached by ballista fire, I was able to make")
                lines.add("<str>my way inside and recover the orb of protection.")
                lines.add("")
            }
            if (stage == 9) {
                lines.add("<navy>I should return the <maroon>orb<navy> to <maroon>King Bolren<navy> immediately")
            } else if (stage >= 10) {
                lines.add("<str>I returned the orb to King Bolren, but while I was busy getting it")
                lines.add("<str>the Khazard troops invaded the village and stole the remaining two")
                lines.add("<str>orbs before heading north.")
                lines.add("")
            }
            if (stage == 10) {
                lines.add("<str>After a fierce battle I defeated the warlord who'd stolen the orbs,")
                lines.add("<str>and reclaimed them for the gnome people.")
                lines.add("<navy>I should return them now to <maroon>King Bolren<navy> where they belong")
            } else if (stage == 11) {
                lines.add("<str>After a fierce battle I defeated the warlord who'd stolen the orbs,")
                lines.add("<str>and reclaimed them for the gnome people.")
                lines.add("<navy>I should speak to King Bolren now - hopefully he will reward me")
            } else if (stage >= 12) {
                lines.add("<str>After a fierce battle I defeated the warlord who'd stolen the orbs,")
                lines.add("<str>and reclaimed them for the gnome people.")
                lines.add("<str>I returned them to King Bolren, and was rewarded for all of my")
                lines.add("<str>help to the Gnome people in recovering the orbs.")
                lines.add("")
            }
            if (stage >= 12) {
                lines.add("<str>I can now use the gnome's spirit tree to teleport around the world")
                lines.add("<str>to places that are linked to the spirit tree!")
                lines.add("")
            }
            if (stage == 12) {
                lines.add("<red>QUEST COMPLETE!")
            }
            questJournal("Tree Gnome Village", lines)
        }
    }

    private suspend fun Player.cross(target: GameObject, axis: Direction, animation: String) {
        val direction = when (axis) {
            Direction.WEST -> if (tile.x < target.tile.x) Direction.EAST else Direction.WEST
            Direction.NORTH -> if (tile.y > target.tile.y) Direction.SOUTH else Direction.NORTH
            else -> return
        }
        val start = if (direction == axis) target.tile else target.tile.minus(direction)
        walkOverDelay(start)
        face(direction)
        delay()
        anim(animation)
        val destination = if (direction == axis) target.tile.add(direction) else target.tile
        exactMoveDelay(destination, 30, direction = direction)
    }
}
