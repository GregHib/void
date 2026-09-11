package content.area.misthalin.varrock

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

private const val KANEL_STRING_NAME = "kanel"

// Source: https://runescape.wiki/w/Transcript:Kanel

class Kanel : Script {
    init {
        npcOperate("Talk-to", KANEL_STRING_NAME) {
            when (quest("gertrudes_cat")) {
                "spoke_to_gertrude" -> lookingForInformation()
                else -> unstarted()
            }
        }
    }

    private suspend fun Player.unstarted() {
        player<Happy>("Hello there.")
        npc<Quiz>("Hel-lo?", largeHead = true)
        player<Idle>("Right. Goodbye.")
        npc<Quiz>("Bye?", largeHead = true)
    }

    private suspend fun Player.lookingForInformation() {
        player<Happy>("Hello there.")
        npc<Quiz>("Hel-lo?", largeHead = true)
        player<Quiz>("Have you seen Fluffs?")
        npc<Quiz>("F-wuffs?", largeHead = true)
        player<Idle>("She's your mummy's cat.")
        npc<Quiz>("Cat?", largeHead = true)
        player<Idle>("Right. Bye Kanel.")
        npc<Quiz>("Me?", largeHead = true)
    }
}