package content.area.misthalin.varrock

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.EvilLaugh
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.member.gertrudes_cat.GERTRUDES_CAT
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

// Source: https://runescape.wiki/w/Transcript:Wilough
class WiloughAndShilop : Script {
    init {
        npcOperate("Talk-to", "wilough,shilop") { (target) ->
            when (quest(GERTRUDES_CAT)) {
                "completed" -> gertrudePostQuest(target)
                "spoke_to_gertrude" -> lookingForInformationBranch(target)
                "found_the_boys" -> gertrudeWhereFluffs()
                "attempt_fluffs_pickup" -> gertrudeFluffsNotComeBack()
                else -> gertrudeUnstarted()
            }
        }
    }

    suspend fun Player.gertrudeUnstarted() {
        player<Happy>("Hello youngster!")
        npc<Bored>("I don't talk to strange old people.")
        player<Angry>("Hey who you calling old?! And strange?!")
        npc<Bored>("You obviously. Old, strange and stupid.")
        player<Neutral>("Whatever.")
    }

    suspend fun Player.gertrudePostQuest(target: NPC) {
        player<Happy>("Hello again.")
        npc<Angry>("You think you're tough do you?")
        player<Shock>("Pardon?")
        npc<Angry>("I can beat anyone up.")
        npc<Angry>(npcId = target.brother, "He can you know!")
        player<Bored>("Really?")
        // anim 12447 on rs3
        statement("The boy starts jumping around with his fists up. You decide it's best not to kill him yet.") // Source - Under "Trivia" last point: https://runescape.wiki/w/Wilough
    }

    // Source: https://oldschool.runescape.wiki/w/Transcript:Gertrude%27s_Cat
    suspend fun Player.gertrudeWhereFluffs() {
        player<Neutral>("Where did you say you saw Fluffs?")
        npc<Bored>("Weren't you listening? I saw the flea bag in the old lumber mill just north east of here. Just walk past the Jolly Boar Inn and you should find it.")
    }

    suspend fun Player.gertrudeFluffsNotComeBack() {
        player<Neutral>("I've found Fluffs but she won't come back home with me. Any ideas?")
        npc<Bored>("None at all. Not that I want that boring thing back, anyway. I prefer dogs to a fat cat, any day.")
    }

    suspend fun Player.lookingForInformationBranch(target: NPC) {
        gertrudeLookingForInformation()
        choice {
            option<Angry>("Tell me sonny, or I will inform your mum you are a pair of criminals.") {
                npc<Scared>("W..wh..what?! Y..you wouldn't! Anyway, I'll deny it all and she'll be sure to believe me over some wandering killer like you.", largeHead = true)
                player<Angry>("I'm an upstanding citizen!")
                npc<EvilLaugh>("I'm her darling boy and you'd have to forget about her rewarding you. Hop it snitch.")
                npc<Angry>(npcId = target.brother, "And I'm even more her favourite than he is!")
                statement("You decide it's best not to aggravate the repulsive boys any more.")
            }
            option<Neutral>("What will make you tell me?") {
                gertrudeFluffsCost(target)
            }
            option<Bored>("Well never mind, it's Fluffs loss.") {
                npc<Bored>("I'm sure our mum will get over it.")
            }
        }
    }

    suspend fun Player.gertrudeLookingForInformation() {
        player<Neutral>("Hello there, I've been looking for you and it's important.")
        npc<Shock>("I didn't mean to take it! I just forgot to pay.")
        player<Confused>("What? I'm trying to help your mum find some cat called Fluffs.")
        npc<Bored>("Ohh...well, in that case I might be able to help. Fluffs followed me to my super secret hideout, I haven't seen her since. She's probably off eating small creatures somewhere.")
        player<Quiz>("Where is this secret hideout? I really need to find that cat for your mum.")
        npc<Bored>("If I told you that, it wouldn't be a secret. What if I need to escape from the law? I need a hideout.")
        player<Neutral>("From my limited knowledge of the law, they are not usually involved in manhunts for children.")
        npc<Bored>("Well it's still mine anyway, we need a place to relax sometimes. Those two little brothers at the house are just such babies.")
    }

    suspend fun Player.gertrudeFluffsCost(target: NPC) {
        npc<Shifty>("Well... Now you ask, I'm a bit short on cash.")
        player<Bored>("How much?")
        npc<Happy>("100 coins should cover it.")
        player<Angry>("100 coins! What sort of expensive things do you need that badly?")
        npc<Neutral>("Well I don't like chocolate and have you seen how much sweets cost to buy?")
        player<Bored>("Why should I pay you then, can you answer that as easily?")
        npc<Bored>("Obviously you shouldn't pay that much, but I won't help otherwise. I never liked that cat anyway, fussy scratchy thing it is, so what do you say?")
        choice {
            option<Angry>("I'm not paying you a thing.") {
                npc<Sad>("Okay then, I'll find another way to make money. You only have yourself to blame if I'm forced into a life of crime.")
            }
            option("Okay then, I'll pay.") {
                gertrudePayChildren(target)
            }
        }
    }

    suspend fun Player.gertrudePayChildren(target: NPC) {
        player<Neutral>("Okay then, I'll pay, but I'll want you to tell your mother what a nice person I am.")
        npc<Quiz>("What?")
        player<Neutral>("I'll want you to tell your mother what a nice person I am so she rewards me for this search.")
        npc<Happy>("It's a deal.")
        if (inventory.remove("coins", 100)) {
            set(GERTRUDES_CAT, "found_the_boys")
            set("gertrudes_cat_know_where_fluffs_is", true)
            statement("You give the lad 100 coins.")
            player<Neutral>("There you go, now where did you see Fluffs?")
            npc<Neutral>(npcId = "wilough", "I play at an abandoned lumber mill to the north east. Just beyond the Jolly Boar Inn. I saw Fluffs running around in there.")
            player<Neutral>("Anything else?")
            npc<Bored>(npcId = target.brother, "Well, you'll have to find the broken fence to get in. I'm sure you can manage that.")
        } else {
            player<Shifty>("This is a bit embarrassing - I'll have to get some money first.")
            npc<Neutral>("I'll be waiting.") // TODO proper expression
            message("<red>You can not afford that.")
        }
    }

    private val NPC.brother: String
        get() = if (id == "wilough") "shilop" else "wilough"
}
