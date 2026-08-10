package content.area.karamja.brimhaven

import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.skillcapeMasterDialogue
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class CapnIzzy : Script {
    init {
        npcOperate("Talk-to", "capn_izzy_no_beard_brimhaven") {
            player<Quiz>("What is that cape you're wearing?")
            skillcapeMasterDialogue(Skill.Agility, "as agile as possible")
        }
    }
}
