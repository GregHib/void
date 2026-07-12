package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.summoning.follower
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.random

class VoidShifter : Script {
    init {
        // Emergency shift - once the owner's life points drop below 10% the shifter yanks them to the
        // safety of the Void Knights' Outpost (the same courtyard its Call to Arms teleports to).
        levelChanged(Skill.Constitution) { skill, _, to ->
            if (follower?.id == "void_shifter_familiar" && to > 0 && to < levels.getMax(skill) / 10) {
                tele(2659, 2658, 0)
                message("Your void shifter teleports you to safety.")
            }
        }

        npcOperate("Interact", "void_shifter_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("What a splendid day, sir!")
                    player<Happy>("Yes, it is!")
                    npc<Neutral>("It could only be marginally improved, perhaps, by tea and biscuits.")
                    player<Happy>("What a marvellous idea!")
                }
                1 -> {
                    npc<Neutral>("I'm sorry to bother you, but could you assist me briefly?")
                    player<Happy>("I suppose so.")
                    npc<Neutral>("I was wondering, briefly, if perchance you might care to dance?")
                    player<Happy>("Dance? With a pest?")
                    npc<Neutral>("Well, you see, I'm dreadfully out of practice and now I can barely leap, let alone teleport.")
                    player<Happy>("I'm not going to help you remember how to destroy the world!")
                    npc<Neutral>("What a beastly world we live in where one gentleman/lady will not aid a pest in need...")
                }
                2 -> {
                    npc<Neutral>("How do you do?")
                    player<Happy>("Okay, I suppose.")
                    npc<Neutral>("Marvellous, simply marvellous!")
                }
                3 -> {
                    npc<Neutral>("Lets go and see to those cads and bounders!")
                    player<Happy>("Which 'cads and bounders' did you mean, exactly?")
                    npc<Neutral>("Why, the ones with no honour, of course.")
                    player<Happy>("I don't think he knows what pests do...")
                }
            }
        }
    }
}
