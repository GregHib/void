package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

class GiantChinchompa : Script {
    init {
        npcOperate("Interact", "giant_chinchompa_familiar") {
            if (inventory.items.any { it.id.contains("chinchompa") }) {
                npc<Neutral>("Woah, woah, woah - hold up there.")
                player<Happy>("What is it, ratty?")
                npc<Neutral>("You got something in your backpack that you'd like to tell me about?")
                player<Happy>("I was wondering when you were going to bring up the chinchompa. I'm sure they like it in my inventory.")
                npc<Neutral>("Did they not teach you anything in school? Chinchompas die in hot bags. You know what happens when chinchompas die. Are you attached to your back?")
                player<Happy>("Medically, yes. And I kind of like it too. I get the point.")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Half a pound of tuppenny rice, half a pound of treacle...")
                    player<Happy>("I hate it when you sing that song.")
                    npc<Neutral>("...that's the way the money goes...")
                    player<Happy>("Couldn't you sing 'Kumbaya' or something?")
                    npc<Neutral>("...BANG, goes the chinchompa!")
                    player<Happy>("Sheesh.")
                }
                1 -> {
                    npc<Neutral>("What's small, brown and blows up?")
                    player<Happy>("A brown balloon?")
                    npc<Neutral>("A chinchompa! Pull my finger.")
                    player<Happy>("I'm not pulling your finger.")
                    npc<Neutral>("Nothing will happen. Truuuuust meeeeee.")
                    player<Happy>("Oh, go away.")
                }
                2 -> {
                    npc<Neutral>("I spy, with my little eye, something beginning with 'B'.")
                    player<Happy>("Bomb? Bang? Boom? Blowing-up-little-chipmunk?")
                    npc<Neutral>("No. Body odour. You should wash a bit more.")
                    player<Happy>("Well, that was pleasant. You don't smell all that great either, you know.")
                    npc<Neutral>("Stop talking, stop talking! Your breath stinks!")
                    player<Happy>("We're never going to get on, are we?")
                }
                3 -> {
                    npc<Neutral>("I seem to have found a paper bag.")
                    player<Happy>("Well done. Anything in it?")
                    npc<Neutral>("Hmmm. Let me see. It seems to be full of some highly sought after, very expensive...chinchompa breath!")
                    player<Happy>("No, don't pop it!")
                    statement("*BANG!!*")
                    player<Happy>("You just cannot help yourself, can you?")
                }
            }
        }
    }
}
