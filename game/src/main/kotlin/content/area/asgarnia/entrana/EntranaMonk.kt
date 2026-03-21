package content.area.asgarnia.entrana

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class EntranaMonk : Script {
    init {
        npcOperate("Talk-to", "monk_entrana") { (target) ->
            npc<Neutral>("Greetings traveller.")
            choice {
                option<Quiz>("Can you heal me? I'm injured.") {
                    npc<Neutral>("Ok.")
                    message("You feel a little better.")
                    gfx("heal")
                    areaSound("heal", tile, radius = 10)
                    levels.restore(Skill.Constitution, -levels.getOffset(Skill.Constitution))
                }
                option("Isn't this place built a bit out the way?") {
                    player<Quiz>("Isn't this place built a bit out of the way?")
                    npc<Neutral>("We like it that way actually! We get disturbed less. We still get rather a large amount of travellers looking for sanctuary and healing here as it is!")
                }
            }
        }
    }
}
