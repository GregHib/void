package content.area.misthalin.varrock

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

private const val PHILOP_STRING_NAME = "philop"

// Source: https://runescape.wiki/w/Transcript:Philop
class Philop : Script {
    init {
        npcOperate("Talk-to", PHILOP_STRING_NAME) {
            when (quest("gertrudes_cat")) {
                "spoke_to_gertrude" -> lookingForInformation()
                else -> unstarted()
            }
        }
    }
    private suspend fun Player.unstarted() {
        greetChild()
        player<Idle>("Enjoy playing with your dragon, then.")
        npc<Happy>("Gwwwrrr!")
    }
    private suspend fun Player.greetChild() {
        player<Happy>("Hello, what's your name?")
        npc<Happy>("Gwwrr!")
        player<Quiz>("Err, hello there. What's that you have there?")
        npc<Happy>("Gwwwrrr! Dwa-gon Gwwwwrrrr!")
    }

    private suspend fun Player.lookingForInformation() {
        greetChild()
        player<Quiz>("What a nice dragon, have you seen Fluffs little boy?")
        npc<Happy>("Dwa-gon nasty, dwa-gon eat Fwuffs, gwwwrrr, hungwy chomp chomp!")
        player<Idle>("I see. I don't think this is the most helpful clue I have ever been given.")
        npc<Happy>("Gwwrr!")
    }
}