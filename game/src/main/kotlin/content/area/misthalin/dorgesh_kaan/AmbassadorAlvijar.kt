package content.area.misthalin.dorgesh_kaan

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class AmbassadorAlvijar : Script {
    init {
        // https://runescape.wiki/w/Transcript:Ambassador_Alvijar#After_Another_Slice_of_H.A.M._and_before_The_Chosen_Commander
        npcOperate("Talk-to", "ambassador_alvijar_dorgesh_kaan_2") {
            when (random.nextInt(5)) {
                0 -> npc<Angry>("I can't stand these goblins sometimes! I come up with suggestions to make their city more efficient, and they just say it would spoil their precious way of life!")
                1 -> npc<Sad>("I miss Keldagrim. This city is all very well, but no one makes an underground city like the dwarves!")
                2 -> {
                    npc<Neutral>("It's funny... whenever we dwarves have ventured onto the surface, we've found ourselves at war with the goblins there.")
                    npc<Neutral>("But now we meet these goblins underground, and they're no threat at all. Maybe this will herald a change in our relations with the surface goblins as well.")
                }
                3 -> npc<Neutral>("Trade with the goblins is going well at the moment. It's a pity they don't seem as interested in our beer as in your food, though!")
                else -> {
                    npc<Happy>("Thank you again for helping us complete the train link. Further delays would have cost the Consortium a lot of money.")
                    npc<Happy>("Oh, and I hear you saved someone's life as well. Well done on that count too.")
                }
            }
        }
    }
}
