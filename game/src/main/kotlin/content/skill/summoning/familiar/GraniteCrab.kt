package content.skill.summoning.familiar

import content.entity.player.dialogue.Frustrated
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

class GraniteCrab : Script {
    init {
        npcOperate("Interact", "granite_crab_familiar") {
            if (inventory.items.any { it.id.startsWith("raw_") || it.id.startsWith("leaping_") }) {
                npc<Neutral>("Can I have some fish?")
                player<Happy>("No, I have to cook these for later.")
                npc<Neutral>("Free fish, please?")
                player<Happy>("No...I already told you you can't.")
                npc<Neutral>("Can it be fish time soon?")
                player<Frustrated>("Great...I get stuck with the only granite crab in existence that can't take no for an answer...")
                return@npcOperate
            }
            when (random.nextInt(3)) {
                0 -> {
                    npc<Neutral>("Rock fish now, please?")
                    player<Happy>("Not right now. I don't have any rock fish.")
                }
                1 -> {
                    npc<Neutral>("When can we go fishing? I want rock fish.")
                    player<Happy>("When I need some fish. It's not that hard to work out, right?")
                }
                2 -> {
                    npc<Neutral>("I'm stealthy!")
                    player<Happy>("Errr... of course you are.")
                }
            }
        }
    }
}
