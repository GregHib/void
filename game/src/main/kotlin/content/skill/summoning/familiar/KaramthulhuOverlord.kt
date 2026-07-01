package content.skill.summoning.familiar

import content.entity.player.dialogue.Frustrated
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class KaramthulhuOverlord : Script {
    init {
        npcOperate("Interact", "karamthulhu_overlord_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    player<Happy>("Do you want-")
                    npc<Neutral>("Silence!")
                    player<Frustrated>("But I only...")
                    npc<Neutral>("Silence!")
                    player<Frustrated>("Now, listen here...")
                    npc<Neutral>("SIIIIIILLLLLEEEEENCE!")
                    player<Frustrated>("Fine!")
                    npc<Neutral>("Good!")
                    player<Frustrated>("Maybe I'll be so silent you'll think I never existed")
                    npc<Neutral>("Oh, how I long for that day...")
                }
                1 -> {
                    npc<Neutral>("Kneel before my awesome might!")
                    player<Happy>("I would, but I have a bad knee you see...")
                    npc<Neutral>("Your feeble prattlings matter not, air-breather! Kneel or face my wrath!")
                    player<Happy>("I'm not afraid of you. You're only a squid in a bowl!")
                    npc<Neutral>("Only? I, radiant in my awesomeness, am 'only' a squid in a bowl? Clearly you need to be shown in your place, lung-user!")
                    statement("*The Karamthulhu overlord narrows its eye and you find yourself unable to breathe!")
                    player<Scared>("Gaak! Wheeeze!")
                    npc<Neutral>("Who rules?")
                    player<Scared>("You rule!")
                    npc<Neutral>("And don't forget it!")
                }
                2 -> {
                    player<Happy>("...")
                    npc<Neutral>("The answer 'be silent'!")
                    player<Happy>("You have no idea what I was going to ask you.")
                    npc<Neutral>("Yes I do; I know all!")
                    player<Happy>("Then you will not be surprised to know I was going to ask you what you wanted to do today.")
                    npc<Neutral>("You dare doubt me!")
                    npc<Neutral>("The answer 'be silent' because your puny compressed brain could not even begin to comprehend my needs!")
                    player<Happy>("Well, how about I dismiss you so you can go and do what you like?")
                    npc<Neutral>("Well, how about I topple your nations into the ocean and dance my tentacle-waving victory dance upon your watery graves?")
                    player<Happy>("Yeah...well...")
                    npc<Neutral>("Silence! Your burbling vexes me greatly!")
                }
                3 -> {
                    player<Happy>("Errr...Have you calmed down yet?")
                    npc<Neutral>("Calmed down? Why would I need to calm down?")
                    player<Happy>("Well there is that whole 'god complex' thing...")
                    npc<Neutral>("Complex? What 'complex' are you drooling about this time, minion?")
                    npc<Neutral>("It is a sad thing indeed when a god as powerful as I cannot gain recognition from the foolish mewling sheep of this 'surface' place.")
                    player<Shifty>("I don't really think sheep really make mewling noises...")
                    npc<Neutral>("Silence!")
                }
            }
        }
    }
}
