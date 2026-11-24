package content.area.kharidian_desert.pollnivneach.dungeon

import content.entity.player.dialogue.Amazed
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * https://runescape.wiki/w/Transcript:Catolax
 */
class Catolax : Script {

    init {
        npcOperate("Talk-to", "catolax_pollninveach_dungeon_2") {
            npc<Amazed>("I sense you are becoming adept at new Slayer techniques. Impressive.")
            player<Happy>("It was all thanks to your help.")
            npc<Neutral>("Nice of you to say so, although the cats have definitely had something to do with it. It hints at divine intervention.")
            player<Neutral>("It seems I'm the subject of a few too many divine interventions for comfort. It's lucky indeed that I have my feline friend.")
            npc<Quiz>("Heroes aren't the same as they were in my day. Can I help you with anything else?")
            options()
        }
    }

    private suspend fun Player.options() {
        choice {
            option("Could you tell me what's going on?") {
                player<Quiz>("Could you tell me what was going on?")
                npc<Neutral>("The crux of the matter is that you were working for Sumona, who is the Devourer.")
                player<Neutral>("I can't say I am happy to have helped her in her plans. She never struck me as a nice type.")
                npc<Neutral>("Oh, but you didn't help her - she expected you to die. She did promise to reward you, however, so take advantage of that.")
                player<Quiz>("What about this twin sister then? Is she a deity too?")
                npc<Neutral>("Twin sister? It was the Devourer too; she just wanted to watch when the banshee defeated you.")
                player<Neutral>("I presume that her being a deity is going to make it awkward for me, having riled her so much.")
                npc<Neutral>("Her bargain isn't one she can wriggle out of easily, although sending you on Slayer tasks will be far from safe. You'll also find that you forget her evil deeds when talking with her, such is the power she possesses.")
                player<Neutral>("The fake-sister thing explains why she burned that book and what the insectoids were doing there.")
                npc<Neutral>("Indeed. Thank you for avenging me. It is as much as can be expected by a dead mortal who wishes revenge on an immortal deity.")
                options()
            }
            option("Can you tell me about the smoky dungeon?") {
                player<Quiz>("Can you tell me about the smoky dungeon at all?")
                npc<Neutral>("You will, of course, be aware that the place is a hive of Slayer creatures. Each of these must be combated within the smoke you mention.")
                player<Quiz>("There are no new dangers I should be aware of?")
                npc<Neutral>("There are four guardians - turoth, kurask, cave crawler and basilisk - that must be slain in order to access the four new areas within the underground passages. No problem for you, I would suspect, as you were able to easily deal with the banshee mistress. Is there anything more I can help with?")
                options()
            }
            option<Quiz>("I'd like some information on the remaining guardians.") {
                guardians()
            }
            option("Are you both in your tomb and the smoky dungeon?") {
                player<Quiz>("Are you in both your tomb and the smoky dungeon?")
                npc<Neutral>("I have a spiritual presence in both places associated with my death - my tomb and the place of my murder.")
                player<Neutral>("That must be useful. I wish I could be in two places at once.")
                npc<Neutral>("I don't think that my position is altogether enviable. After all, I'm dead and can only ever exist in two places. You are alive and can go anywhere your legs take you.")
            }
        }
    }

    private suspend fun Player.guardians() {
        if (GUARDIANS.all { get("killed_$it", false) }) {
            npc<Neutral>("You seem to have killed all of these creatures already. I'm not sure what more you need to know.")
            return
        }
        choice {
            option<Quiz>("I'd like to know about the mightiest turoth.") {
                npc<Neutral>("From what I've heard, the mightiest turoth can summon its offspring to aid it. These are smaller and weaker than the mightiest turoth but they are essentially unlimited in number.")
                player<Quiz>("How can I deal with such an onslaught?")
                npc<Neutral>("I'd guess that killing the leader first would be wise. If you like infinite carnage, however, then slaughtering the offspring might be pleasant for a few moments.")
                guardians()
            }
            option<Quiz>("I'd like to know about the kurask overlord.") {
                npc<Neutral>("As far as I can divine, the kurask overlord in the caves can summon its cohorts to aid it. These are almost the equal of the creature itself.")
                player<Quiz>("What would you suggest?")
                npc<Neutral>("I'd guess that killing the overlord as quickly as you can would be the plan. They are bulky creatures, however, so you may be able to use this to your advantage.")
                guardians()
            }
            option<Quiz>("I'd like to know about the monstrous cave crawler.") {
                npc<Neutral>("The cave crawlers are a venomous bunch and their guardian even more so. Adventurers who have seen it and lived have told of its power to poison, even if an opponent has temporary immunity. I certainly would not look at it as a feeble pushover. Take loads of some poison cure.")
                guardians()
            }
            option<Quiz>("I'd like to know about the basilisk boss.") {
                npc<Neutral>("The basilisk boss is similar to most of its kind, but with a fiercer glare. Whatever draining effects a normal basilisk unleashes, you can expect the basilisk boss to unleash with more power.")
                guardians()
            }
            option("I'm not after anything in fact.") {
                player<Neutral>("I'm not after anything, in fact.")
            }
        }
    }

    companion object {
        private val GUARDIANS = listOf("turoth_mightiest", "kurask_overlord", "monstrous_cave_crawler", "basilisk_boss")
    }
}
