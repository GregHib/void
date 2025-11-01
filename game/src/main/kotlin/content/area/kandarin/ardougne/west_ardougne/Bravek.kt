package content.area.kandarin.ardougne.west_ardougne

import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.softQueue

class Bravek : Script {

    init {
        npcOperate("Talk-to", "bravek") { (target) ->
            when (quest("plague_city")) {
                "talk_to_bravek" -> {
                    npc<Uncertain>("My head hurts! I'll speak to you another day...")
                    choice {
                        option<Angry>("This is really important though!") {
                            npc<Uncertain>("I can't possibly speak to you with my head spinning like this... I went a bit heavy on the drink again last night. Curse my herbalist, she made the best hang over cures. Darn inconvenient of her catching the plague.")
                            choice {
                                option<Neutral>("Okay, goodbye.")
                                option<Angry>("You shouldn't drink so much then!") {
                                    shouldNotDrink()
                                }
                                option<Quiz>("Do you know what's in the cure?") {
                                    cure()
                                }
                            }
                        }
                        option<Neutral>("Okay, goodbye.")
                    }
                }
                "has_cure_paper" -> hasCurePaper(target)
                "gave_cure" -> gaveCure()
                else -> completed()
            }
        }

        itemOnNPCOperate("hangover_cure", "bravek") {
            if (player.quest("plague_city") == "has_cure_paper") {
                player.hasCurePaper(target)
            }
        }
    }

    suspend fun Player.shouldNotDrink() {
        npc<Sad>("Well positions of responsibility are hard, I need something to take my mind off things... Especially with the problems this place has.")
        choice {
            option<Neutral>("Okay, goodbye.")
            option<Quiz>("Do you know what's in the cure?") {
                cure()
            }
            option<Uncertain>("I don't think drink is the solution.") {
                notTheSolution()
            }
        }
    }

    suspend fun Player.cure() {
        npc<Uncertain>("Hmmm let me think... Ouch! Thinking isn't clever. Ah here, she did scribble it down for me.")
        set("plague_city", "has_cure_paper")
        if (inventory.add("a_scruffy_note")) {
            item("a_scruffy_note", 600, "Bravek hands you a tatty piece of paper.")
        } else {
            item("a_scruffy_note", 600, "Bravek waves a tatty piece of paper at you, but you don't have room to take it.")
        }
    }

    suspend fun Player.notTheSolution() {
        npc<Sad>("I don't feel well enough to have a philosophical discussion about it right now. My head hurts.")
        choice {
            option<Quiz>("Do you know what's in the cure?") {
                cure()
            }
            option<Neutral>("Okay, goodbye.")
        }
    }

    suspend fun Player.hasCurePaper(target: NPC) {
        npc<Uncertain>("Uurgh! My head still hurts too much to think straight. Oh for one of Trudi's hangover cures!")
        if (holdsItem("hangover_cure")) {
            player<Neutral>("Try this.")
            inventory.remove("hangover_cure")
            set("plague_city", "gave_cure")
            target.say("Grruurgh!")
            target.transform("bravek_hangover_cure_anim")
            target.softQueue("bravek_transform", 5) {
                target.clearTransform()
            }
            item("hangover_cure", 600, "You give Bravek the hangover cure. Bravek gulps down the foul looking liquid.")
            npc<Happy>("Ooh that's much better! Thanks, that's the clearest my head has felt in a month. Ah now, what was it you wanted me to do for you?")
            gaveCureMenu()
        }
    }

    suspend fun Player.gaveCure() {
        npc<Happy>("Thanks again for the hangover cure.")
        if (holdsItem("warrant")) {
            player<Happy>("Not a problem, happy to help out.")
            npc<Happy>("I'm just having a little drop of whisky, then I'll feel really good.")
        } else {
            npc<Quiz>("Ah now what was it you wanted me to do for you?")
            gaveCureMenu()
        }
    }

    suspend fun Player.gaveCureMenu() {
        player<Neutral>("I need to rescue a kidnap victim called Elena. She's being held in a plague house, I need permission to enter.")
        npc<Neutral>("Well the mourners deal with that sort of thing...")
        choice {
            option<Neutral>("Okay, I'll go speak to them.")
            option<Angry>("Is that all anyone says around here?") {
                npc<Neutral>("Well, they know best about plague issues.")
                choice {
                    option<Quiz>("Don't you want to take an interest in it at all?") {
                        npc<Surprised>("Nope, I don't wish to take a deep interest in plagues. That stuff is too scary for me!")
                        choice {
                            option<Shifty>("I see why people say you're a weak leader.") {
                                npc<Neutral>("Bah, people always criticise their leaders but delegating is the only way to lead. I delegate all plague issues to the mourners.")
                                player<Angry>("This whole city is a plague issue!")
                            }
                            option<Neutral>("Okay, I'll talk to the mourners.")
                            option<Angry>("They won't listen to me!") {
                                wontListen()
                            }
                        }
                    }
                    option<Angry>("They won't listen to me!") {
                        wontListen()
                    }
                }
            }
            option<Angry>("They won't listen to me!") {
                wontListen()
            }
        }
    }

    suspend fun Player.wontListen() {
        player<Angry>("They say I'm not properly equipped to go in the house, though I do have a very effective gas mask.")
        npc<Uncertain>("Hmmm, well I guess they're not taking the issue of a kidnapping seriously enough. They do go a bit far sometimes.")
        npc<Neutral>("I've heard of Elena, she has helped us a lot... Okay, I'll give you this warrant to enter the house.")
        if (inventory.add("warrant")) {
            item("warrant", 600, "Bravek hands you a warrant.")
        } else {
            item("warrant", 600, "Bravek waves a warrant at you, but you don't have room to take it.")
        }
    }

    suspend fun Player.completed() {
        npc<Happy>("Thanks again for the hangover cure.")
        player<Happy>("Not a problem, happy to help out.")
        npc<Happy>("I'm just having a little drop of whisky, then I'll feel really good.")
    }
}
