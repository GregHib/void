package content.area.asgarnia.port_sarim

import content.entity.player.dialogue.Cry
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script

class Felkrash : Script {

    init {
        npcOperate("Talk-to", "felkrash_rat_pits") {
            player<Quiz>("What was it I heard about some pits around here?")
            statement("- Felkrash breaks down into tears. -")
            npc<Sad>("It is... It used to be my rat pits. You could bring your cats here to fight. It was going to be glorious!")
            npc<Cry>("It was open for years. Not a single person visited!")
            npc<Sad>("How could so many be so foolish as to ignore such brilliance?")
            player<Laugh>("I guess brilliance is one word for it.")
            statement("- Felkrash glares at you angrily. You walk away while you can.- ")
        }
    }
}
