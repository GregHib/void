package content.area.misthalin.draynor_village

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Joe : Script {

    init {
        npcOperate("Talk-to", "jail_guard_joe") {
            when (quest("prince_ali_rescue")) {
                "guard" -> {
                    choice {
                        if (inventory.contains("beer")) {
                            fancyABeer()
                        }
                        guardLife()
                        guardDreams()
                        option<Neutral>("I had better leave, I don't want trouble.")
                    }
                }
                "joe_beer" -> anotherBeer()
                "joe_beers", "keli_tied_up" -> {
                    npc<Drunk>("Halt! Who goes there?")
                    player<Happy>("Hello friend. I'm just here to rescue the Prince, if thats okay?")
                    npc<Drunk>("Thatsh a funny joke. You are lucky I'm shober. Go in peace, friend.")
                }
                "prince_ali_disguise" -> {
                    npc<Drunk>("Did yoush say something about shome Prince?")
                    player<Confused>("No.")
                    npc<Drunk>("Oh... okay.")
                }
                "completed" -> {
                    npc<Neutral>("Halt! Who goes there?")
                    player<Happy>("Hi friend, I am just checking out things here.")
                    npc<Disheartened>("The Prince got away, I am in trouble. I better not talk to you, they are not sure I was drunk.")
                    player<Neutral>("I won't say anything, your secret is safe with me.")
                }
                else -> {
                    player<Quiz>("Hi. Who are you guarding here?")
                    npc<Angry>("Can't say. It's all very secret. You should get out of here. I am not supposed to talk while I guard.")
                }
            }
        }

        itemOnNPCOperate("beer", "jail_guard_joe") {
            when (quest("prince_ali_rescue")) {
                "guard" -> {
                    player<Happy>("I have some beer here. Fancy one?")
                    beer()
                }
                "joe_beer" -> anotherBeer()
                else -> player<Neutral>("I don't see any need to give the guard my beer. I'll keep it for myself.")
            }
        }
    }

    fun ChoiceOption.fancyABeer() {
        option<Happy>("I have some beer here, fancy one?") {
            beer()
        }
    }

    suspend fun Player.beer() {
        npc<Happy>("Ah, that would be lovely. Only one though, just to wet my throat.")
        player<Neutral>("Of course. It must be tough being here without a drink.")
        set("prince_ali_rescue", "joe_beer")
        inventory.remove("beer")
        sound("drink")
        statement("You hand a beer to the guard. He drinks it in seconds.")
        npc<Happy>("That was perfect! I can't thank you enough.")
        anotherBeer()
    }

    suspend fun Player.anotherBeer() {
        player<Quiz>("How are you? Still ok? Not too drunk?")
        if (!inventory.contains("beer", 2)) {
            npc<Neutral>("No, I don't get drunk from only one drink. I reckon I'd need at least two more for that. Still, thanks for the beer.")
            return
        }
        player<Happy>("Would you care for another beer, my friend?")
        npc<Bored>("I'd better not. I don't want to be drunk on duty.")
        player<Happy>("Here, just keep these for later. I hate to see a thirsty guard.")
        set("prince_ali_rescue", "joe_beers")
        inventory.remove("beer", 2)
        sound("drink")
        items("beer", "beer", "You hand two more beers to the guard. He takes a sip of one, and then he quickly drinks them both.")
        npc<Drunk>("Franksh! That wash jusht what I need to shtay on guard. No more beersh, I don't want to get drunk.")
    }

    fun ChoiceOption.guardLife() {
        option<Neutral>("Tell me about the life of a guard.") {
            npc<Bored>("Well, the hours are good, but most of those hours are a drag.")
            npc<Sad>("Sometimes I wonder if I should have spent more time learning when I was a young boy. Maybe I wouldn't be here now, scared of Keli.")
            choice {
                guardDreams()
                betterGo()
            }
        }
    }

    fun ChoiceOption.guardDreams() {
        option<Neutral>("What did you want to be when you were a boy?") {
            npc<Bored>("Well, I loved to sit by the lake, with my toes in the water. I'd shoot the fish with my bow and arrow.")
            player<Confused>("That's a strange hobby for a boy.")
            npc<Happy>("It kept us from goblin hunting, which was what most boys did.")
            npc<Angry>("Hang on... Why do you ask? What do you want?")
            choice {
                option<Happy>("Hey, chill out. I won't cause you trouble.") {
                    npc<Neutral>("Sorry, it's hard to relax when I'm on duty. Stress of the job, and all.")
                    player<Quiz>("So why do you do it?")
                    npc<Neutral>("There's good money in it, and some of the shouting I rather like.")
                    npc<Angry>("RESISTANCE IS USELESS!")
                    choice {
                        option<Happy>("So what do you buy with your great wages?") {
                            npc<Bored>("Really, after working here, there's only time for a drink or three. All us guards go to the same pub and drink ourselves stupid.")
                            npc<Happy>("It's what I enjoy these days. I can't resist the sight of a really cold beer.")
                            choice {
                                if (inventory.contains("beer")) {
                                    fancyABeer()
                                }
                                guardLife()
                                guardDreams()
                                betterGo()
                            }
                        }
                        guardLife()
                        option<Happy>("Would you be interested in making a little more money?") {
                            npc<Angry>("What? Are you trying to bribe me? I may not be a great guard, but I am loyal. How dare you try to bribe me!")
                            player<Shock>("No, no, you've got the wrong idea, totally. I just wondered if you wanted some part-time bodyguard work.")
                            npc<Neutral>("Oh... sorry. No, I don't need money. As long as you were not offering me a bribe.")
                            choice {
                                guardLife()
                                guardDreams()
                                betterGo()
                            }
                        }
                        betterGo()
                    }
                }
                guardLife()
                betterGo()
            }
        }
    }

    fun ChoiceOption.betterGo() {
        option<Neutral>("I'd better go.") {
            npc<Neutral>("Thanks, I appreciate that. Talking on duty can be punished by having your mouth stitched up. These are tough people, make no mistake.")
        }
    }
}
