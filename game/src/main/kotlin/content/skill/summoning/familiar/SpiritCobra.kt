package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

class SpiritCobra : Script {
    init {
        npcOperate("Interact", "spirit_cobra_familiar") {
            if (equipped(EquipSlot.Ring).id in setOf("ring_of_charos", "ring_of_charos_a", "ring_of_charos_ai")) {
                npc<Neutral>("You are under my power!")
                player<Happy>("No, you are under my power!")
                npc<Neutral>("No, you are under my power!")
                player<Happy>("No, my power is greater!")
                npc<Neutral>("Your power is the greater...")
                player<Happy>("Your powers are no match for mine!")
                npc<Neutral>("You are convinced you have won this argument...")
                player<Happy>("I won the argument...yay!")
                npc<Neutral>("*Manic serpentine laughter*")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Do we have to do thissss right now?")
                    player<Happy>("Yes, I'm afraid so.")
                    npc<Neutral>("You are under my sssspell...")
                    player<Happy>("I will do as you ask...")
                    npc<Neutral>("Do we have to do thissss right now?")
                    player<Happy>("Not at all, I had just finished!")
                }
                1 -> {
                    npc<Neutral>("You are feeling ssssleepy...")
                    player<Happy>("I am feeling sssso ssssleepy...")
                    npc<Neutral>("You will bring me lotssss of sssstuff!")
                    player<Happy>("What ssssort of sssstuff?")
                    npc<Neutral>("What ssssort of sssstuff have you got?")
                    player<Happy>("All kindsss of sssstuff.")
                    npc<Neutral>("Then just keep bringing sssstuff until I'm ssssatissssfied!")
                }
                2 -> {
                    npc<Neutral>("I'm bored, do ssssomething to entertain me...")
                    player<Happy>("Errr, I'm not here to entertain you, you know.")
                    npc<Neutral>("You will do as I assssk...")
                    player<Happy>("Your will is my command...")
                    npc<Neutral>("I'm bored, do ssssomething to entertain me...")
                    player<Happy>("I'll dance for you!")
                }
                3 -> {
                    npc<Neutral>("I am king of the world!")
                    player<Happy>("You know, I think there is a law against snakes being the king.")
                    npc<Neutral>("My will is your command...")
                    player<Happy>("I am yours to command...")
                    npc<Neutral>("I am king of the world!")
                    player<Happy>("All hail King Serpentor!")
                }
            }
        }
    }
}
