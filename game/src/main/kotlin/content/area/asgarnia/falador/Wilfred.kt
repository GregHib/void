package content.area.asgarnia.falador

import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.skillcapeMasterDialogue
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class Wilfred : Script {
    init {
        npcOperate("Talk-to", "wilfred_falador") {
            player<Quiz>("What is that cape you're wearing?")
            skillcapeMasterDialogue(Skill.Woodcutting, "a master woodsman")
        }
    }
}
