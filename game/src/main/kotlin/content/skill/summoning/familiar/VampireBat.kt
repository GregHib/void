package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.random

class VampireBat : Script {
    init {
        npcOperate("Interact", "vampire_bat_familiar") {
            if (levels.get(Skill.Constitution) < levels.getMax(Skill.Constitution) / 2) {
                npc<Neutral>("You're vasting all that blood, can I have some?")
                player<Happy>("No!")
                return@npcOperate
            }
            when (random.nextInt(3)) {
                0 -> {
                    npc<Neutral>("Ven are you going to feed me?")
                    player<Happy>("Well for a start, I'm not giving you any of my blood.")
                }
                1 -> {
                    npc<Neutral>("I want to eat something.")
                    player<Happy>("I'm sure you do; let's go see what we can find.")
                }
                2 -> {
                    npc<Neutral>("Ven can I eat something?")
                    player<Happy>("Just as soon as I find something to attack.")
                }
            }
        }
    }
}
