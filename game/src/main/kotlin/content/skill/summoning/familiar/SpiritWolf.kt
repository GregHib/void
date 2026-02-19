package content.skill.summoning.familiar

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

class SpiritWolf : Script {
    init {
        npcOperate("Interact", "spirit_wolf_familiar") {
            if (inventory.items.any { it.isNotEmpty() && it.id.endsWith("bones") }) {
                npc<Happy>("Whuff-whuff! Arf!<br>(Throw the bone! I want to chase!)")
                player<Laugh>("I can't just throw bones away - I need them to train my Prayer!")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Quiz>("Whurf?<br>(What are you doing?)")
                    player<Neutral>("Oh, just some...biped thing. I'm sure it would bored you.")
                }
                1 -> {
                    npc<Angry>("Bark! Bark!<br>(Danger!)")
                    player<Shock>("Where?")
                    npc<Sad>("Whiiiine...<br>(False alarm...)")
                }
                2 -> {
                    npc<Angry>("Pant pant whine?<br>(When am I going to get to chase something?)")
                    player<Laugh>("Oh, I'm sure we'll find something for you in a bit.")
                }
                3 -> {
                    npc<Happy>("Whuff whuff. Pantpant awf!<br>(I smell something good. Hunting time!)")
                    player<Neutral>("We can go hunting in a moment. I just have to take care of something first.")
                }
            }
        }
    }
}
