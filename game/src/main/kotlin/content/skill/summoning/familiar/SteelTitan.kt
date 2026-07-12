package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class SteelTitan : Script {
    init {
        npcOperate("Interact", "steel_titan_familiar") {
            when (random.nextInt(5)) {
                0 -> {
                    npc<Neutral>("Forward, master, to a battle that will waken the gods!")
                    player<Happy>("I'd rather not, if it's all the same to you.")
                    npc<Neutral>("I shall never meet my end at this rate...")
                }
                1 -> {
                    npc<Neutral>("How do you wish to meet your end, master?")
                    player<Happy>("Hopefully not for a very long time.")
                    npc<Neutral>("You do not wish to be torn asunder by the thousand limbs of a horde of demons?")
                    player<Happy>("No! I'm quite happy picking flax and turning unstrung bows into gold...")
                }
                2 -> {
                    npc<Neutral>("Why must we dawdle when glory awaits?")
                    player<Happy>("I'm beginning to think you just want me to die horribly...")
                    npc<Neutral>("We could have deaths that bards sing of for a thousand years.")
                    player<Happy>("That's not much compensation.")
                }
                3 -> {
                    npc<Neutral>("Master, we should be marching into glorious battle!")
                    player<Happy>("You know, I think you're onto something.")
                    npc<Neutral>("We could find a death befitting such heroes of RuneScape!")
                    player<Happy>("Ah. You know, I'd prefer not to die...")
                    npc<Neutral>("Beneath the claws of a mighty foe shall I be sent into the embrace of death!")
                }
                4 -> {
                    npc<Neutral>("Let us go forth to battle, my lord!")
                    player<Quiz>("Why do you like fighting so much? It's not very nice to kill things.")
                    npc<Neutral>("It is the most honourable thing in life.")
                    player<Quiz>("But I summoned you, I'm not sure I can even say that you're alive...")
                    npc<Neutral>("Alas, you have discovered the woe of all summoned creatures' existence.")
                    player<Quiz>("Really? I was right?")
                    npc<Neutral>("Oh, woe...woe!")
                }
            }
        }
    }
}
