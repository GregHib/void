package content.area.misthalin.varrock.museum

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Marfet : Script {
    init {
        npcOperate("Talk-to", "marfet") {
            npc<Happy>("Hello!")
            player<Neutral>("Hi, have you seen the Dig Site displays?")
            npc<Happy>("Yes, good aren't they! Have you done the quest?")
            player<Happy>("Yes, I did it all.")
            npc<Neutral>("Did you know after the quest you can get the teddy bear back by stealing it from the female student?")
            player<Happy>("I shall have to try that sometime.")
        }
    }
}
