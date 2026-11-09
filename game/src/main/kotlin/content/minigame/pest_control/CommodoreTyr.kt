package content.minigame.pest_control

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.name

class CommodoreTyr : Script {
    init {
        // https://runescape.wiki/w/Transcript:A_Void_Dance#Post-Quest_Dialogue
        npcOperate("Talk-to", "commodore_tyr_pest_control") {
            if (!questCompleted("void_dance")) {
                return@npcOperate
            }
            npc<Neutral>("Thank you once again for your services, $name. We shall work with the Temple Knights to find out what happened to the cubes that already shipped, and keep you informed.")
        }

        // https://runescape.wiki/w/Transcript:The_Void_Stares_Back#Post-quest
        npcOperate("Talk-to", "commodore_tyr_vallutas_den_2") {
            npc<Neutral>("We should leave this place. If you have questions for the Valluta, now's the time to ask them. I plan to have the portal Grayzag opened disrupted as soon as we return.")
            npc<Neutral>("Any other business you have with me can wait until after we're back at the outpost.")
        }
    }
}
