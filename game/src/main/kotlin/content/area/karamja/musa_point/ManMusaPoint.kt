package content.area.karamja.musa_point

import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class ManMusaPoint : Script {
    init {
        npcOperate("Talk-to", "man_musa_point") {
            npc<Sad>(
                listOf(
                    "I can't take the heat!",
                    "Snakes! Snakes! Ohhhh, there are snakes!",
                    "Too many monkeys - I can't stand it!",
                    "Why don't I have a 'Teleport Home to Lumbridge' button like everybody else?",
                    "So... many... trees...",
                    "I should have stayed in Port Sarim.",
                    "Molten lava! MOLTEN LAVA!",
                ).random(),
            )
        }
    }
}
