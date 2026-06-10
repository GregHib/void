package content.area.misthalin.lumbridge.blood_pact

import content.entity.combat.dead
import content.entity.combat.killer
import content.entity.effect.transform
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.quest.instanceOffset
import content.quest.quest
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape

class Reese : Script {
    init {
        // Interacting with Reese's chamber door triggers the intro dialog;
        // interacting with Caitlin's area gates (same object) just shows a blocked message.
        objectOperate("Open", "blood_pact_tomb_door") { (target) ->
            when (quest("blood_pact")) {
                "reese" -> {
                    val offset = instanceOffset()
                    if (target.tile != offset.tile(3866, 5527, 0)) {
                        message("This gate won't budge.")
                        return@objectOperate
                    }
                    if (get("blood_pact_reese_door", false)) {
                        // Door dialog already triggered; just remove it so the player can pass
                        GameObjects.remove(target)
                        return@objectOperate
                    }

                    set("blood_pact_reese_door", true)

                    val reese = NPCs.find(offset.tile(3865, 5525, 0), "reese_attackable")

                    npc<Angry>("reese_attackable", "The potion is complete. Where are they? The whole group should be present.")

                    npc<Scared>("ilona_tied", "Let me go, you-")

                    npc<Angry>("reese_attackable", "Shut up!")

                    GameObjects.remove(target)
                    val slidingDoor = GameObjects.add("tomb_door_sliding_down", offset.tile(3866, 5527, 0), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)
                    delay(1)
                    GameObjects.remove(slidingDoor)

                    talkWith(reese) {
                        npc<Angry>("Who are you? What are you doing here?")
                        reeseOptionsBeforeFight()
                    }
                    reese.interactPlayer(this, "Attack")

                    reese.huntMode = "aggressive"
                }
            }
        }

        npcAfterDeath("reese_attackable") {
            dead = false
            mode = EmptyMode
            huntMode = ""
            levels.restore(Skill.Constitution)
            val player = killer as? Player
            if (player != null) {
                player["blood_pact_reese"] = "defeated"
                val original = tile.minus(player.instanceOffset())
                player["blood_pact_reese_tile"] = original.id
                player.refreshQuestJournal()
            }
            transform("reese_defeated")
        }

        npcOperate("Talk-to", "reese_defeated") { (target) ->
            npc<Angry>("You've beaten me, adventurer.")
            npc<Angry>("Now strike the final blow! End the blood pact in this tomb.")
            initialOptions(target)
        }
    }

    suspend fun Player.initialOptions(target: NPC) {
        choice {
            option<Neutral>("I have some questions.") {
                npc<Angry>("Ask your questions.")
                questionToReese(target)
            }
            option<Angry>("Time for you to die!") {
                killReese(target, "Reese dies. The fake tomb of Dragith Nurn breaks, revealing stairs to the last level of the catacombs.\n")
            }
            option<Neutral>("I'm not killing you. Give me your stuff and get out of here.") {
                npc<Angry>("No! There must be a death! The blood pact must be complete!")
                killReese(target, "Reese drinks a poisonous potion, and dies. The fake tomb of Dragith Nurn breaks, revealing stairs to the last level of the catacombs.")
            }
        }
    }

    suspend fun Player.reeseOptionsBeforeFight() {
        choice {
            option("My Name is $name. I'm an adventurer") {
                npc<Angry>("This will be your tomb, adventurer. The blood pact will prevail!")
            }
            option("I'm $name. Don't worry, Ilona, I'm here to rescue you.") {
                npc<Scared>("ilona_tied", "Thank Saradomin! He's insane! He's going to kill me!")
                npc<Angry>("Maybe you can take her place as the sacrifice, adventurer. Stand and fight!")
            }
            if (get<String>("blood_pact_kayle") == "killed" && get<String>("blood_pact_caitlin") == "killed") {
                option("I'm the one who killed your lackeys. Think you can do better?") {
                    npc<Angry>("They were weak. Zamorak will turn his face from them - but he will smile on me when I offer him your blood!")
                }
            } else if (get<String>("blood_pact_kayle") == "spared" && get<String>("blood_pact_caitlin") == "spared") {
                option("I let both the others live. This doesn't have to end in violence.") {
                    npc<Angry>("They were weak. Zamorak will turn his face from them - but he will smile on me when I offer him your blood!")
                }
            } else {
                option("I'm the one who defeated your lackeys. Think you can do better?") {
                    npc<Angry>("They were weak. Zamorak will turn his face from them - but he will smile on me when I offer him your blood!")
                }
            }
            option("I'm your worst nightmare, Zamorakian scum!") {
                npc<Angry>("This will be your tomb, adventurer. The blood pact will prevail!")
            }
        }
    }

    suspend fun Player.killReese(target: NPC, msg: String) {
        set("blood_pact_reese", "killed")
        target.anim("reese_death")
        delay(4)
        NPCs.remove(target)
        // TODO: make drop on death tile
        FloorItems.add(instanceOffset().tile(3865, 5525, 0), "reeses_sword", disappearTicks = 300, owner = this)
        // Altar crumbles when Reese dies
        val altar = GameObjects.findOrNull(instanceOffset().tile(3865, 5524, 0), "blood_pact_altar")
        if (altar != null) GameObjects.remove(altar)
        val crumblingAltar = GameObjects.add("blood_pact_altar_crumbling", instanceOffset().tile(3865, 5524, 0), ObjectShape.CENTRE_PIECE_STRAIGHT, 2)
        delay(1)
        statement(msg)
        delay(1)
        GameObjects.remove(crumblingAltar)
        npc<Scared>("ilona_tied", "Help! Untie me so we can get out of here!")
    }

    suspend fun Player.questionToReese(target: NPC) {
        choice {
            option<Neutral>("Who are you?") {
                npc<Angry>("I am Reese! Warrior of Zamorak and leader of the blood pact!")
                questionToReese(target)
            }
            option<Neutral>("Who were the others?") {
                npc<Angry>("Faithful servants of Zamorak! He is the god of chaos and destruction. We bound ourselves to his service!")
                questionToReese(target)
            }
            option<Neutral>("What were you planning to do down here?") {
                npc<Angry>("End Saradomin's dominance over Lumbridge! The tyrant god shall fall.")
                npc<Angry>("With the blood pact, and the power of the tomb of Dragith Nurn, we would send an army of the dead to claim this town for Zamorak!")
                questionToReese(target)
            }
            option<Neutral>("Enough questions.") {
                npc<Angry>("Now strike the final blow!")
                initialOptions(target)
            }
        }
    }
}
