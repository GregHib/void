package content.area.kandarin.tree_gnome_stronghold

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

class CaptainErrdo : Script {

    init {
        npcOperate("Talk-to", "captain_errdo,captain_bleemadge,captain_dalbur,captain_klemfoodle") { (target) ->
            if (!questCompleted("the_grand_tree")) {
                npc<Neutral>("Welcome to Gnome Air!")
                choice {
                    whatsGnomeAir(target)
                    whereCanYouTakeMe(target)
                    oneWayToVarrock(target)
                    leaveYouToIt()
                }
                return@npcOperate
            }
            choice {
                takeMe(target)
                whyAreGlidersBetter(target)
                if (target.id == "captain_dalbur") {
                    option<Neutral>("What do you think of Ali Morrisane?") {
                        npc<Neutral>("Oh, he's always up to something. Like that magic carpet business.")
                        player<Quiz>("What do you think of his business?")
                        npc<Angry>("Like I said, we'll not be happy if it starts up.")
                        npc<Happy>("Of course, it's likely to flop and not get off the ground, if you see what I mean.")
                        choice {
                            takeMe(target)
                            whyAreGlidersBetter(target)
                            nothing()
                        }
                    }
                }
                nothing()
            }
        }

        npcOperate("Glider,captain_errdo,captain_bleemadge,captain_dalbur,captain_klemfoodle") { (target) ->
            if (!questCompleted("the_grand_tree")) {
                takeMe(target)
                return@npcOperate
            }
            location(this, target)
            open("glider_map")
        }

        npcOperate("Talk-to", "captain_errdo_crashed") {
            if (questCompleted("the_grand_tree")) {
                embarrassing()
            } else {
                player<Quiz>("What happened here?")
                npc<Shifty>("Call it 'creative landing'.")
            }
        }
    }

    fun ChoiceOption.takeMe(target: NPC) {
        option<Quiz>("Can you take me on the glider?") {
            npc<Happy>("Of course!")
            location(this, target)
            open("glider_map")
        }
    }

    fun location(player: Player, npc: NPC) {
        player["glider_location"] = when (npc.id) {
            "captain_bleemadge" -> "sindarpos"
            "captain_errdo" -> "ta_quir_priw"
            "captain_dalbur" -> "kar_hewo"
            "captain_klemfoodle" -> "gandius"
            else -> return
        }
    }

    fun ChoiceOption.nothing() {
        option<Confused>("Sorry, I don't want anything now.")
    }

    fun ChoiceOption.whyAreGlidersBetter(target: NPC) {
        option<Neutral>("Why are gliders better than other transport?") {
            npc<Happy>("Oh we have a whole network! It's wonderful for getting to hard to reach places.")
            npc<Happy>("There are so many places where your teleports cannot reach!")
            player<Happy>("How did you all manage to build such an established network?")
            npc<Neutral>("I think you'll find that is a gnome trade secret!")
            choice {
                takeMe(target)
                nothing()
            }
        }
    }

    suspend fun Player.embarrassing() {
        npc<Confused>("Ah, how embarrassing.")
        player<Quiz>("What happened?")
        npc<Neutral>("A bit of a technical hitch with the landing gear. I won't be able to fly you anywhere, sorry.")
    }

    fun ChoiceOption.whatsGnomeAir(target: NPC) {
        option<Quiz>("What's Gnome Air?") {
            npc<Happy>("Gnome Air is the finest airline in Gielinor!")
            npc<Confused>("Well...it's the only real airline in Gielinor.")
            npc<Laugh>("Ha!")
            player<Neutral>("What do you mean by real airline?")
            npc<Laugh>("Well there's a dodgy magic carpet operation in the desert, I doubt it will ever take off... Ha!")
            if (target.id == "captain_dalbur") {
                player<Quiz>("What magic carpet business?")
                npc<Neutral>("That fellow over by that stall says he's setting up a magic carpet business. He said his name was Ali Morrisane.")
                //                player["desert"] = 2 // https://chisel.weirdgloop.org/varbs/display?varplayer=599
                npc<Angry>("If he ever gets it set up, us gnomes won't be happy with him. Especially not King Narnode!")
            }
            choice {
                whereCanYouTakeMe(target)
                oneWayToVarrock(target)
                leaveYouToIt()
            }
        }
    }

    fun ChoiceOption.whereCanYouTakeMe(target: NPC) {
        option("Where can you take me?") {
            takeMe(target)
        }
    }

    fun ChoiceOption.oneWayToVarrock(target: NPC) {
        option<Quiz>("How much for one-way to Varrock?") {
            npc<Sad>("I can't take you anywhere.")
            player<Sad>("How come?")
            npc<Sad>("I would, but Glough has told me not to take humans on Gnome Air.")
            choice {
                whatsGnomeAir(target)
                whereCanYouTakeMe(target)
                leaveYouToIt()
            }
        }
    }

    fun ChoiceOption.leaveYouToIt() {
        option<Confused>("I'll leave you to it.")
    }

    suspend fun Player.takeMe(target: NPC) {
        player<Quiz>("Where can you take me?")
        npc<Sad>("Glough has ordered that I only take gnomes on Gnome Air.")
        choice {
            whatsGnomeAir(target)
            oneWayToVarrock(target)
            leaveYouToIt()
        }
    }
}
