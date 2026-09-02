package content.area.morytania.braindeath_island

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory

/**
 * Brewers on Braindeath Island. Eight unique NPCs all share this dialogue
 * handler with personality-specific responses at each branch point.
 *
 * NPC IDs (Java) -> roles:
 *   2851 - "one of THEM" / paranoid
 *   2852 - "Game over, man!" / panicked
 *   2853 - calm but secretly crying
 *   2854 - angry war veteran
 *   2855 - opportunistic traitor / Hungry Frank
 *   2856 - sleep-deprived / drunk
 *   2857 - paranoid about disguised zombies
 *   2858 - resigned to dying
 */
class Brewer : Script {
    init {
        npcOperate("Talk-to", "brewer*") { (target) ->
            val brewer = target.number()
            when (questStage("rum_deal")) {
                13 -> wrenchSearch(brewer)
                19 -> postQuestGreeting(brewer)
                else -> initialGreeting(brewer)
            }
        }
    }

    // ===== Default greeting (during the quest, before the wrench step) =====

    private suspend fun Player.initialGreeting(brewer: Int) {
        player<Quiz>("So... how are you holding up?")
        when (brewer) {
            1 -> {
                npc<Shock>("Who are you?")
                npc<Angry>("You're one of THEM arent you!")
                player<Happy>("O...kay...walking away now, smiling and not making eye contact.")
            }
            2 -> {
                npc<Scared>("Game over, man! Game over!")
                player<Shifty>("Errr...keep up the good work.")
            }
            3 -> {
                npc<Neutral>("Very well, considering.")
                player<Quiz>("Considering what?")
                npc<Neutral>("That I've been crawling into the corner to cry whenever nobody is looking.")
                player<Shifty>("Wow... I always thought pirates were tough...")
            }
            4 -> {
                npc<Angry>("Don't sneak up on me like that!")
                npc<Neutral>("Don't you know there's a war on?")
                player<Quiz>("I didn't think this counted as a war.")
                npc<Angry>("That's because you're a landlubber!")
                npc<Angry>("I bet ye've never had to beat a dozen zombies to death with a blunt toothpick!")
                player<Neutral>("That's right. Have you?")
                npc<Scared>("No, I've spent most of my time in the toilet to be honest.")
            }
            5 -> npc<Angry>("Don't talk to me! If I play this right, I can sell you lot out and make it to the mainland!")
            6 -> {
                npc<Drunk>("No school today mother, my brain hurts real baaaad...")
                player<Quiz>("What?")
                npc<Drunk>("I no sleep in many, many, many days.")
            }
            7 -> {
                npc<Scared>("How do I know you're not one of them?")
                player<Quiz>("Well, I don't know... the way I'm breathing could give it away.")
                npc<Angry>("That means nothing! NOTHING!")
            }
            8 -> {
                npc<Neutral>("Very well.")
                player<Quiz>("You sure?")
                npc<Scared>("Oh Guthix, who am I kidding?")
                npc<Scared>("We're all gonna die!")
            }
        }
    }

    // ===== Progress 13: Looking for a priest and a wrench =====

    private suspend fun Player.wrenchSearch(brewer: Int) {
        if (inventory.contains("holy_wrench")) {
            // Show off the blessed wrench
            player<Happy>("Bask in the glory of my Holy Wrench!")
            when (brewer) {
                1 -> npc<Neutral>("Oooh...It's so shiny...")
                2 -> npc<Sad>("(Sob) It's so beautiful...")
                3 -> npc<Shock>("I am not worthy to gaze upon it!")
                4 -> npc<Shock>("I can feel the aura of Wrenchly Power radiating from it!")
                5 -> npc<Sad>("Take it away, I am not worthy to be in its Wrenchly presence.")
                6 -> npc<Neutral>("Wow...I don't feel tired any more...")
                7 -> npc<Scared>("Such power!")
                8 -> npc<Happy>("We're saved!")
            }
        } else {
            // Asking around for help
            player<Neutral>("I'm looking for a priest. And a wrench.")
            when (brewer) {
                1 -> npc<Shock>("What possible plan could involve a priest and a wrench?")
                2 -> {
                    npc<Scared>("What? Are you gonna build some strange mechanical priest to save us?")
                    npc<Neutral>("What if it runs riot? We'll all be killed!")
                }
                3 -> npc<Neutral>("Well good luck. If you find any spare trousers please pass them my way.")
                4 -> npc<Neutral>("Well the usual combo is an old priest and a young priest, but whatever floats your boat lad.")
                5 -> npc<Shock>("What are you going to do to that priest? No, wait, I don't want to know.")
                6 -> {
                    npc<Drunk>("Try Davey, he might have seen it.")
                    player<Happy>("Thanks!")
                    npc<Drunk>("His giant, green, inflatable dwarf might be able to tell you where one is as well.")
                }
                7 -> npc<Quiz>("If this is some sort of secret zombie code then I don't get it.")
                8 -> {
                    npc<Neutral>("Well I can't help you there.")
                    player<Neutral>("Oh, sorry to have bothered you.")
                    npc<Neutral>("No problem.")
                    npc<Sad>("It's not like I'm going to live through this or anything.")
                    npc<Scared>("What if they get me while I sleep?")
                    npc<Scared>("I don't wanna die!")
                }
            }
        }
    }

