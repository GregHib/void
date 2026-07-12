package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class Macaw : Script {
    init {
        npcOperate("Interact", "macaw_familiar") {
            when (random.nextInt(3)) {
                0 -> {
                    npc<Neutral>("Awk! Gimme the rum! Gimme the rum!")
                    player<Happy>("I don't think you'll like the stuff. Besides, I think there is a law about feeding birds alcohol.")
                }
                1 -> {
                    npc<Neutral>("Awk! I'm a pirate! Awk! Yo, ho ho!")
                    player<Happy>("I'd best not keep you around any customs officers!")
                }
                2 -> {
                    npc<Neutral>("Awk! Caw! Shiver me timbers!")
                    player<Happy>("I wonder where you picked up all these phrases?")
                }
            }
        }
    }
}
