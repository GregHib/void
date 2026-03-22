package content.area.misthalin.zanaris

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.random

class Blaec : Script {
    init {
        npcOperate("Talk-to", "blaec") {
            when (random.nextInt(2)) {
                0 -> npc<Happy>("Wunnerful weather we're having today!")
                1 -> npc<Sad>("Please leave me alone, I'm busy trapping the pygmy shrews.")
                else -> npc<Neutral>("Greetin's $name, fine day today!")
            }
        }
    }
}
