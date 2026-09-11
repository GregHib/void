package content.area.tirannwn.lletya

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.male

class LletyaResidents : Script {

    init {
        npcOperate("Talk-to", "goreu") {
            player<Neutral>("Hello.")
            npc<Happy>("Good day, can I help you?")
            player<Neutral>("No thanks I'm just watching the world go by.")
            npc<Happy>("Well I can think of no better place to do it, it is beautiful here is it not?")
            player<Happy>("Indeed it is.")
        }

        npcOperate("Talk-to", "arvel") {
            npc<Happy>("Good day traveller. You are far from home, what brings you here?")
            player<Happy>("I am a wandering ${if (male) "hero" else "heroine"}. I come here in search of adventure.")
            npc<Neutral>("Sounds ghastly, I just want to live in peace.")
            player<Neutral>("Unfortunately without people like me, peace doesn't last for long.")
            npc<Neutral>("True, but then again most adventurers cause as much trouble as they put right.")
            player<Quiz>("You've got a point there... Hmm...")
            npc<Happy>("Well, good day traveller. And always do the right thing.")
        }

        npcOperate("Talk-to", "mawrth") {
            npc<Neutral>("Those children are nothing but trouble - if I did not watch them all the time, Seren knows what trouble they would get in to!")
            player<Neutral>("They look old enough to look after themselves.")
            npc<Neutral>("They are only 34 and 38, far too young to be left unsupervised.")
            player<Quiz>("Only!?! How old does that make you?")
            npc<Neutral>("Has no one told you it is rude to ask a lady her age?")
            player<Neutral>("Sorry, I wasn't thinking. Anyway... I'd better stop distracting you.")
            npc<Happy>("Okay, See you some other time.")
        }

        npcOperate("Talk-to", "kelyn") {
            player<Neutral>("Hello.")
            npc<Neutral>("Huh... Oh sorry, you made me jump. I was miles away, day dreaming.")
            player<Quiz>("About what may I ask?")
            npc<Happy>("I was thinking about the crystal spires of Prifddinas.")
            player<Neutral>("It must be beautiful, I've only seen the city walls.")
            npc<Neutral>("I have never seen it, all I know are the stories. I hope that changes one day.")
        }

        npcOperate("Talk-to", "eoin") {
            player<Neutral>("Hello.")
            npc<Happy>("Sorry, I cannot stop or Iona will catch me, we are playing tag!")
        }

        npcOperate("Talk-to", "iona") {
            player<Neutral>("Hello.")
            npc<Neutral>("I can never catch Eoin, he is just too fast, I am always 'It'.")
        }

        npcOperate("Talk-to", "ysgawyn") {
            player<Neutral>("Hello.")
            npc<Neutral>("Greetings, human. Not many of your kind find their way to Lletya.")
            player<Quiz>("It's quite the hidden village.")
            npc<Neutral>("And it must remain so. The safety of everyone here depends on it.")
        }

        npcOperate("Talk-to", "gwir") {
            player<Neutral>("Hello.")
            npc<Happy>("Good day to you. I am recording the history of our people, there is much to set down.")
            player<Quiz>("Anything I can help with?")
            npc<Neutral>("Unless you can remember what happened a few thousand years ago, I fear not.")
        }

        npcOperate("Talk-to", "arianwyn_lletya,arianwyn_lletya_2,arianwyn_lletya_3") {
            statement("He doesn't seem interested in talking to you.")
        }
    }
}
