package world.gregs.voextractedd.activity.skill.slayer.master

import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.activity.quest.questComplete
import world.gregs.voidps.world.activity.skill.slayer.slayerTasks
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate("Talk-to", "turael") {
    if (player.slayerTasks == 0) {
        player<Quiz>("Who are you?")
        npc<Talk>("I'm one of the elite Slayer Masters.")
        choice {
            option<Talk>("What's a slayer?") {
                npc<Upset>("Oh dear, what do they teach you in school?")
                player<Uncertain>("Well... er...")
                npc<Talk>("I suppose I'll have to educate you then. A slayer is someone who is trained to fight specific creatures. They know these creatures' every weakness and strength. As you can guess it makes killing them a lot easier.")
                teachMe()
            }
            option<Talk>("Never heard of you...") {
                npc<Talk>("That's because my foe never lives to tell of me. We slayers are a dangerous bunch.")
                teachMe()
            }
        }
    } else {
        npc<Talk>("'Ello, and what are you after then?")
        choice {
            option<Talk>("I need another assignment.") {

            }
            option<Quiz>("Have you any rewards for me, or anything to trade?") {

            }
            option<Talk>("Let's talk about the difficulty of my assignments.") {

            }
            option<Talk>("I'm here about blessed axes again.", filter = { player.questComplete("animal_magnetism") }) {

            }
        }
    }
}

suspend fun NPCOption.teachMe() {
    choice {
        option<Talk>("Wow, can you teach me?") {
            npc<Uncertain>("Hmmm well I'm not so sure...")
            player<Talk>("Pleeeaasssse!")
            npc<Talk>("Oh okay then, you twisted my arm. You'll have to train against specific groups of creatures.")
            player<Quiz>("Okay, what's first?")
            val monster = ""
            val count = 0
            npc<Talk>("We'll start you off hunting $monster, you'll need to kill $count of them.")
            npc<Talk>("You'll also need this enchanted gem, it allows Slayer Masters like myself to contact you and update you on your progress. Don't worry if you lose it, you can buy another from any Slayer Master.")
            player.inventory.add("enchanted_gem")
            choice {
                option("Got any tips for me?") {

                }
                option<Talk>("Okay, great!") {
                    npc<Happy>("Good luck! Don't forget to come back when you need a new assignment.")
                }
            }
        }
        option<Neutral>("Sounds useless to me.") {
            npc<Neutral>("Suit yourself.")
        }
    }
}