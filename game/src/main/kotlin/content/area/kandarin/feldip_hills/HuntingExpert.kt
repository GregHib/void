package content.area.kandarin.feldip_hills

import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.skillcapeMasterDialogue
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class HuntingExpert : Script {
    init {
        npcOperate("Talk-to", "hunting_expert_feldip_hills") {
            player<Quiz>("What is that cape you're wearing?")
            skillcapeMasterDialogue(Skill.Hunter, "a master hunter")
        }
    }
}
