package content.skill.summoning.familiar

import content.entity.player.dialogue.Frustrated
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class Bloodrager : Script {
    init {
        npcOperate("Interact", "*_bloodrager_familiar") {
            if (!this["talked_to_bloodrager", false]) {
                this["talked_to_bloodrager"] = true
                npc<Frustrated>("Brother, you are always welcome to talk with me.")
                return@npcOperate
            }
            when (random.nextInt(3)) {
                0 -> {
                    player<Happy>("Are all gorajo as cheery as you?")
                    npc<Frustrated>("Come to the gorajo plane and find out, brother! In the clan fringes, you will find bloodragers, and there are none more welcoming. You would be treated like a sachem.")
                    player<Happy>("I would love to! Are the other gorajo as friendly?")
                    npc<Frustrated>("Their lives are more complicated, brother. They must bear burdens, teach, guide and lead. Although we must protect the clan and serve Challem, we have nothing else to cloud our minds.")
                    player<Happy>("Well, I'll hold you to that invite. If we ever get out of here, of course.")
                }
                1 -> {
                    player<Happy>("How do you like it in Daemonheim?")
                    npc<Frustrated>("It is a place, as any other. I am just happy to be alive, taking sharp rukhs full of air-")
                    player<Happy>("Im not sure I have a rukh.")
                    npc<Frustrated>("Sure you do! Or how else would you grebbit? I am just happy to be alive, breathing the air and completing the task that has been asked of me. Challem be praised.")
                }
                2 -> {
                    player<Happy>("I don't have any more questions.")
                    npc<Frustrated>("Shame. I feel that we are pollen on the same wind, friend.")
                }
            }
        }
    }
}
