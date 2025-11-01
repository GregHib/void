package content.area.kandarin.tree_gnome_stronghold

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.SuspendableContext

class CaptainErrdo : Script {

    init {
        npcOperate("Talk-to", "captain_errdo,captain_bleemadge,captain_dalbur,captain_klemfoodle") { (target) ->
            if (!questCompleted("the_grand_tree")) {
                npc<Talk>("Welcome to Gnome Air!")
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
                    option<Talk>("What do you think of Ali Morrisane?") {
                        npc<Talk>("Oh, he's always up to something. Like that magic carpet business.")
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

    fun ChoiceBuilder2.takeMe(target: NPC) {
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

    fun ChoiceBuilder2.nothing() {
        option<Uncertain>("Sorry, I don't want anything now.")
    }

    fun ChoiceBuilder2.whyAreGlidersBetter(target: NPC) {
        option<Talk>("Why are gliders better than other transport?") {
            npc<Happy>("Oh we have a whole network! It's wonderful for getting to hard to reach places.")
            npc<Happy>("There are so many places where your teleports cannot reach!")
            player<Happy>("How did you all manage to build such an established network?")
            npc<Talk>("I think you'll find that is a gnome trade secret!")
            choice {
                takeMe(target)
                nothing()
            }
        }
    }

    suspend fun Player.embarrassing() {
        npc<Uncertain>("Ah, how embarrassing.")
        player<Quiz>("What happened?")
        npc<Talk>("A bit of a technical hitch with the landing gear. I won't be able to fly you anywhere, sorry.")
    }

    fun ChoiceBuilder2.whatsGnomeAir(target: NPC) {
        option<Quiz>("What's Gnome Air?") {
            npc<Happy>("Gnome Air is the finest airline in Gielinor!")
            npc<Uncertain>("Well...it's the only real airline in Gielinor.")
            npc<Chuckle>("Ha!")
            player<Talk>("What do you mean by real airline?")
            npc<Chuckle>("Well there's a dodgy magic carpet operation in the desert, I doubt it will ever take off... Ha!")
            if (target.id == "captain_dalbur") {
                player<Quiz>("What magic carpet business?")
                npc<Talk>("That fellow over by that stall says he's setting up a magic carpet business. He said his name was Ali Morrisane.")
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

    fun ChoiceBuilder2.whereCanYouTakeMe(target: NPC) {
        option("Where can you take me?") {
            takeMe(target)
        }
    }

    fun ChoiceBuilder2.oneWayToVarrock(target: NPC) {
        option<Quiz>("How much for one-way to Varrock?") {
            npc<Upset>("I can't take you anywhere.")
            player<Upset>("How come?")
            npc<Upset>("I would, but Glough has told me not to take humans on Gnome Air.")
            choice {
                whatsGnomeAir(target)
                whereCanYouTakeMe(target)
                leaveYouToIt()
            }
        }
    }

    fun ChoiceBuilder2.leaveYouToIt() {
        option<Uncertain>("I'll leave you to it.")
    }

    suspend fun Player.takeMe(target: NPC) {
        player<Quiz>("Where can you take me?")
        npc<Upset>("Glough has ordered that I only take gnomes on Gnome Air.")
        choice {
            whatsGnomeAir(target)
            oneWayToVarrock(target)
            leaveYouToIt()
        }
    }
}
