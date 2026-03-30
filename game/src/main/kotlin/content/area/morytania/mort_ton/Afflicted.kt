package content.area.morytania.mort_ton

import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class Afflicted : Script {
    init {
        npcOperate("Talk-to", "afflicted*") {
            val words = setOf("ughugh", "knows'is", "nots", "pirsl", "wot's", "zurgle", "gurghl", "mee's", "seysyi", "sfriess", "says")
            npc<Shock>(
                buildString {
                    repeat(random.nextInt(1, 7)) {
                        append(words.random())
                        append(" ")
                    }
                },
            )
            npc<Shock>("<blue>~ This person doesn't make any sense at all. ~")
        }
    }
}
