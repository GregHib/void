package content.area.kharidian_desert

import content.entity.player.dialogue.Chuckle
import content.entity.player.dialogue.EvilLaugh
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.None
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.RollEyes
import content.entity.player.dialogue.Surprised
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.Uncertain
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.closeTabs
import content.quest.openTabs
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class RugMerchant : Script {
    init {
        npcOperate("Talk-to", "rug_merchant_*") { (target) ->
            player<Talk>("Hello.")
            npc<Talk>("Greetings, desert traveler. Do you require the services of Ali Morrisane's flying carpet fleet?")
            choice {
                option<Happy>("Yes please.") {
                    travel(target)
                }
                aboutAli(target)
                explainFleet(target)
                questions(target)
                noThanks()
            }
        }

        npcOperate("Travel", "rug_merchant_*") { (target) ->
            choice("Where do you wish to travel?") {
                val current = target.id.removePrefix("rug_merchant_")
                when (target.id) {
                    "rug_merchant_shantay_pass" -> {
                        if (questCompleted("the_golem")) {
                            option("I want to travel to Uzer.") { travel(current, "uzer", skip = true) }
                        }
                        option("I want to travel to the Bedabin camp.") { travel(current, "bedabin_camp", skip = true) }
                        option("I want to travel to Pollnivneach.") { travel(current, "pollnivneach_north", skip = true) }
                    }
                    "rug_merchant_uzer", "rug_merchant_bedabin", "rug_merchant_north_pollnivneach" -> option("Shantay Pass") { travel(current, "shantay_pass", skip = true) }
                    "rug_merchant_south_pollnivneach" -> {
                        if (questCompleted("icthlarins_little_helper")) {
                            option("Sophanem") { travel(current, "sophanem", skip = true) }
                            option("Menaphos") { travel(current, "menaphos", skip = true) }
                        }
                        option("Nardah") { travel(current, "nardah", skip = true) }
                    }
                    "rug_merchant_sophanem", "rug_merchant_menaphos", "rug_merchant_nardah" -> option("Pollnivneach") { travel(current, "pollnivneach_south", skip = true) }
                }
                option("I don't want to travel to any of those places.")
            }
        }

        npcOperate("Talk-to", "magic_carpet_monkey") {
            player<Talk>("Who's a cute little monkey?")
            if (equipped(EquipSlot.Ammo).id != "mspeak_amulet") {
                npc<Talk>("Ukkuk oook! Eeek aka, ahh aka gonk.")
                return@npcOperate
            }
            npc<Talk>("Who's an ugly human? Give me a banana!")
            player<Talk>("What's up with you?")
            npc<Talk>("Stupid human! Give monkey a banana!")
            if (!inventory.contains("banana")) {
                player<Talk>("Sorry monkey, I don't have any bananas.")
                npc<Talk>("Aghhh. You're a rubbish human, get monkey a banana now.")
                return@npcOperate
            }
            choice {
                option("Give the monkey a banana.") {
                    giveBanana()
                }
                option("Don't give it a banana.") {
                    player<Talk>("I'll not give you a banana until you learn manners.")
                }
            }
        }

        itemOnNPCOperate("banana", "magic_carpet_monkey") {
            giveBanana()
        }
    }

    private suspend fun Player.travel(from: String, location: String, skip: Boolean = false) {
        val price = price(skip)
        if (!inventory.remove("coins", price)) {
            player<Talk>("I don't have enough money with me.")
            npc<Talk>("Looks like you're walking then.")
            return
        }
        if (from == "shantay_pass") {
            startShantayCarpet()
        }
    }

    private fun Player.beginTravel() {
        closeTabs()
        anim("magic_carpet_takeoff")
        gfx("magic_carpet_takeoff")
        set("magic_carpet_var", true)
        sound("carpet_rise")
    }

    private fun Player.endTravel() {
        openTabs()
        set("magic_carpet_var", false)
        clearAnim()
        clearCamera()
    }

    private suspend fun Player.startShantayCarpet() {
        moveCamera(Tile(3302, 3110), height = 500, constantSpeed = 100, variableSpeed = 100)
        turnCamera(Tile(3308, 3110), height = 100, constantSpeed = 100, variableSpeed = 100)
        walkToDelay(Tile(3309, 3110))
        jingle("magic_carpet_travel")
        clearWatch()
        clear("face_entity")
        walkOverDelay(Tile(3308, 3110))
        face(Direction.SOUTH)
        delay(2)
        beginTravel()
        delay(3)
        moveCamera(Tile(3308, 3114), height = 500, constantSpeed = 10, variableSpeed = 10)
        turnCamera(Tile(3308, 3110), height = 300, constantSpeed = 10, variableSpeed = 10)
//        face(Direction.SOUTH)
        delay(2)
        gfx("magic_carpet_fly")
//        clearCamera()

//        walkOverDelay(Tile(3308, 3106))
        // TODO what happens if you disconnect half way through?
        delay(10)
        endTravel()
    }

    private suspend fun Player.price(skip: Boolean): Int {
        var price = 200
        if (questCompleted("rogue_trader")) {
            price = 100
            if (!skip) {
                npc<Talk>("There is a fare for this service you know - normally it's 200 gold per journey, but for you, I'll let you go for 100.")
            }
        }
        if (equipped(EquipSlot.Ring).id == "ring_of_charos_a") {
            price = if (price == 100) 75 else 100
        }
        return price
    }

    private suspend fun Player.giveBanana() {
        if (!inventory.remove("banana")) {
            return
        }
        inc("gifted_bananas")
        npc<EvilLaugh>("Ha ha! Smelly human gave monkey a banana.")
        player<Talk>("Wow you're one nasty piece of work. Have you ever heard of gratitude?")
        npc<Talk>("Hey baldy, give monkey another banana!")
        player<Talk>("Just because I'm not covered in fur doesn't make me bald, you cheeky monkey.")
        npc<Talk>("Monkey wants another banana now! Give me, give me!")
        player<Talk>("Look I've had it with you, you little degenerate.")
    }

    private fun ChoiceOption.aboutAli(target: NPC) {
        option<Talk>("Tell me about Ali Morrisane.") {
            npc<Surprised>("What, you haven't heard of Ali M? Possibly the greatest salesman of the Kharidian empire if not all Gielinor?")
            if (questCompleted("the_feud")) {
                player<Talk>("Ah yes I remember him now, I went on a wild goose chase looking for his nephew.")
                npc<Happy>("Ha! No doubt old Ali M instigated the whole thing.")
                player<Talk>("I had a bit of fun though, the whole job was quite diverting.")
                npc<Talk>("There's never a dull moment around that man, he's always looking for a way to make a quick coin or two.")
            } else {
                // TODO correct expressions
                player<Talk>("I can't say that I have, but he must be the ambitious type to try and set up his own airline.")
                npc<Talk>("You know something, I reckon that he's trying to take on those gnomes at their own game and I'd bet good money that he'll probably win.")
                player<Talk>("Hah? I think you've gone and lost me now.")
                npc<Talk>("You know those small little guys, not the dwarves now mind.")
                player<Talk>("Ya... gnomes, I'm with you that far.")
                npc<Talk>("Well they have already established an Airline, Gnome Air...")
                player<Talk>("Go on...")
                npc<Talk>("Anyway I think that Ali M's setup here will prove really successful and maybe once we're properly established we could try compete with those gnomes.")
                player<Talk>("I'll watch this space.")
            }
            choice {
                travelOption(target)
                explainFleet(target)
                questions(target)
                thanks()
            }
        }
    }

    private fun ChoiceOption.travelOption(target: NPC) {
        option<Happy>("I want to travel by magic carpet.") {
            travel(target)
        }
    }

    private suspend fun Player.travel(target: NPC) {
        // TODO didn't check dialogues without quest requirements
        val current = target.id.removePrefix("rug_merchant_")
        when (target.id) {
            "rug_merchant_south_pollnivneach" -> {
                npc<Talk>("From here you can travel to Nardah and the Menaphite cities of Sophanem and Menaphos.")
                choice {
                    option<Talk>("I want to travel to Nardah.") {
                        travel(current, "nardah")
                    }
                    if (questCompleted("icthlarins_little_helper")) {
                        option<Talk>("I want to travel to Menaphos.") {
                            travel(current, "menaphos")
                        }
                        option<Talk>("I want to travel to Sophanem.") {
                            travel(current, "sophanem")
                        }
                    }
                    option<RollEyes>("I don't want to travel to any of those places.") {
                        npc<Talk>("Fair enough, magic carpet travel isn't for everyone. Enjoy the walk.")
                    }
                }
            }
            "rug_merchant_sophanem" -> {
                npc<Talk>("The carpets here will take you to the south of Pollnivneach. Do you want to take a lift?")
                choice {
                    option<Talk>("Pollnivneach will do.") {
                        travel(current, "pollnivneach_south")
                    }
                    option<RollEyes>("I don't want to travel there.") {
                        npc<Talk>("Fair enough, magic carpet travel isn't for everyone. Enjoy the walk.")
                    }
                }
            }
            "rug_merchant_nardah" -> {
                npc<Talk>("The carpets here will take you to the south of Pollnivneach.")
                choice {
                    option<Talk>("Let's go then.") {
                        travel(current, "pollnivneach_south")
                    }
                    option<RollEyes>("I don't want to travel there.") {
                        npc<Talk>("Fair enough, magic carpet travel isn't for everyone. Enjoy the walk.")
                    }
                }
            }
            "rug_merchant_north_pollnivneach" -> {
                npc<Talk>("From here you can travel to the Shantay Pass - the Southern gate of Al Kharid.")
                choice {
                    option<Talk>("Take me to the Pass then.") {
                        travel(current, "shantay_pass")
                    }
                    option<RollEyes>("I don't want to travel there.") {
                        npc<Talk>("Fair enough, magic carpet travel isn't for everyone. Enjoy the walk.")
                    }
                }
            }
            "rug_merchant_uzer" -> {
                npc<Talk>("You can travel from here back to the Shantay Pass.")
                choice {
                    option<Talk>("That sounds good, take me there.") {
                        travel(current, "shantay_pass")
                    }
                    option<RollEyes>("I don't want to travel there.") {
                        npc<Talk>("Fair enough, magic carpet travel isn't for everyone. Enjoy the walk.")
                    }
                }
            }
            "rug_merchant_bedabin" -> {
                npc<Talk>("From here you can travel to the Shantay Pass.")
                choice {
                    option<Talk>("Take me there.") {
                        travel(current, "shantay_pass")
                    }
                    option<RollEyes>("I don't want to travel there.") {
                        npc<Talk>("Fair enough, magic carpet travel isn't for everyone. Enjoy the walk.")
                    }
                }
            }
            "rug_merchant_shantay_pass" -> {
                npc<Talk>("From here you can travel to Uzer, to the Bedabin camp or to the North of Pollnivneach.")
                npc<Talk>("The second major carpet hub station, to the south of Pollnivneach is in easy walking distance from there.")
                choice {
                    if (questCompleted("the_golem")) {
                        option("I want to travel to Uzer.") {
                            travel(current, "uzer")
                        }
                    }
                    option<Talk>("I want to travel to the Bedabin camp.") {
                        travel(current, "bedabin_camp")
                    }
                    option<Talk>("I want to travel to Pollnivneach.") {
                        travel(current, "pollnivneach")
                    }
                }
            }
        }
    }

    private fun ChoiceOption.noThanks() {
        option("No thanks.") {
            npc<Talk>("Come back anytime.")
        }
    }

    private fun ChoiceOption.questions(target: NPC) {
        option<Talk>("I have some questions.") {
            npc<Talk>("I'll try help you as much as I can.")
            choice {
                option<Talk>("What are you doing here?") {
                    when (target.id) {
                        "rug_merchant_shantay_pass" -> npc<Talk>("Well this is a good position for desert traffic. Shantay seems to have a nice little money spinner setup, but I reckon, this could turn out even better.")
                        "rug_merchant_north_pollnivneach" -> {
                            npc<Talk>("Well Pollnivneach is the ideal location for setting up a carpet station.")
                            player<Talk>("Why's that?")
                            npc<Talk>("You see it's located halfway between Al Kharid, and the Menaphite cities and close enough to Nardah too, so we get more than enough traffic to keep the business running.")
                        }
                        "rug_merchant_south_pollnivneach" -> {
                            player<Talk>("What are you doing here?")
                            npc<Talk>("I work here renting out magic carpets. I'm from Pollnivneach so it is a handy job, I don't have to commute too far to work every day.")
                            player<Talk>("So I suppose you're called Ali then.")
                            npc<Talk>("Not the most remarkable of names, not that it matters, you see everyone in town knows me as Flash.")
                            player<Talk>("Really?")
                            npc<None>("No.")
                            player<None>("........")
                            npc<None>(".........")
                            player<Talk>("Oh right.")
                        }
                        "rug_merchant_sophanem" -> {
                            player<Talk>("What are you doing here?")
                            npc<Chuckle>("I look after the carpet station here. The place is a bit dead though. Ha! I'm just too much.")
                            player<Uncertain>("What?")
                            npc<Talk>("You know, Sophanem, city of the dead and all that?")
                            player<None>("...")
                            npc<Talk>("Aww come on, the joke wasn't that bad.")
                            player<None>("...")
                        }
                        "rug_merchant_nardah" -> {
                            npc<Talk>("Well I'd preferred to have been running one of the carpet stations at a hub such as Pollnivneach. I was a bit slow off the mark to get that gig though. Still business in Nardah isn't bad for a terminal. At least")
                            npc<Talk>("people come here for the bank and to see the herbalist.")
                        }
                        "rug_merchant_uzer" -> {
                            npc<Talk>("You mightn't realise it, but this is quite a busy station.")
                            player<Talk>("Who would want to come here?")
                            npc<Talk>("Well you for one, and besides that we get quite a few archaeologists from the dig site passing through to examine the golem.")
                        }
                        "rug_merchant_attendant" -> {
                            npc<Talk>("Until recently I was looking after one of the busiest carpet stations. But that all changed since Menaphos closed its gates.")
                            npc<Talk>("Right now I have to fill my day trying to come up with reasons why Ali M should keep this place open.")
                            player<Talk>("So have you come up with any good ideas then?")
                            npc<Talk>("Not really. The only reason I have come up with to date is that by keeping the station open, people will become familiar with it.")
                            npc<Talk>("Maybe once Menaphos opens her gates again, the station will make a fortune once more.")
                        }
                        "rug_merchant_bedabin" -> npc<Talk>("Well besides the obvious - looking after this station, I'm trying to figure out how these Tentis manage to cultivate such delicious pineapples.")
                    }
                    choice {
                        travelOption(target)
                        aboutAli(target)
                        explainFleet(target)
                        questions(target)
                        thanks()
                    }
                }
                monkey(target)
                hat(target)
            }
        }
    }

    private fun ChoiceOption.monkey(target: NPC) {
        option<Talk>("Is that your pet monkey nearby?") {
            npc<Talk>("He's his own monkey, he does whatever suits him, a total nuisance.")
            player<Talk>("I detect a degree of hostility being directed towards the monkey.")
            npc<Talk>("I shouldn't say this really, but sometimes I begin to question some of Ali Morrisane's ideas, he says that associating a monkey with any product will increase sales. I just don't know, what will be next?")
            player<Quiz>("Frogs?")
            npc<Talk>("I doubt it, amphibians don't have the same cutesy factor as monkeys.")
            player<Talk>("I'm confused. I thought you didn't like monkeys.")
            npc<Talk>("I don't dislike monkeys, it's just that monkey. I don't know, I might just be paranoid but I think he's... well... evil.")
            player<Talk>("Hmmm... Interesting.")
            player<Talk>("Hang on a minute, I think I have an amulet of monkeyspeak stored somewhere. Perhaps I could get it and see what it has to say for itself.")
            npc<Talk>("Would you? It sometimes gives me these stares... <blue>~A visible shiver runs down his back.")
            npc<Talk>("I would be really grateful if you could get that monkey off my back.")
            choice {
                travelOption(target)
                aboutAli(target)
                explainFleet(target)
                questions(target)
                thanks()
            }
        }
    }

    private fun ChoiceOption.hat(target: NPC) {
        option<Talk>("Where did you get that hat?") {
            npc<Talk>("My fez? I got it from Ali Morrisane, it's a uniform of sorts, apparently it makes us more visible, but I'm not too sure about it.")
            player<Talk>("Well it is quite distinctive.")
            npc<Talk>("Do you like it? I haven't really made my mind up about it yet. You see it's not all that practical for desert conditions.")
            player<Talk>("How so?")
            npc<Talk>("Well it doesn't keep the sun out of my eyes and after a while sitting out in the desert they really begin to burn.")
            choice {
                travelOption(target)
                aboutAli(target)
                explainFleet(target)
                questions(target)
                thanks()
            }
        }
    }

    private fun ChoiceOption.thanks() {
        option("Thanks, I'm done here.") {
            npc<Talk>("Come back anytime.")
        }
    }

    private fun ChoiceOption.explainFleet(target: NPC) {
        option("Tell me about this magic carpet fleet.") {
            player<Talk>("Tell me about this Magic Carpet fleet.")
            npc<Talk>("The latest idea from the great Ali Morrisane. Desert travel will never be the same again.")
            player<Talk>("So how does it work?")
            npc<Talk>("The carpet or the whole enterprise?")
            choice {
                option<Talk>("Tell me about how the carpet works.") {
                    npc<Talk>("I'm not really too sure, it's just an enchanted rug really, made out of special Ugthanki hair. It flies to whatever destination its owner commands.")
                }
                option("Tell me about the enterprise then.") {
                    npc<Talk>("It's quite simple really, Ali Morrisane has hired myself and a few others to set up carpet stations at some of the desert's more populated places and run flights between the stations.")
                    player<Talk>("So why has he limited the service to just the desert?")
                    npc<Talk>("I don't think Ali is prepared to take on Gnome Air just yet, their gliders are much faster than our carpets, besides that I think we are in the short haul business, something that would only work in harsh conditions like")
                    npc<Talk>("the desert.")
                    player<Talk>("Why is that?")
                    npc<Talk>("I suppose because people would just walk. Getting lost isn't too much of a problem generally, but it's a different matter when you're in the middle of the Kharidian desert with a dry waterskin and no idea")
                    npc<Talk>("which direction to go in.")
                    player<Talk>("You're right I guess. How's the business going then?")
                    npc<Talk>("Not too bad, the hubs are generally quite busy. But the stations in Uzer and the Bedabin camp could do with a bit more traffic.")
                    player<Talk>("A growth market I guess.")
                    choice {
                        travelOption(target)
                        aboutAli(target)
                        questions(target)
                        thanks()
                    }
                }
            }
        }
    }
}