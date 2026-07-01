package content.skill.summoning.familiar

import content.entity.player.dialogue.Frustrated
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.random

class Hoardstalker : Script {
    init {
        npcOperate("Interact", "*_hoardstalker_familiar") {
            if (!this["talked_to_hoardstalker", false]) {
                this["talked_to_hoardstalker"] = true
                npc<Frustrated>("Are you sure we can stop, naabe? Aren't there creatures about?")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    player<Happy>("You're a little timid for a gorajo, aren't you?")
                    npc<Frustrated>("Naabe, a hoardstalker has little or no experience of combat. I am told that I fight like a cat on hind legs.")
                    choice("Choose an option:") {
                        option<Happy>("Don't worry, I have your back.") {
                            npc<Frustrated>("Not just my back, I hope. I find a foe more terrifying when they run at me from the front. Although we do not die in our spirit form, we must rest for a week due to our spirit wounds, so others would have to perform my role for me.")
                            npc<Frustrated>("They would be like a woodpecket attempting to feed from a tortoise. I'd come back to find the crops irrigated with milk, weapons dipped in water, and babies drinking poison. Churra, it is too terrible to think on.")
                        }
                        option<Happy>("Then what use are you?") {
                            npc<Frustrated>("Churra! You have a closed mind on those shoulders. I may not be a towering bloodrager or a graceful deathslinger, but I can be useful where they cannot")
                            npc<Frustrated>("Let me scavenge in these ruins, naabe. I will bring you such items that you would never question my place here.")
                        }
                    }
                }
                1 -> {
                    player<Happy>("Do you like it in Daemonheim? I can't imagine how anyone could.")
                    npc<Frustrated>("I see this place in a way that you do not, naabe. It amazes me how something can go down so deep, yet still be strong and broad. I cannot help but applaud the mind behind these dungeons.")
                    npc<Frustrated>("The workmanship, too...it makes me want to put down my tools and reincarnate as a bloodrager. I feel like a sparrowhawk who has been chased off his kill by a dragon.")
                    player<Happy>("But this place is evil, and thousands died building it. I doubt you've murdered anyone to make a dagger, you know.")
                    npc<Frustrated>("And that is some comfort. I feel I must be careful about what I learn and study on this plane. There are poisoned thorns among the flowering wonders")
                }
                2 -> {
                    player<Happy>("Why are you called a hoardstalker? It seems a strange choice for a...blacksmith and scavenger, I guess.")
                    npc<Frustrated>("We are not just required to make the tools of our clansmen, naabe, We must protect the tools from those who would take them.")
                    player<Happy>("Still, hoardstalker is a silly name.")
                    npc<Frustrated>("Naabe, I have held this back from you until now, but the term $name, in our tongue, means 'One-Who-Juggles-Piglets'. A less-mature gorajo would find that amusing.")
                }
                3 -> {
                    player<Happy>("I don't have any more questions.")
                    npc<Frustrated>("No problem, naabe. Just make sure nothing sneaks past and hurts me.")
                }
            }
        }
    }
}
