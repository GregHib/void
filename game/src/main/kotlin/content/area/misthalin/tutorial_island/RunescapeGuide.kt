package content.area.misthalin.tutorial_island

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.Settings

class RunescapeGuide : Script {

    init {
        npcOperate("Talk-to", "runescape_guide") {
            when (tutorialStage) {
                0 -> {
                    npc<Happy>("Greetings! I see you are a new arrival to this land. My job is to teach you a few basic skills and functions.")
                    npc<Neutral>("First we shall go through some of the game's control panels, which you can find at the bottom right of your screen.")
                    npc<Neutral>("Click on the flashing spanner icon to open your game options.")
                    advanceTutorial(0)
                }
                2 -> {
                    npc<Neutral>("The options panel lets you change the screen brightness, the volume of the music and sound effects, and whether other players may offer you help.")
                    npc<Happy>("That's all I have to teach you. Go through that door and the Survival Expert will show you how to look after yourself.")
                    player<Neutral>("Thanks, I'll do that.")
                    advanceTutorial(2)
                }
                else -> {
                    npc<Neutral>("You've learnt all I have to teach. Follow the arrow to your next instructor.")
                    npc<Neutral>("Welcome to ${Settings["server.name"]}.")
                }
            }
        }
    }
}
