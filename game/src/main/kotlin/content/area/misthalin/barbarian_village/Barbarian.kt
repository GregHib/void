package content.area.misthalin.barbarian_village

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class Barbarian : Script {
    init {
        npcOperate("Talk-to", "barbarian_barbarian_village*") {
            when (random.nextInt(5)) {
                0 -> npc<Quiz>("Wanna fight?")
                1 -> npc<Quiz>("Ah, you come for fight, ja?")
                2 -> npc<Angry>("You look funny.")
                3 -> npc<Angry>("Grrr!")
                4 -> npc<Quiz>("What you want?")
                else -> npc<Angry>("Go Away!")
            }
        }
    }
}