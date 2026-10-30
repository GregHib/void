package content.area.misthalin.varrock.blue_moon_inn

import content.entity.player.bank.bank
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

// Source: https://runescape.wiki/w/Transcript:Dr_Harlow?oldid=36227375
class DrHarlow : Script {
    init {
        npcOperate("Talk-to", "dr_harlow") { talk() }
    }

    private suspend fun Player.talk() {
        if (harlowVampireSlayer()) {
            return
        }
        npc<Drunk>("Buy me a drrink pleassh.")
        choice {
            option<Neutral>("No, you've had enough. ") {
                npc<Drunk>("Pssssh, I never have enough!")
            }
            option("Okay, here you go. ") {
                if (inventory.contains("beer")) {
                    player<Happy>("Okay, here you go.")
                    inventory.remove("beer")
                    statement("You give a beer to Dr Harlow.")
                    npc<Drunk>("Cheersh, matey.")
                } else {
                    player<Neutral>("I'll just go and buy one.")
                }
            }
        }
    }

    private suspend fun Player.harlowVampireSlayer(): Boolean {
        when (quest("vampire_slayer")) {
            "started" -> {
                player<Neutral>("Morgan sent me. Count Draynor is terrorising his village.")
                npc<Neutral>("A vampyre, eh? Bring me a beer and I'll tell you how to deal with him.")
                if (!inventory.contains("beer")) {
                    return true
                }
                // Require room for every missing tool before taking the player's payment.
                val tools = missingVampireTools()
                if (inventory.spaces + 1 < tools.size) {
                    npc<Neutral>("Make some room in your inventory for a stake and hammer first.")
                    return true
                }
                inventory.remove("beer")
                tools.forEach { inventory.add(it) }
                set("vampire_slayer", "armed")
                statement("You give Dr Harlow a beer. He gives you the tools you need.")
                npc<Neutral>("When he's weakened, drive a wooden stake through his heart with a hammer. Keep the stake and hammer with you.")
                npc<Neutral>("Garlic will weaken him too. Look in the drawers upstairs in Morgan's house.")
            }
            "armed" -> {
                val tools = missingVampireTools()
                if (inventory.spaces < tools.size) {
                    npc<Neutral>("Make room in your inventory and I'll replace your missing tools.")
                } else {
                    tools.forEach { inventory.add(it) }
                    if (tools.isNotEmpty()) {
                        statement("Dr Harlow replaces your missing tools.")
                    }
                    npc<Neutral>("Take your stake and hammer to the cellar of Draynor Manor. Garlic will help weaken the Count.")
                }
            }
            else -> return false
        }
        return true
    }

    private fun Player.missingVampireTools(): List<String> = listOf("stake", "stake_hammer").filterNot { carriesItem(it) || bank.contains(it) }
}
