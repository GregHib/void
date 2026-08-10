package content.area.asgarnia.falador

import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.skillcapeMasterDialogue
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class EstateAgent : Script {
    init {
        // TODO house purchase, redecoration and relocation dialogue when construction lands
        npcOperate("Talk-to", "estate_agent*") {
            player<Quiz>("What's that cape you are wearing?")
            skillcapeMasterDialogue(Skill.Construction, "a master home builder")
        }
    }
}
