package content.area.misthalin.draynor_village.manor

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Unamused
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script

class Witch : Script {

    init {
        // https://runescape.wiki/w/Transcript:Helda
        npcOperate("Talk-to", "witch_normal") {
            if (questCompleted("animal_magnetism")) {
                npc<Unamused>("Hello my ickle...oh, it's you. What do you want?")
                player<Quiz>("I wondered whether you had anything interesting to say.")
                npc<Unamused>("Clearly not.")
                return@npcOperate
            }
            npc<Happy>("Hello there, deary. Don't worry; I'm friendly.")
            player<Quiz>("You look very familiar but I can't remember chatting to you before.")
            npc<Neutral>("Well, my sisters used to live here but folk kept on killing them. A terrible affair, it was. I knew that no one would want to kill a chatty old dear like me, though.")
            player<Neutral>("Oh, well...")
            npc<Neutral>("We still have a terrible problem with local louts, though. Poisoning my fish, spilling my compost all over the place and causing a nuisance for the dear count.")
            player<Neutral>("I think...")
            npc<Happy>("I'm sure a nice young sort like you won't be any trouble, though. In any case, the Professor will soon share the secret of chickenisation with me... Not that I'd use it these days of course.")
            player<Neutral>("I have to go now.")
        }
    }
}
