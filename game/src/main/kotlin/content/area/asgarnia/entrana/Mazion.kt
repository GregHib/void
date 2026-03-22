package content.area.asgarnia.entrana

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.random

class Mazion : Script {
    init {
        npcOperate("Talk-to", "mazion") {
            when (random.nextInt(3)) {
                0 -> npc<Happy>("Hello $name, fine day today!")
                1 -> npc<Happy>("Nice weather we're having today!")
                else -> npc<Sad>("Please leave me alone, a parrot stole my banana.")
            }
        }
    }
}
