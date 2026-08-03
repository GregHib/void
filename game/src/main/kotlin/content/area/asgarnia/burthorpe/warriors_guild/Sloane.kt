package content.area.asgarnia.burthorpe.warriors_guild

import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.skillcapeMasterDialogue
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class Sloane : Script {
    init {
        npcOperate("Talk-to", "sloane,sloane_falador") {
            player<Quiz>("What is that cape you're wearing?")
            skillcapeMasterDialogue(Skill.Strength, "as strong as is possible")
        }
    }
}