    // ===== Progress 19: Post-quest greeting =====

    private suspend fun Player.postQuestGreeting(brewer: Int) {
        // The sleep-deprived brewer gets a different opener
        if (brewer == 6) {
            player<Quiz>("So...got any sleep yet?")
        } else {
            player<Happy>("Hello there!")
        }
        when (brewer) {
            1 -> {
                npc<Angry>("I don't know what your game is, but I know you're one of THEM!")
                player<Shock>("But I just saved you!")
                npc<Angry>("The voices tell me different. It's all part of a plot! Confess!")
            }
            2 -> {
                npc<Scared>("Have they gone yet?")
                player<Neutral>("Well, no, but they are a lot calmer now.")
                npc<Scared>("What are we gonna do now, huh? What are we gunna do now?")
                player<Neutral>("In your case I would say relax.")
            }
            3 -> {
                npc<Happy>("Hello yourself!")
                player<Quiz>("How's things?")
                npc<Shifty>("Fine...")
                player<Happy>("Excellent! Since I get the feeling I don't want to know why you said that so oddly I'll just go over here!")
                npc<Neutral>("I think that would be for the best!")
            }
            4 -> {
                npc<Happy>("Hello yerself Landlubber!")
                player<Quiz>("Everything ok with you now?")
                npc<Neutral>("Hmmm...Overall everything is good!")
                player<Happy>("Great!")
            }
            5 -> {
                npc<Laugh>("Hello there, brave hero, in whom I had total confidence!")
                player<Quiz>("Total confidence?")
                npc<Happy>("Yes! I was so confident that I would never, ever have sold your soft, edible body to the pirates outside!")
                player<Shock>("Well, great...")
                npc<Shifty>("On a completely unrelated note, I would steer clear of Hungry Frank for a while.")
                npc<Angry>("He's a filthy liar. And a forger. It wouldn't surprise me if he has written out a note detailing the terms of our surrender and your dismemberment and cooking in MY handwriting.")
                npc<Shifty>("Imagine that, the fiend.")
            }
            6 -> {
                npc<Drunk>("My brain is no longer capable of sleep.")
                player<Quiz>("So...what are you going to do now?")
                npc<Drunk>("I was gonna try and will myself dead.")
                player<Shifty>("Right...good luck with that.")
            }
            7 -> {
                npc<Neutral>("Well you proved that you're probably not a zombie.")
                npc<Quiz>("So what are you then? A ghoul? A vampire?")
                player<Angry>("I'm not any form of undead!")
                player<Quiz>("What?")
                npc<Shifty>("Nothing...")
            }
            8 -> {
                npc<Happy>("You saved us! Huzzah!")
                player<Happy>("All in a day's work, think nothing of it.")
                npc<Neutral>("I don't have anything to reward you with except my collection of bleak, gothic poetry I wrote when I assumed we were all done for. Do you want it?")
                player<Shifty>("I may come for it later, you hang on to it for now.")
            }
        }
    }

    /** Brewers are named `brewer` then `brewer_2` through `brewer_8`. */
    private fun NPC.number(): Int = id.substringAfterLast('_').toIntOrNull() ?: 1
}
