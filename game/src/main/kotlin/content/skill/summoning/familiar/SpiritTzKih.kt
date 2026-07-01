package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

class SpiritTzKih : Script {
    init {
        npcOperate("Interact", "spirit_tz-kih_familiar") {
            if (inventory.items.any { it.id.startsWith("prayer_potion") }) {
                npc<Neutral>("You drink pray, me drink pray.")
                player<Happy>("What's that, Tz-Kih?")
                npc<Neutral>("You got pray pray pot. Tz-Kih drink pray pray you, you drink pray pray pot.")
                player<Happy>("You want to drink my Prayer points?")
                npc<Neutral>("Yes. Pray pray.")
                player<Happy>("Err, not right now, Tz-Kih. I, er, need them myself.")
                player<Happy>("Sorry.")
                npc<Neutral>("But, pray praaaay...?")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("How's it going, Tz-kih?")
                    player<Happy>("Pray pray?")
                    npc<Neutral>("Don't start with all that again.")
                    player<Happy>("Hmph, silly JalYt.")
                }
                1 -> {
                    npc<Neutral>("Does JalYt think Tz-kih as strong as Jad Jad?")
                    player<Happy>("Are you as strong as TzTok-Jad? Yeah, sure, why not.")
                    npc<Neutral>("Really? Thanks, JalYt. Tz-Kih strong and happy.")
                }
                2 -> {
                    npc<Neutral>("Have you heard of blood bat, JalYt?")
                    player<Happy>("Blood bats? You mean vampire bats?")
                    npc<Neutral>("Yes. Blood bat.")
                    player<Happy>("Yes, I've heard of them. What about them?")
                    npc<Neutral>("Tz-Kih like blood bat, but drink pray pray not blood blood. Blood blood is yuck.")
                    player<Happy>("Thanks, Tz-Kih, that's nice to know.")
                }
                3 -> {
                    npc<Neutral>("Pray pray pray pray pray pray pray pray!")
                    player<Happy>("Calm down, Tz-Kih, we'll find you something to drink soon.")
                    npc<Neutral>("Pray praaaaaaaaaaaaaay!")
                    player<Happy>("Okay, okay. Calm down!")
                }
            }
        }
    }
}
