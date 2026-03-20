package content.area.fremennik_province.rellekka

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Erjolf : Script {
    init {
        npcOperate("Talk-to", "erjolf") {
            npc<Happy>("Incredible! This'll show them!")
            player<Quiz>("Hello?")
            npc<Happy>("Oh! Hello there.")
            player<Quiz>("You seem rather excited about something.")
            npc<Shifty>("Who, me? No, I'm not excited about anything. I certainly haven't found anything amazing inside the caves here.")
            player<Quiz>("You won't mind if I have a look inside the cave, then.")
            npc<Angry>("Just you hold on a minute. I know what you adventurers are like, you'll wander in there and decide that anything you find is yours to take.")
            // TODO Tale of the Muspah
        }
    }
}
