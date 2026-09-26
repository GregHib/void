package content.area.misthalin.varrock.blue_moon_inn

import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

// Source: https://runescape.wiki/w/Transcript:Dr_Harlow?oldid=36227375
class DrHarlow : Script {
    init {
        npcOperate("Talk-to", "dr_harlow") {
            npc<Drunk>("Buy me a drrink pleassh.")
            choice {
                option<Neutral>("No, you've had enough. ") {
                    npc<Drunk>("Pssssh, I never have enough!")
                }
                option("Okay, here you go. ") {
                    if(inventory.contains("beer")) {
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
    }
}