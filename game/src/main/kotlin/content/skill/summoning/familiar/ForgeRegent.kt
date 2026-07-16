package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class ForgeRegent : Script {
    init {
        npcOperate("Interact", "forge_regent_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Crackley spit crack sizzle? (Can we go Smithing?)")
                    player<Happy>("Maybe.")
                    npc<Neutral>("Hiss? (Can we go smelt something?)")
                    player<Happy>("Maybe.")
                    npc<Neutral>("Flicker crackle sizzle? (Can we go mine something to smelt?)")
                    player<Happy>("Maybe.")
                    npc<Neutral>("Sizzle flicker! (Yay! I like doing that!)")
                    player<Happy>("...")
                }
                1 -> {
                    npc<Neutral>("Hiss. (I'm happy.)")
                    player<Happy>("Good.")
                    npc<Neutral>("Crackle. (Now I'm sad.)")
                    player<Happy>("Oh dear, why?")
                    npc<Neutral>("Hiss-hiss. (Happy again.)")
                    player<Happy>("Glad to hear it.")
                    npc<Neutral>("Crackley-crick. (Sad now.)")
                    player<Happy>("Um.")
                    npc<Neutral>("Hiss. (Happy.)")
                    player<Happy>("Right...")
                    npc<Neutral>("Crackle. (Sad.)")
                    player<Happy>("You're very strange.")
                    npc<Neutral>("Sizzle hiss? (What makes you say that?)")
                    player<Happy>("Oh...nothing in particular.")
                }
                2 -> {
                    npc<Neutral>("Sizzle! (I like logs.)")
                    player<Happy>("They are useful for making planks.")
                    npc<Neutral>("Sizzley crack hiss spit. (No, I just like walking on them. They burst into flames.)")
                    player<Happy>("It's a good job I can use you as a firelighter really!")
                }
                3 -> {
                    npc<Neutral>("Sizzle... (I'm bored.)")
                    player<Happy>("Are you not enjoying what we're doing?")
                    npc<Neutral>("Crackley crickle sizzle. (Oh yes, but I'm still bored.)")
                    player<Happy>("Oh, I see.")
                    npc<Neutral>("Sizzle hiss? (What's that over there?)")
                    player<Happy>("I don't know. Should we go and look?")
                    npc<Neutral>("Hiss crackle spit sizzle crack? (Nah, that's old news - I'm bored of it now.)")
                    npc<Neutral>("Sizzle hiss? (What's that over there?)")
                    player<Happy>("But...wha...where now?")
                    npc<Neutral>("Sizzle crack crickle. (Oh no matter, it no longer interests me.)")
                    player<Happy>("You're hard work.")
                }
            }
        }
    }
}
