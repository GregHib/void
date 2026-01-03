package content.area.karamja.brimhaven

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.queue

class Saniboch : Script {

    val dungeonEntryFee = 875

    init {
        npcOperate("Talk-to", "saniboch") {
            npc<Neutral>("Good day to you, Bwana.")

            choice {
                option<Quiz>("Can I go through that door please?") {
                    if (get("can_enter_brimhaven_dungeon", false)) {
                        npc<Neutral>("Most certainly, you have already given me lots of nice coins.")
                        return@option
                    }

                    npc<Neutral>("Most certainly, but I must charge you the sum of 875 coins first.")
                    if (inventory.contains("coins", dungeonEntryFee)) {
                        choice {
                            option("Okay, here's 875 coins.") {
                                inventory.remove("coins", dungeonEntryFee)
                                set("can_enter_brimhaven_dungeon", true)
                                statement("You pay Saniboch 875 coins.")
                                npc<Neutral>("Many thanks. You may now pass the door. May your death be a glorious one!")
                            }
                            option("Never mind.") {
                                player<Neutral>("Never mind.")
                            }
                            option("Why is it worth the entry cost?") {
                                player<Neutral>("Why is it worth the entry cost?")
                                npc<Neutral>("It leads to a huge fearsome dungeon, populated by giants and strange dogs. Adventurers come from all around to explore its depths.")
                                npc<Neutral>("I know not what lies deeper in myself, for my skills in agility and woodcutting are inadequate, but I hear tell of even greater dangers deeper in.")
                            }
                        }
                    } else {
                        player<Neutral>("I don't have the money on me at the moment.")
                        npc<Neutral>("Well this is a dungeon for the more wealthy discerning adventurer. Begone with you, riff raff.")
                        player<Neutral>("But you don't even have clothes, how can you seriously call anyone riff raff.")
                        npc<Neutral>("Hummph.")
                    }
                }

                option<Quiz>("Where does this strange entrance lead?") {
                    npc<Happy>("To a huge fearsome dungeon, populated by giants and strange dogs. Adventurers come from all around to explore its depths.")
                    npc<Neutral>("I know not what lies deeper in myself, for my skills in agility and woodcutting are inadequate.")
                }

                option("Good day to you too.") {
                    player<Neutral>("Good day to you too.")
                }

                option("I'm impressed, that tree is growing on that shed.") {
                    player<Neutral>("I'm impressed, that tree is growing on that shed.")
                    npc<Neutral>("My employer tells me it is an uncommon sort of tree called the Fyburglars tree.")
                }
            }
        }

        npcOperate("Pay", "saniboch") {
            if (get("can_enter_brimhaven_dungeon", false)) {
                npc<Neutral>("You have already given me lots of nice coins, you may go in.")
                return@npcOperate
            }

            val coins = inventory.count("coins")
            if (coins >= dungeonEntryFee) {
                inventory.remove("coins", dungeonEntryFee)
                set("can_enter_brimhaven_dungeon", true)
                statement("You pay Saniboch 875 coins.")
                npc<Neutral>("Many thanks. You may now pass the door. May your death be a glorious one!")
            } else {
                npc<Neutral>("I'll want 875 coins to let you enter.")
                npc<Neutral>("Well this is a dungeon for the more wealthy discerning adventurer. Begone with you, riff raff.")
            }
        }

        objTeleportTakeOff("Enter", "brimhaven_dungeon_entrance") { _, _ ->
            if (!get("can_enter_brimhaven_dungeon", false)) {
                queue("saniboch_door_access_check") {
                    statement("You can't go in there without paying!")
                }
                return@objTeleportTakeOff Teleport.CONTINUE
            }

            // Reset access after one-time use
            set("can_enter_brimhaven_dungeon", false)
            Teleport.CANCEL
        }
    }
}
