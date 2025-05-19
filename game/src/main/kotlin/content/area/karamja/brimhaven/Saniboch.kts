package content.area.karamja.brimhaven

import content.entity.obj.objTeleportTakeOff
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.queue

val DUNGEON_ENTRY_FEE = 875

// Saniboch "Talk-to" option
npcOperate("Talk-to", "saniboch") {
    npc<Talk>("Good day to you, Bwana.")

    choice {
        option("Can I go through that door please?") {
            player<Talk>("Can I go through that door please?")
            if (player["temporary_saniboch_access", false]) {
                npc<Talk>("You have already given me lovely coins, so you may go in, and may your death be a glorious one!")
            } else {
                npc<Talk>("Most certainly, but I must charge you the sum of 875 coins first.")
                val coins = player.inventory.count("coins")
                if (coins >= DUNGEON_ENTRY_FEE) {
                    choice {
                        option("Okay, here's 875 coins.") {
                            player.inventory.remove("coins", DUNGEON_ENTRY_FEE)
                            player["temporary_saniboch_access"] = true
                            npc<Talk>("Many thanks. You may now pass the door. May your death be a glorious one!")
                        }
                        option("Never mind.") {
                            player<Talk>("Never mind.")
                        }
                    }
                } else {
                    player<Talk>("I haven't got that much money with me.")
                    npc<Talk>("Begone with you, riff raff.")
                }
            }
        }

        option("Where does this strange entrance lead?") {
            player<Talk>("Where does this strange entrance lead?")
            npc<Talk>("To a huge fearsome dungeon, populated by giants and strange dogs. Adventurers come from all around to explore its depths.")
        }

        option("Good day to you too.") {
            player<Talk>("Good day to you too.")
        }
    }
}

// Saniboch "Pay" right-click option
npcOperate("Pay", "saniboch") {
    val coins = player.inventory.count("coins")
    if (player["temporary_saniboch_access", false]) {
        npc<Talk>("You may go in, you already paid.")
    } else if (coins >= DUNGEON_ENTRY_FEE) {
        player.inventory.remove("coins", DUNGEON_ENTRY_FEE)
        player["temporary_saniboch_access"] = true
        npc<Talk>("Many thanks. You may now pass the door. May your death be a glorious one!")
    } else {
        npc<Talk>("I'll want 875 coins to let you enter. Begone with you, riff raff.")
    }
}

// Door object teleport logic to enter dungeon
objTeleportTakeOff("Enter", "brimhaven_dungeon_entrance") {
    if (!player["temporary_saniboch_access", false]) {
        cancel() // Cancel teleport before queueing dialogue
        player.queue("saniboch_door_access_check") {
            statement("I should speak to Saniboch first.")
        }
        return@objTeleportTakeOff
    }
}

// Door object teleport logic to exit dungeon - resets access
objTeleportTakeOff("Leave", "brimhaven_dungeon_exit") {
    player["temporary_saniboch_access"] = false
}