package content.area.kandarin.fishing_guild

import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.skillcapeMasterDialogue
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class MasterFisher : Script {
    init {
        npcOperate("Talk-to", "master_fisher") {
            player<Quiz>("What is that cape you're wearing?")
            skillcapeMasterDialogue(Skill.Fishing, "a master fisherman")
        }
    }
}
