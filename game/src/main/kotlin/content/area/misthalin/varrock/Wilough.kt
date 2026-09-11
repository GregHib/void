package content.area.misthalin.varrock

import content.entity.player.dialogue.Admit
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.type.npc
import content.quest.member.gertrudes_cat.GERTRUDES_CAT_STRING_NAME
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement

const val WILOUGH_STRING_NAME = "wilough"

// Source: https://runescape.wiki/w/Transcript:Wilough
// For some reason his face is small in dialogue.

class Wilough : Script {
    init {
        npcOperate("Talk-to", WILOUGH_STRING_NAME){
            when (quest(GERTRUDES_CAT_STRING_NAME)) {
                "completed" -> postQuest()
                "spoke_to_gertrude" -> gertrudeLookingForInformation()
                "found_the_boys" -> gertrudeWhereFluffs()
                else -> unstarted()
            }
        }
    }

    private suspend fun Player.unstarted() {
        player<Happy>("Hello youngster!")
        npc<Idle>("I don't talk to strange old people.")
        player<Angry>("Hey who you calling old?! And strange?!")
        npc<Idle>("You obviously. Old, strange and stupid.")
        player<Idle>("Whatever.")
    }

    suspend fun Player.gertrudeLookingForInformation() {
        player<Idle>("Hello there, I've been looking for you and it's important.")
        npc<Scared>("I didn't mean to take it! I just forgot to pay.")
        player<Quiz>("What? I'm trying to help your mum find some cat called Fluffs.")
        npc<Idle>("Ohh...well, in that case I might be able to help. Fluffs followed me to my super secret hideout, I haven't seen her since. She's probably off eating small creatures somewhere.")
        player<Quiz>("Where is this secret hideout? I really need to find that cat for your mum.")
        npc<Idle>("If I told you that, it wouldn't be a secret. What if I need to escape from the law? I need a hideout.")
        player<Idle>("From my limited knowledge of the law, they are not usually involved in manhunts for children.")
        npc<Admit>("Well it's still mine anyway, we need a place to relax sometimes. Those two little brothers at the house are just such babies.")
    }

    private suspend fun Player.postQuest() {
        player<Idle>("Hello again.")
        npc<Idle>("You think you're tough do you?")
        player<Quiz>("Pardon?")
        npc<Laugh>("I can beat anyone up.")
        player<Quiz>("Really?")
        statement("The boy starts jumping around with his fists up. You decide it's best not to kill him yet.") // Source - Under "Trivia" last point: https://runescape.wiki/w/Wilough
    }

    // Source: https://oldschool.runescape.wiki/w/Transcript:Gertrude%27s_Cat
    suspend fun Player.gertrudeWhereFluffs() {
        player<Quiz>("Where did you say you saw Fluffs?")
        npc<Quiz>("Weren't you listening? I saw the flea bag in the old lumber mill just north east of here. Just walk past the Jolly Boar Inn and you should find it.")
    }
}