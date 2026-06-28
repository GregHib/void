package content.skill.summoning.familiar

import content.entity.player.dialogue.Frustrated
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.random

class ArcticBear : Script {
    init {
        npcOperate("Interact", "arctic_bear_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Crikey! We’re tracking ourselves a real live one here. I call ’em ‘Brighteyes’.")
                    player<Frustrated>("Will you stop stalking me like that?")
                    npc<Neutral>("Lookit that! Something’s riled this one up good and proper.")
                    player<Happy>("Who are you talking to anyway?")
                    npc<Neutral>("Looks like I’ve been spotted.")
                    player<Happy>("Did you think you didn’t stand out here or something?")
                }
                1 -> {
                    npc<Neutral>("Crikey! Something seems to have startled Brighteyes, here.")
                    player<Happy>("What? What’s happening?")
                    npc<Neutral>("Maybe he's scented a rival.")
                    player<Happy>("I smell something, but it’s not a rival.")
                }
                2 -> {
                    npc<Neutral>("We’re tracking Brighteyes here as he goes about his daily routine.")
                    player<Happy>("My name is $name, not Brighteyes!")
                    npc<Neutral>("Looks like the little critter’s upset about something.")
                    player<Happy>("I wonder if he’d be quiet if I just did really boring stuff.")
                }
                3 -> {
                    npc<Neutral>("These little guys get riled up real easy.")
                    player<Happy>("Who wouldn’t be upset with a huge bear tracking along behind them, commenting on everything they do?")
                }
            }
        }
    }
}
