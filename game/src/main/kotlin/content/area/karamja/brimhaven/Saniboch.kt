package content.area.karamja.brimhaven

import content.entity.obj.objTeleportTakeOff
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.queue

@Script
class Saniboch {

    val dungeonEntryFee = 875

    init {
        npcOperate("Talk-to", "saniboch") {
            npc<Talk>("Good day to you, Bwana.")

            choice {
                option<Quiz>("Can I go through that door please?") {
                    if (player["can_enter_brimhaven_dungeon", false]) {
                        npc<Talk>("Most certainly, you have already given me lots of nice coins.")
                        return@option
                    }

                    npc<Talk>("Most certainly, but I must charge you the sum of 875 coins first.")
                    if (player.inventory.contains("coins", dungeonEntryFee)) {
                        choice {
                            option("Okay, here's 875 coins.") {
                                player.inventory.remove("coins", dungeonEntryFee)
                                player["can_enter_brimhaven_dungeon"] = true
                                statement("You pay Saniboch 875 coins.")
                                npc<Talk>("Many thanks. You may now pass the door. May your death be a glorious one!")
                            }
                            option("Never mind.") {
                                player<Talk>("Never mind.")
                            }
                            option("Why is it worth the entry cost?") {
                                player<Talk>("Why is it worth the entry cost?")
                                npc<Talk>("It leads to a huge fearsome dungeon, populated by giants and strange dogs. Adventurers come from all around to explore its depths.")
                                npc<Talk>("I know not what lies deeper in myself, for my skills in agility and woodcutting are inadequate, but I hear tell of even greater dangers deeper in.")
                            }
                        }
                    } else {
                        player<Talk>("I don't have the money on me at the moment.")
                        npc<Talk>("Well this is a dungeon for the more wealthy discerning adventurer. Begone with you, riff raff.")
                        player<Talk>("But you don't even have clothes, how can you seriously call anyone riff raff.")
                        npc<Talk>("Hummph.")
                    }
                }

                option<Quiz>("Where does this strange entrance lead?") {
                    npc<Happy>("To a huge fearsome dungeon, populated by giants and strange dogs. Adventurers come from all around to explore its depths.")
                    npc<Talk>("I know not what lies deeper in myself, for my skills in agility and woodcutting are inadequate.")
                }

                option("Good day to you too.") {
                    player<Talk>("Good day to you too.")
                }

                option("I'm impressed, that tree is growing on that shed.") {
                    player<Talk>("I'm impressed, that tree is growing on that shed.")
                    npc<Talk>("My employer tells me it is an uncommon sort of tree called the Fyburglars tree.")
                }
            }
        }

        npcOperate("Pay", "saniboch") {
            if (player["can_enter_brimhaven_dungeon", false]) {
                npc<Talk>("You have already given me lots of nice coins, you may go in.")
                return@npcOperate
            }

            val coins = player.inventory.count("coins")
            if (coins >= dungeonEntryFee) {
                player.inventory.remove("coins", dungeonEntryFee)
                player["can_enter_brimhaven_dungeon"] = true
                statement("You pay Saniboch 875 coins.")
                npc<Talk>("Many thanks. You may now pass the door. May your death be a glorious one!")
            } else {
                npc<Talk>("I'll want 875 coins to let you enter.")
                npc<Talk>("Well this is a dungeon for the more wealthy discerning adventurer. Begone with you, riff raff.")
            }
        }

        objTeleportTakeOff("Enter", "brimhaven_dungeon_entrance") {
            if (!player["can_enter_brimhaven_dungeon", false]) {
                cancel()
                player.queue("saniboch_door_access_check") {
                    statement("You can't go in there without paying!")
                }
                return@objTeleportTakeOff
            }

            // Reset access after one-time use
            player["can_enter_brimhaven_dungeon"] = false
        }
    }

    // Saniboch "Talk-to" dialogue

    // Saniboch "Pay" right-click option

    // Door object to enter dungeon
}
