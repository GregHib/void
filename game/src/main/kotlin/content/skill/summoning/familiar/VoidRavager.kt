package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class VoidRavager : Script {
    init {
        npcOperate("Interact", "void_ravager_familiar") {
            when (random.nextInt(3)) {
                0 -> {
                    npc<Neutral>("You look delicious!")
                    player<Happy>("Don't make me dismiss you!")
                }
                1 -> {
                    npc<Neutral>("Take me to the rift!")
                    player<Happy>("I'm not taking you there! Goodness knows what you'd get up to.")
                    npc<Neutral>("I promise not to destroy your world...")
                    player<Happy>("If only I could believe you...")
                }
                2 -> {
                    npc<Neutral>("How do you bear life without ravaging?")
                    player<Happy>("It's not always easy.")
                    npc<Neutral>("I could show you how to ravage, if you like...")
                }
            }
        }
    }
}
