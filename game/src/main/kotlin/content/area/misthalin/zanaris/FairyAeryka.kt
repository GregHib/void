package content.area.misthalin.zanaris

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

class FairyAeryka : Script {
    init {
        npcOperate("Talk-to", "fairy_aeryka") {
            if (get("met_fairy_aeryka", false)) {
                npc<Happy>("It's still here.")
                player<Quiz>("Pardon?")
                npc<Happy>("It's still here. The crop circle is still here.")
                player<Happy>("Oh yes, thanks Aery. It didn't go anywhere in the meantime, then?")
                npc<Happy>("Nope. It just sat there.")
                player<Happy>("Jolly good. I can come back and visit Puro-Puro whenever I want then. Brilliant!")
                anythingElse()
            } else {
                player<Idle>("Hello, my name is $name. Who are you?")
                npc<Happy>("Oh hello $name. I'm Aeryka. Aery for short.")
                player<Laugh>("Airy Fairy?")
                npc<Happy>("That's right. What can I do for you?")
                player<Quiz>("What's that crop circle thing doing here?")
                npc<Happy>("Crop circle? Oh, you mean the Puromantic portal?")
                player<Quiz>("The pyromanic what?")
                npc<Happy>("The Puromantic portal. At least that's what we call them. It's the way the implings travel from Puro-Puro to other planes.")
                player<Quiz>("Puro-Puro?")
                npc<Happy>("The impling home. We call it Puro-Puro. The implings just call it home, I think.")
                set("met_fairy_aeryka", true)
                anythingElse()
            }
        }
    }

    private suspend fun Player.anythingElse() {
        choice("Is there anything else you want to ask?") {
            puroPuro()
            implings()
            dragonEquipment()
            bye()
        }
    }

    private fun ChoiceOption.dragonEquipment() {
        option<Neutral>("I've heard I may find dragon equipment in Puro-Puro.") {
            npc<Happy>("Really? You humans like that stuff a lot, don't you? I don't like really old stuff myself.")
            player<Quiz>("Old?")
            npc<Happy>("Yes, dragon stuff feels really old.")
            player<Quiz>("How can you tell that?")
            npc<Happy>("From its magical aura, obviously. Oh, I forget you humans can't feel auras.")
            player<Quiz>("How old is old?")
            npc<Happy>("Really, really, old.")
            player<Quiz>("Can you be any more precise?")
            npc<Happy>("Not really. Time doesn't really mean a lot round here. Hundreds, maybe thousands of your human lifespans, I suppose. Anyway, it would have to be old since it all comes from the Necrosyrtes.")
            player<Quiz>("Necrosyrtes? Who are they?")
            npc<Neutral>("Old and powerful creatures. I don't think there have been any around here for aeons. I haven't seen one. I don't think they are very nice. Not like me. Anyway, this is all ancient history. Boooring!")
            player<Quiz>("While I'm on the subject do you have any dragon stuff I can have?")
            npc<Happy>("Oh, I did have loads, but I threw it away.")
            player<Shock>("What?")
            npc<Laugh>("Only joking. ")
            npc<Happy>("No, sorry, I can't help you there. You'll have to look for it yourself. Although, maybe if you find Necrosyrtes then they'll give you some. I heard they give dragon stuff away to people they like. Not fairies. We're too")
            npc<Happy>("nice.")
            choice("Is there anything else you want to ask?") {
                puroPuro()
                implings()
                bye()
            }
        }
    }

    private fun ChoiceOption.implings() {
        option<Quiz>("So what are these implings then?") {
            npc<Happy>("Well, no-one knows for sure. The mischievous little creatures are probably related to imps. And they fly as well.")
            npc<Happy>("Also, like imps, they love collecting things. I'm not sure why, though. They also seem to like being chased.")
            player<Quiz>("So how would I get hold of what they are carrying, then?")
            npc<Happy>("Catch them, I suppose. I don't know really. Why would you want to?")
            player<Quiz>("Well, if they were carrying something useful. Maybe I could catch them with a big net - like butterflies.")
            npc<Sad>("Sounds a bit cruel to me, but I suppose that's possible.")
            choice("Is there anything else you want to ask?") {
                puroPuro()
                dragonEquipment()
                bye()
            }
        }
    }

    private fun ChoiceOption.puroPuro() {
        option<Quiz>("What's in Puro-Puro?") {
            npc<Happy>("Implings...and wheat.")
            player<Quiz>("Erm, anything else?")
            npc<Happy>("Not really. Though I have noticed quite a lot of you humans travelling through the portal recently. I suppose you must like wheat.")
            player<Quiz>("Well, most of us prefer lobsters to be perfectly honest, but there must be something interesting there.")
            npc<Happy>("Oh, I did notice a very serious-looking gnome go into the portal. Maybe he knows what's going on.")
            player<Quiz>("Do you remember what his name was?")
            npc<Happy>("Errm, Egg-nog, or something like that.")
            player<Quiz>("Right, thanks! I'll have a chat with him when I go. Maybe he'll know what's going on.")
            choice("Is there anything else you want to ask?") {
                implings()
                dragonEquipment()
                bye()
            }
        }
    }

    private fun ChoiceOption.bye() {
        option<Happy>("No, bye!") {
            npc<Happy>("See you around!")
        }
    }
}
