package content.area.misthalin.draynor_village

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.entity.sound.sound
import content.quest.quest
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

npcOperate("Talk-to", "jail_guard_joe") {
    when (player.quest("prince_ali_rescue")) {
        "guard" -> {
            choice {
                fancyABeer()
                guardLife()
                guardDreams()
                option<Talk>("I had better leave, I don't want trouble.")
            }
        }
        "joe_beer" -> anotherBeer()
        "joe_beers", "keli_tied_up", "prince_ali_disguise" -> {
            npc<Drunk>("Halt! Who goes there?")
            player<Happy>("Hello friend. I'm just here to rescue the Prince, if thats okay?")
            npc<Drunk>("Thatsh a funny joke. You are lucky I'm shober. Go in peace, friend.")
        }
        "completed" -> {
            npc<Talk>("Halt! Who goes there?")
            player<Happy>("Hi friend, I am just checking out things here.")
            npc<Sad>("The Prince got away, I am in trouble. I better not talk to you, they are not sure I was drunk.")
            player<Talk>("I won't say anything, your secret is safe with me.")
        }
        else -> {
            player<Quiz>("Hi. Who are you guarding here?")
            npc<Angry>("Can't say. It's all very secret. You should get out of here. I am not supposed to talk while I guard.")
        }
    }
}


itemOnNPCOperate("beer", "jail_guard_joe") {
    when (player.quest("prince_ali_rescue")) {
        "guard" -> {
            player<Happy>("I have some beer here. Fancy one?")
            beer()
        }
        "joe_beer" -> anotherBeer()
        else -> player<Talk>("I don't see any need to give the guard my beer. I'll keep it for myself.")
    }
}

suspend fun TargetInteraction<Player, NPC>.beer() {
    npc<Happy>("Ah, that would be lovely. Only one though, just to wet my throat.")
    player<Talk>("Of course. It must be tough being here without a drink.")
    player["prince_ali_rescue"] = "joe_beer"
    player.inventory.remove("beer")
    player.sound("drink")
    // TODO what if don't have 3 beers?
    statement("You hand a beer to the guard. He drinks it in seconds.")
    npc<Happy>("That was perfect! I can't thank you enough.")
    player<Quiz>("How are you? Still ok? Not too drunk?")
    anotherBeer()
}

suspend fun TargetInteraction<Player, NPC>.anotherBeer() {
    player<Happy>("Would you care for another beer, my friend?")
    npc<RollEyes>("I'd better not. I don't want to be drunk on duty.")
    player<Happy>("Here, just keep these for later. I hate to see a thirsty guard.")
    player["prince_ali_rescue"] = "joe_beers"
    player.inventory.remove("beer", 2)
    player.sound("drink")
    items("beer", "beer", "You hand two more beers to the guard. He takes a sip of one, and then he quickly drinks them both.")
    npc<Drunk>("Franksh! That wash jusht what I need to shtay on guard. No more beersh, I don't want to get drunk.")
}

fun ChoiceBuilder<NPCOption<Player>>.guardLife() {
    option<Talk>("Tell me about the life of a guard.") {
        npc<RollEyes>("Well, the hours are good, but most of those hours are a drag.")
        npc<Upset>("Sometimes I wonder if I should have spent more time learning when I was a young boy. Maybe I wouldn't be here now, scared of Keli.")
        choice {
            guardDreams()
            option<Talk>("I'd better go.")
        }
    }
}

fun ChoiceBuilder<NPCOption<Player>>.guardDreams() {
    option<Talk>("What did you want to be when you were a boy?") {
        npc<RollEyes>("Well, I loved to sit by the lake, with my toes in the water. I'd shoot the fish with my bow and arrow.")
        player<Uncertain>("That's a strange hobby for a boy.")
        npc<Happy>("It kept us from goblin hunting, which was what most boys did.")
        npc<Angry>("Hang on... Why do you ask? What do you want?")
        choice {
            option<Happy>("Hey, chill out. I won't cause you trouble.") {
                npc<Talk>("Sorry, it's hard to relax when I'm on duty. Stress of the job, and all.")
                player<Quiz>("So why do you do it?")
                npc<Talk>("There's good money in it, and some of the shouting I rather like.")
                npc<Angry>("RESISTANCE IS USELESS!")
                choice {
                    option<Happy>("So what do you buy with your great wages?") {
                        npc<RollEyes>("Really, after working here, there's only time for a drink or three. All us guards go to the same pub and drink ourselves stupid.")
                        npc<Happy>("It's what I enjoy these days. I can't resist the sight of a really cold beer.")
                        choice {
                            fancyABeer()
                            guardLife()
                            guardDreams()
                            betterGo()
                        }
                    }
                    guardLife()
                    option("Would you be interested in making a little more money?") {
                        // TODO?
                    }
                    betterGo()
                }
            }
            guardLife()
            betterGo()
        }
    }
}

fun ChoiceBuilder<NPCOption<Player>>.betterGo() {
    option<Talk>("I'd better go.") {
        npc<Talk>("Thanks, I appreciate that. Talking on duty can be punished by having your mouth stitched up. These are tough people, make no mistake.")
    }
}

fun ChoiceBuilder<NPCOption<Player>>.fancyABeer() {
    option<Happy>("I have some beer here, fancy one?") {
        beer()
    }
}