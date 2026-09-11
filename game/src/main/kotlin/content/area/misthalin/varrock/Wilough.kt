package content.area.misthalin.varrock

import content.entity.player.dialogue.Admit
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.quest.member.gertrudes_cat.GERTRUDES_CAT_STRING_NAME
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

const val WILOUGH_STRING_NAME = "wilough"

// Source: https://runescape.wiki/w/Transcript:Wilough
// For some reason his face is small in dialogue.

class Wilough : Script {
    init {
        npcOperate("Talk-to", WILOUGH_STRING_NAME){
            when (quest(GERTRUDES_CAT_STRING_NAME)) {
                "completed" -> postQuest()
                "spoke_to_gertrude" -> lookingForInformationBranch()
                "found_the_boys" -> gertrudeWhereFluffs()
                else -> unstarted()
            }
        }
    }

    private suspend fun Player.unstarted() {
        player<Happy>("Hello youngster!")
        npc<Idle>("I don't talk to strange old people.", largeHead = true)
        player<Angry>("Hey who you calling old?! And strange?!")
        npc<Idle>("You obviously. Old, strange and stupid.", largeHead = true)
        player<Idle>("Whatever.")
    }

    private suspend fun Player.lookingForInformationBranch() {
        gertrudeLookingForInformation()
        choice {
            option<Angry>("Tell me sonny, or I will inform your mum you are a pair of criminals.") {
                npc<Scared>("W..wh..what?! Y..you wouldn't! Anyway, I'll deny it all and she'll be sure to believe me over some wandering killer like you.", largeHead = true)
                player<Angry>("I'm an upstanding citizen!")
                npc<Angry>("I'm her darling boy and you'd have to forget about her rewarding you. Hop it snitch.", largeHead = true)
                npc<Happy>(npcId = SHILOP_STRING_NAME, "And I'm even more her favourite than he is!", largeHead = true)
                statement("You decide it's best not to aggravate the repulsive boys any more.")
            }
            option<Quiz>("What will make you tell me?") {
                gertrudeFluffsCost()
            }
            option<Idle>("Well never mind, it's Fluffs loss.") {
                gertrudeFluffsLoss()
            }
        }
    }

    suspend fun Player.gertrudeFluffsCost() {
        npc<Neutral>("Well... Now you ask, I'm a bit short on cash.", largeHead = true)
        player<Idle>("How much?")
        npc<Happy>("100 coins should cover it.", largeHead = true)
        player<Angry>("100 coins! What sort of expensive things do you need that badly?")
        npc<Happy>("Well I Don't like chocolate and have you seen how much sweets cost to buy?", largeHead = true)
        player<Quiz>("Why should I pay you then, canyou answer that as easily?")
        npc<Idle>("Obviously you shouldn't pay that much, but I won't help otherwise. I never liked that cat anyway, fussy scratchy thing it is, so what do you say?", largeHead = true)
        choice {
            option<Idle>("I'm not paying you a thing.") {
                gertrudeLifeOfCrime()
            }
            option("Okay then, I'll pay.") {
                gertrudePayChildren()
            }
        }
    }

    suspend fun  Player.gertrudePayChildren() {
        player<Idle>("Okay then, I'll pay, but I'll want you to tell your mother what a nice person I am.")
        npc<Quiz>("What?", largeHead = true)
        player<Idle>("I'll want you to tell your mother what a nice person I am so she rewards me for this search.")
        npc<Happy>("It's a deal.", largeHead = true)
        if (inventory.contains("coins", 100)) {
            inventory.remove("coins", 100)
            set(GERTRUDES_CAT_STRING_NAME, "found_the_boys")
            statement("You give the lad 100 coins.")
            player<Idle>("There you go, now where did you see Fluffs?")
            npc<Idle>(npcId = WILOUGH_STRING_NAME, "I play at an abandoned lumber mill to the north east. Just beyond the Jolly Boar Inn. I saw Fluffs running around in there.")
            player<Quiz>("Anything else?")
            npc<Idle>(npcId = SHILOP_STRING_NAME, "Well, you'll have to find the broken fence to get in. I'm sure you can manage that.")
        } else {
            player<Sad>("This is a bit embarrassing - I'll have to get some money first.")
            npc<Idle>("I'll be waiting.")
            message("<red>You can not afford that.")
        }
    }

    suspend fun Player.gertrudeLifeOfCrime() {
        npc<Idle>("Okay then, I'll find another way to make money. You only have yourself to blame if I'm forced into a life of crime.")
    }

    suspend fun Player.gertrudeFluffsLoss() {
        npc<Idle>("I'm sure our mum will get over it.")
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