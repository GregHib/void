package content.area.kandarin.ardougne.witchaven

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

class Kent : Script {

    init {
        npcOperate("Talk-to", "kent_fishing_platform") {
            when {
                questCompleted("kenniths_concerns") -> afterKennithsConcerns()
                questCompleted("slug_menace") -> afterSlugMenace()
                else -> afterSeaSlug()
            }
        }
    }

    // https://runescape.wiki/w/Transcript:Sea_Slug (Post-quest dialogue)
    private suspend fun Player.afterSeaSlug() {
        npc<Happy>("Hello there, $name!")
        player<Neutral>("Hello again Kent.")
        npc<Happy>("I never did get the chance to thank you properly for saving Kennith and myself.")
        player<Happy>("Oh, don't be silly, it was nothing really.")
        npc<Happy>("Play it down if you will. It was a truly brave thing you did.")
        player<Happy>("I only did what anyone would have done in my position. I have to go now, take care.")
        npc<Happy>("You too, $name. Goodbye.")
    }

    // https://runescape.wiki/w/Transcript:Kent
    private suspend fun Player.afterSlugMenace() {
        player<Happy>("Hello, Kent!")
        npc<Happy>("Oh, hello, $name. It's great to see you again. You know, Kennith is always talking about you since you rescued him from the sea slugs. I'm sure he'd love to talk to you - he's just downstairs.")
        player<Neutral>("Sure, I might go and say 'Hello'.")
    }

    // https://runescape.wiki/w/Transcript:Kent
    private suspend fun Player.afterKennithsConcerns() {
        player<Neutral>("Hello, Kent. How are things in Witchaven with Kennith gone and all.")
        npc<Neutral>("Hi, $name. We miss him terribly, but there can be no denying that things are a lot more peaceful for us.")
        player<Neutral>("How is he doing out on the Fishing Platform?")
        npc<Neutral>("Bailey's looking after him well and Kennith seems less, erm, troubled.")
        player<Neutral>("Maybe Kennith has learnt his lesson.")
        npc<Neutral>("Oh, yes, he's grown up, that's for sure. I don't think we'll be seeing him enslaving whole populations of people over a lost toy again.")
    }
}
