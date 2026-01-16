package content.area.kharidian_desert

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.effect.energy.runEnergy
import content.quest.closeTabs
import content.quest.openTabs
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.LogoutBehaviour
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class RugMerchant : Script {

    init {
        npcOperate("Talk-to", "rug_merchant_*") { (target) ->
            player<Neutral>("Hello.")
            npc<Neutral>("Greetings, desert traveler. Do you require the services of Ali Morrisane's flying carpet fleet?")
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
                        option("I want to travel to Pollnivneach.") { travel(current, "north_pollnivneach", skip = true) }
                        option("I don't want to travel to any of those places.")
                    }
                    "rug_merchant_uzer", "rug_merchant_bedabin_camp", "rug_merchant_north_pollnivneach" -> {
                        option("I want to travel to Shantay Pass.") { travel(current, "shantay_pass", skip = true) }
                        option("Actually, I've changed my mind.")
                    }
                    "rug_merchant_south_pollnivneach" -> {
                        option("I want to travel to Nardah.") { travel(current, "nardah", skip = true) }
                        if (questCompleted("icthlarins_little_helper")) {
                            option("I want to travel to Sophanem.") { travel(current, "sophanem", skip = true) }
                            option("I want to travel to Menaphos.") { travel(current, "menaphos", skip = true) }
                            option("I don't want to travel to any of those places.")
                        } else {
                            option("Actually, I've changed my mind.")
                        }
                    }
                    "rug_merchant_sophanem", "rug_merchant_attendant", "rug_merchant_nardah" -> {
                        option("I want to travel to Pollnivneach.") { travel(current, "south_pollnivneach", skip = true) }
                        option("Actually, I've changed my mind.")
                    }
                }
            }
        }

        npcOperate("Talk-to", "magic_carpet_monkey") {
            player<Neutral>("Who's a cute little monkey?")
            if (equipped(EquipSlot.Ammo).id != "mspeak_amulet") {
                npc<Neutral>("Ukkuk oook! Eeek aka, ahh aka gonk.")
                return@npcOperate
            }
            npc<Neutral>("Who's an ugly human? Give me a banana!")
            player<Neutral>("What's up with you?")
            npc<Neutral>("Stupid human! Give monkey a banana!")
            if (!inventory.contains("banana")) {
                player<Neutral>("Sorry monkey, I don't have any bananas.")
                npc<Neutral>("Aghhh. You're a rubbish human, get monkey a banana now.")
                return@npcOperate
            }
            choice {
                option("Give the monkey a banana.") {
                    giveBanana()
                }
                option("Don't give it a banana.") {
                    player<Neutral>("I'll not give you a banana until you learn manners.")
                }
            }
        }

        itemOnNPCOperate("banana", "magic_carpet_monkey") {
            giveBanana()
        }

        timerStart("magic_carpet_ride") {
            clearAnim()
            gfx("magic_carpet_fly")
            renderEmote("magic_carpet")
            5
        }

        timerTick("magic_carpet_ride") {
            gfx("magic_carpet_fly")
            Timer.CONTINUE
        }

        timerStop("magic_carpet_ride") {
            clearGfx()
        }

        playerDespawn {
            if (get("magic_carpet", false)) {
                softTimers.stop("magic_carpet_ride")
                set("magic_carpet", false)
                // Note: This allows fast travel by relogging, osrs keeps the
                // player logged in until travelling has completed.
                val dest: String = get("magic_carpet_destination") ?: return@playerDespawn
                strongQueue("magic_carpet_logout", behaviour = LogoutBehaviour.Accelerate) {
                    land(dest)
                }
            }
        }
    }

    private fun Player.carpetEnd() {
        clearAnim()
        clearGfx()
        clearRenderEmote()
        clearCamera()
        openTabs()
        set("magic_carpet", false)
    }

    private suspend fun Player.travel(from: String, to: String, skip: Boolean = false) {
        var price = 200
        if (questCompleted("rogue_trader")) {
            price = 100
            if (!skip) {
                npc<Neutral>("There is a fare for this service you know - normally it's 200 gold per journey, but for you, I'll let you go for 100.")
            }
        } else if (!skip) {
            npc<Neutral>("There is a fare for this service you know : it's 200 gold per journey.")
        }
        // https://youtu.be/qGX2YLs1Pb0?t=652
        if (equipped(EquipSlot.Ring).id != "ring_of_charos_a") {
            travel(from, to, price, skip)
            return
        }
        choice("Pay $price coins?") {
            option("Here you go.") {
                travel(from, to, price, skip)
            }
            val cheaper = if (price == 100) 75 else 100
            option<Quiz>("[Charm] Seeing as you've reduced it already, how about ${cheaper}gp?") {
                npc<Neutral>("Ah, a world-class haggler, I see. Very well, ${cheaper}gp it is.")
                price = cheaper
                travel(from, to, cheaper, skip)
            }
        }
    }

    private suspend fun Player.travel(from: String, to: String, cost: Int, skip: Boolean) {
        if (!inventory.remove("coins", cost)) {
            player<Neutral>("I don't have enough money with me.")
            npc<Neutral>("Looks like you're walking then.")
            return
        }
        if (!skip) {
            player<Happy>("Here you go.")
        }
        // Note: [Steps] doesn't have forceRun so this is the work-around
        if (runEnergy == 0) {
            runEnergy = 1
        }
        running = true
        set("magic_carpet_destination", to)
        when (from) {
            "north_pollnivneach" -> northPollnivneachStart()
            "south_pollnivneach" -> southPollnivneachStart()
            "uzer" -> uzerStart()
            "shantay_pass" -> shantayStart()
            "bedabin_camp" -> bedabinCampStart()
            "sophanem" -> sophanemStart()
            "menaphos" -> menaphosStart()
            "nardah" -> nardahStart()
            "monkey_colony" -> monkeyColony()
        }
        patrolDelay("${from}_to_$to", loop = false, noCollision = true)
        land(to)
    }

    private suspend fun Player.land(to: String) {
        when (to) {
            "north_pollnivneach" -> northPollnivneachLand()
            "south_pollnivneach" -> southPollnivneachLand()
            "uzer" -> uzerLand()
            "shantay_pass" -> shantayLand()
            "bedabin_camp" -> bedabinLand()
            "sophanem" -> sophanemLand()
            "menaphos" -> menaphosLand()
            "nardah" -> nardahLand()
            "monkey_colony" -> monkeyColony()
        }
    }

    private suspend fun Player.giveBanana() {
        if (!inventory.remove("banana")) {
            return
        }
        inc("monkey_gifted_bananas")
        npc<EvilLaugh>("Ha ha! Smelly human gave monkey a banana.")
        player<Neutral>("Wow you're one nasty piece of work. Have you ever heard of gratitude?")
        npc<Neutral>("Hey baldy, give monkey another banana!")
        player<Neutral>("Just because I'm not covered in fur doesn't make me bald, you cheeky monkey.")
        npc<Neutral>("Monkey wants another banana now! Give me, give me!")
        player<Neutral>("Look I've had it with you, you little degenerate.")
    }

    private fun ChoiceOption.aboutAli(target: NPC) {
        option<Neutral>("Tell me about Ali Morrisane.") {
            npc<Shock>("What, you haven't heard of Ali M? Possibly the greatest salesman of the Kharidian empire if not all Gielinor?")
            if (questCompleted("the_feud")) {
                player<Neutral>("Ah yes I remember him now, I went on a wild goose chase looking for his nephew.")
                npc<Happy>("Ha! No doubt old Ali M instigated the whole thing.")
                player<Neutral>("I had a bit of fun though, the whole job was quite diverting.")
                npc<Neutral>("There's never a dull moment around that man, he's always looking for a way to make a quick coin or two.")
            } else {
                // TODO correct expressions
                player<Neutral>("I can't say that I have, but he must be the ambitious type to try and set up his own airline.")
                npc<Neutral>("You know something, I reckon that he's trying to take on those gnomes at their own game and I'd bet good money that he'll probably win.")
                player<Neutral>("Hah? I think you've gone and lost me now.")
                npc<Neutral>("You know those small little guys, not the dwarves now mind.")
                player<Neutral>("Ya... gnomes, I'm with you that far.")
                npc<Neutral>("Well they have already established an Airline, Gnome Air...")
                player<Neutral>("Go on...")
                npc<Neutral>("Anyway I think that Ali M's setup here will prove really successful and maybe once we're properly established we could try compete with those gnomes.")
                player<Neutral>("I'll watch this space.")
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
        val current = target.id.removePrefix("rug_merchant_")
        when (target.id) {
            "rug_merchant_south_pollnivneach" -> {
                if (questCompleted("icthlarins_little_helper")) {
                    npc<Neutral>("From here you can travel to Nardah and the Menaphite cities of Sophanem and Menaphos.")
                    // https://youtu.be/qGX2YLs1Pb0?t=737
                    choice {
                        option<Neutral>("I want to travel to Nardah.") {
                            travel(current, "nardah")
                        }
                        option<Neutral>("I want to travel to Menaphos.") {
                            travel(current, "menaphos")
                        }
                        option<Neutral>("I want to travel to Sophanem.") {
                            travel(current, "sophanem")
                        }
                        option<Bored>("I don't want to travel to any of those places.") {
                            npc<Neutral>("Fair enough, magic carpet travel isn't for everyone. Enjoy the walk.")
                        }
                    }
                } else {
                    // https://youtu.be/AANu2wdVAbQ?t=108
                    npc<Neutral>("Travel to Menaphos and Sophanem from here would be possible, but for the strange goings on there.")
                    player<Neutral>("What do you mean?")
                    npc<Neutral>("Well, for one, the gates to both of the towns have been locked to those trying to enter and leave, so there really isn't any point in servicing them at the moment.")
                    player<Neutral>("So, I can't take a ride on a magic carpet then?")
                    npc<Neutral>("You can still travel from here to Nardah.")
                    choice("Where do you wish to travel?") {
                        option("I want to travel to Nardah.") {
                            travel(current, "nardah")
                        }
                        option("Actually, I've changed my mind.") {
                            npc<Neutral>("Fair enough, magic carpet travel isn't for everyone. Enjoy the walk.")
                        }
                    }
                }
            }
            "rug_merchant_sophanem" -> {
                npc<Neutral>("The carpets here will take you to the south of Pollnivneach. Do you want to take a lift?")
                choice {
                    option<Neutral>("Pollnivneach will do.") {
                        travel(current, "south_pollnivneach")
                    }
                    option<Bored>("I don't want to travel there.") {
                        npc<Neutral>("Fair enough, magic carpet travel isn't for everyone. Enjoy the walk.")
                    }
                }
            }
            "rug_merchant_nardah" -> {
                npc<Neutral>("The carpets here will take you to the south of Pollnivneach.")
                choice {
                    option<Neutral>("Let's go then.") {
                        travel(current, "south_pollnivneach")
                    }
                    option<Bored>("I don't want to travel there.") {
                        npc<Neutral>("Fair enough, magic carpet travel isn't for everyone. Enjoy the walk.")
                    }
                }
            }
            "rug_merchant_north_pollnivneach" -> {
                npc<Neutral>("From here you can travel to the Shantay Pass - the Southern gate of Al Kharid.")
                choice {
                    option<Neutral>("Take me to the Pass then.") {
                        travel(current, "shantay_pass")
                    }
                    option<Bored>("I don't want to travel there.") {
                        npc<Neutral>("Fair enough, magic carpet travel isn't for everyone. Enjoy the walk.")
                    }
                }
            }
            "rug_merchant_uzer" -> {
                npc<Neutral>("You can travel from here back to the Shantay Pass.")
                choice {
                    option<Neutral>("That sounds good, take me there.") {
                        travel(current, "shantay_pass")
                    }
                    option<Bored>("I don't want to travel there.") {
                        npc<Neutral>("Fair enough, magic carpet travel isn't for everyone. Enjoy the walk.")
                    }
                }
            }
            "rug_merchant_bedabin_camp" -> {
                npc<Neutral>("From here you can travel to the Shantay Pass.")
                choice {
                    option<Neutral>("Take me there.") {
                        travel(current, "shantay_pass")
                    }
                    option<Bored>("I don't want to travel there.") {
                        npc<Neutral>("Fair enough, magic carpet travel isn't for everyone. Enjoy the walk.")
                    }
                }
            }
            "rug_merchant_shantay_pass" -> {
                // TODO dialogue without quest requirements
                npc<Neutral>("From here you can travel to Uzer, to the Bedabin camp or to the North of Pollnivneach.")
                npc<Neutral>("The second major carpet hub station, to the south of Pollnivneach is in easy walking distance from there.")
                choice {
                    if (questCompleted("the_golem")) {
                        option("I want to travel to Uzer.") {
                            travel(current, "uzer")
                        }
                    }
                    option<Neutral>("I want to travel to the Bedabin camp.") {
                        travel(current, "bedabin_camp")
                    }
                    option<Neutral>("I want to travel to Pollnivneach.") {
                        travel(current, "pollnivneach")
                    }
                }
            }
        }
    }

    private fun ChoiceOption.noThanks() {
        option("No thanks.") {
            npc<Neutral>("Come back anytime.")
        }
    }

    private fun ChoiceOption.questions(target: NPC) {
        option<Neutral>("I have some questions.") {
            npc<Neutral>("I'll try help you as much as I can.")
            choice {
                option<Neutral>("What are you doing here?") {
                    when (target.id) {
                        "rug_merchant_shantay_pass" -> npc<Neutral>("Well this is a good position for desert traffic. Shantay seems to have a nice little money spinner setup, but I reckon, this could turn out even better.")
                        "rug_merchant_north_pollnivneach" -> {
                            npc<Neutral>("Well Pollnivneach is the ideal location for setting up a carpet station.")
                            player<Neutral>("Why's that?")
                            npc<Neutral>("You see it's located halfway between Al Kharid, and the Menaphite cities and close enough to Nardah too, so we get more than enough traffic to keep the business running.")
                        }
                        "rug_merchant_south_pollnivneach" -> {
                            npc<Neutral>("I work here renting out magic carpets. I'm from Pollnivneach so it is a handy job, I don't have to commute too far to work every day.")
                            player<Neutral>("So I suppose you're called Ali then.")
                            npc<Neutral>("Not the most remarkable of names, not that it matters, you see everyone in town knows me as Flash.")
                            player<Neutral>("Really?")
                            npc<Blink>("No.")
                            player<Blink>("........")
                            npc<Blink>(".........")
                            player<Neutral>("Oh right.")
                        }
                        "rug_merchant_sophanem" -> {
                            npc<Laugh>("I look after the carpet station here. The place is a bit dead though. Ha! I'm just too much.")
                            player<Confused>("What?")
                            npc<Neutral>("You know, Sophanem, city of the dead and all that?")
                            player<Blink>("...")
                            npc<Neutral>("Aww come on, the joke wasn't that bad.")
                            player<Blink>("...")
                        }
                        "rug_merchant_nardah" -> {
                            npc<Neutral>("Well I'd preferred to have been running one of the carpet stations at a hub such as Pollnivneach. I was a bit slow off the mark to get that gig though. Still business in Nardah isn't bad for a terminal. At least")
                            npc<Neutral>("people come here for the bank and to see the herbalist.")
                        }
                        "rug_merchant_uzer" -> {
                            npc<Neutral>("You mightn't realise it, but this is quite a busy station.")
                            player<Neutral>("Who would want to come here?")
                            npc<Neutral>("Well you for one, and besides that we get quite a few archaeologists from the dig site passing through to examine the golem.")
                        }
                        "rug_merchant_attendant" -> {
                            npc<Neutral>("Until recently I was looking after one of the busiest carpet stations. But that all changed since Menaphos closed its gates.")
                            npc<Neutral>("Right now I have to fill my day trying to come up with reasons why Ali M should keep this place open.")
                            player<Neutral>("So have you come up with any good ideas then?")
                            npc<Neutral>("Not really. The only reason I have come up with to date is that by keeping the station open, people will become familiar with it.")
                            npc<Neutral>("Maybe once Menaphos opens her gates again, the station will make a fortune once more.")
                        }
                        "rug_merchant_bedabin_camp" -> npc<Neutral>("Well besides the obvious - looking after this station, I'm trying to figure out how these Tentis manage to cultivate such delicious pineapples.")
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
        option<Neutral>("Is that your pet monkey nearby?") {
            npc<Neutral>("He's his own monkey, he does whatever suits him, a total nuisance.")
            player<Neutral>("I detect a degree of hostility being directed towards the monkey.")
            npc<Neutral>("I shouldn't say this really, but sometimes I begin to question some of Ali Morrisane's ideas, he says that associating a monkey with any product will increase sales. I just don't know, what will be next?")
            player<Quiz>("Frogs?")
            npc<Neutral>("I doubt it, amphibians don't have the same cutesy factor as monkeys.")
            player<Neutral>("I'm confused. I thought you didn't like monkeys.")
            npc<Neutral>("I don't dislike monkeys, it's just that monkey. I don't know, I might just be paranoid but I think he's... well... evil.")
            player<Neutral>("Hmmm... Interesting.")
            player<Neutral>("Hang on a minute, I think I have an amulet of monkeyspeak stored somewhere. Perhaps I could get it and see what it has to say for itself.")
            npc<Neutral>("Would you? It sometimes gives me these stares... <blue>~A visible shiver runs down his back.")
            npc<Neutral>("I would be really grateful if you could get that monkey off my back.")
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
        option<Neutral>("Where did you get that hat?") {
            npc<Neutral>("My fez? I got it from Ali Morrisane, it's a uniform of sorts, apparently it makes us more visible, but I'm not too sure about it.")
            player<Neutral>("Well it is quite distinctive.")
            npc<Neutral>("Do you like it? I haven't really made my mind up about it yet. You see it's not all that practical for desert conditions.")
            player<Neutral>("How so?")
            npc<Neutral>("Well it doesn't keep the sun out of my eyes and after a while sitting out in the desert they really begin to burn.")
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
            npc<Neutral>("Come back anytime.")
        }
    }

    private fun ChoiceOption.explainFleet(target: NPC) {
        option("Tell me about this magic carpet fleet.") {
            player<Neutral>("Tell me about this Magic Carpet fleet.")
            npc<Neutral>("The latest idea from the great Ali Morrisane. Desert travel will never be the same again.")
            player<Neutral>("So how does it work?")
            npc<Neutral>("The carpet or the whole enterprise?")
            choice {
                option<Neutral>("Tell me about how the carpet works.") {
                    npc<Neutral>("I'm not really too sure, it's just an enchanted rug really, made out of special Ugthanki hair. It flies to whatever destination its owner commands.")
                }
                option("Tell me about the enterprise then.") {
                    npc<Neutral>("It's quite simple really, Ali Morrisane has hired myself and a few others to set up carpet stations at some of the desert's more populated places and run flights between the stations.")
                    player<Neutral>("So why has he limited the service to just the desert?")
                    npc<Neutral>("I don't think Ali is prepared to take on Gnome Air just yet, their gliders are much faster than our carpets, besides that I think we are in the short haul business, something that would only work in harsh conditions like")
                    npc<Neutral>("the desert.")
                    player<Neutral>("Why is that?")
                    npc<Neutral>("I suppose because people would just walk. Getting lost isn't too much of a problem generally, but it's a different matter when you're in the middle of the Kharidian desert with a dry waterskin and no idea")
                    npc<Neutral>("which direction to go in.")
                    player<Neutral>("You're right I guess. How's the business going then?")
                    npc<Neutral>("Not too bad, the hubs are generally quite busy. But the stations in Uzer and the Bedabin camp could do with a bit more traffic.")
                    player<Neutral>("A growth market I guess.")
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

    private suspend fun Player.shantayStart() {
        // https://youtu.be/_OMaNKNDrEs?t=29
        moveCamera(Tile(3302, 3110), height = 500, speed = 100, acceleration = 100)
        turnCamera(Tile(3308, 3110), height = 100, speed = 100, acceleration = 100)
        walkToDelay(Tile(3309, 3110))
        jingle("magic_carpet_travel")
        clearWatch()
        clear("face_entity")
        tele(Tile(3308, 3110))
        face(Direction.SOUTH)
        delay(2)
        beginTravel()
        delay(3)
        moveCamera(Tile(3308, 3114), height = 500, speed = 10, acceleration = 10)
        turnCamera(Tile(3308, 3110), height = 300, speed = 10, acceleration = 10)
        delay(1)
        softTimers.start("magic_carpet_ride")
        delay(1)
        exactMoveDelay(Tile(3308, 3108), delay = 30, direction = Direction.SOUTH)
        clearCamera()
    }

    private suspend fun Player.bedabinCampStart() {
        moveCamera(Tile(3184, 3054), height = 1000, speed = 100, acceleration = 100)
        turnCamera(Tile(3180, 3045), height = 325, speed = 100, acceleration = 100)
        walkToDelay(Tile(3180, 3044))
        jingle("magic_carpet_travel")
        clearWatch()
        clear("face_entity")
        tele(Tile(3180, 3044))
        face(Direction.NORTH)
        delay(2)
        beginTravel()
        delay(3)
        softTimers.start("magic_carpet_ride")
        delay(1)
        exactMoveDelay(Tile(3180, 3043), delay = 30, direction = Direction.NORTH)
        clearCamera()
    }

    private suspend fun Player.sophanemStart() {
        moveCamera(Tile(3300, 2813), height = 1000, speed = 100, acceleration = 100)
        turnCamera(Tile(3285, 2813), height = 200, speed = 100, acceleration = 100)
        walkToDelay(Tile(3285, 2813))
        jingle("magic_carpet_travel")
        clearWatch()
        clear("face_entity")
        tele(Tile(3285, 2813))
        face(Direction.NORTH)
        delay(2)
        beginTravel()
        delay(3)
        softTimers.start("magic_carpet_ride")
        delay(1)
        exactMoveDelay(Tile(3285, 2816), delay = 30, direction = Direction.NORTH)
        exactMoveDelay(Tile(3289, 2820), delay = 30, direction = Direction.NORTH)
        clearCamera()
    }

    private suspend fun Player.menaphosStart() {
        moveCamera(Tile(3252, 2813), height = 1000, speed = 100, acceleration = 100)
        turnCamera(Tile(3245, 2813), height = 300, speed = 100, acceleration = 100)
        walkToDelay(Tile(3244, 2813))
        jingle("magic_carpet_travel")
        clearWatch()
        clear("face_entity")
        tele(Tile(3245, 2813))
        face(Direction.NORTH)
        delay(2)
        beginTravel()
        delay(3)
        softTimers.start("magic_carpet_ride")
        delay(1)
        exactMoveDelay(Tile(3245, 2815), delay = 30, direction = Direction.NORTH)
        clearCamera()
    }

    private suspend fun Player.nardahStart() {
        moveCamera(Tile(3408, 2907), height = 650, speed = 100, acceleration = 100)
        turnCamera(Tile(3399, 2915), height = 475, speed = 100, acceleration = 100)
        walkToDelay(Tile(3401, 2916))
        jingle("magic_carpet_travel")
        clearWatch()
        clear("face_entity")
        tele(Tile(3401, 2916))
        face(Direction.WEST)
        delay(2)
        beginTravel()
        delay(3)
        softTimers.start("magic_carpet_ride")
        delay(1)
        exactMoveDelay(Tile(3400, 2916), delay = 30, direction = Direction.WEST)
        exactMoveDelay(Tile(3393, 2915), direction = Direction.WEST)
        clearCamera()
    }

    private suspend fun Player.northPollnivneachStart() {
        moveCamera(Tile(3344, 3005), height = 600, speed = 100, acceleration = 100)
        turnCamera(Tile(3349, 3005), height = 100, speed = 100, acceleration = 100)
        walkToDelay(Tile(3349, 3003))
        jingle("magic_carpet_travel")
        clearWatch()
        clear("face_entity")
        tele(Tile(3349, 3003))
        face(Direction.EAST)
        delay(2)
        beginTravel()
        delay(3)
        moveCamera(Tile(3354, 2998), height = 1000, speed = 100, acceleration = 100)
        turnCamera(Tile(3349, 3003), height = 500, speed = 100, acceleration = 100)
        delay(1)
        softTimers.start("magic_carpet_ride")
        delay(1)
        walkOverDelay(Tile(3347, 3005))
        walkOverDelay(Tile(3346, 3007))
        clearCamera()
    }

    private suspend fun Player.uzerStart() {
        moveCamera(Tile(3460, 3123), height = 1000, speed = 100, acceleration = 100)
        turnCamera(Tile(3469, 3113), height = 325, speed = 100, acceleration = 100)
        walkToDelay(Tile(3469, 3111))
        jingle("magic_carpet_travel")
        clearWatch()
        clear("face_entity")
        tele(Tile(3469, 3113))
        face(Direction.SOUTH)
        delay(2)
        beginTravel()
        delay(3)
        softTimers.start("magic_carpet_ride")
        delay(1)
        walkOverDelay(Tile(3466, 3112))
        clearCamera()
    }

    private suspend fun Player.southPollnivneachStart() {
        moveCamera(Tile(3357, 2942), height = 500, speed = 88, acceleration = 100)
        turnCamera(Tile(3351, 2942), height = 100, speed = 100, acceleration = 100)
        walkToDelay(Tile(3351, 2942))
        jingle("magic_carpet_travel")
        clearWatch()
        clear("face_entity")
        tele(Tile(3351, 2942))
        face(Direction.SOUTH)
        delay(2)
        beginTravel()
        delay(3)
        softTimers.start("magic_carpet_ride")
        delay(1)
        walkOverDelay(Tile(3351, 2939))
        clearCamera()
    }

    private fun Player.beginTravel() {
        closeTabs()
        anim("magic_carpet_takeoff")
        gfx("magic_carpet_takeoff")
        set("magic_carpet", true)
        sound("carpet_rise")
    }

    private suspend fun Player.northPollnivneachLand() {
        moveCamera(Tile(3339, 3007), height = 875, speed = 100, acceleration = 100)
        turnCamera(Tile(3349, 3003), height = 500, speed = 100, acceleration = 100)
        walkOverDelay(Tile(3348, 3005), forceWalk = false)
        walkOverDelay(Tile(3350, 3004), forceWalk = false)
        delay(1)
        exactMove(Tile(3349, 3003), delay = 30, direction = Direction.EAST)
        delay(1)
        face(Direction.EAST)
        delay(2)
        carpetLand()
        delay(4)
        carpetEnd()
        walkOverDelay(Tile(3351, 3003))
        arriveDelay()
    }

    private suspend fun Player.southPollnivneachLand() {
        moveCamera(Tile(3359, 2943), height = 1000, speed = 100, acceleration = 100)
        turnCamera(Tile(3351, 2942), height = 500, speed = 100, acceleration = 100)
        exactMove(Tile(3351, 2941), delay = 30, direction = Direction.NORTH)
        delay(1)
        face(Direction.NORTH)
        delay(2)
        carpetLand()
        delay(4)
        carpetEnd()
        walkOverDelay(Tile(3352, 2941))
        arriveDelay()
    }

    private suspend fun Player.uzerLand() {
        // https://www.youtube.com/watch?v=XYZCVNcmsQc
        moveCamera(Tile(3460, 3123), height = 1000, speed = 100, acceleration = 100)
        turnCamera(Tile(3469, 3113), height = 325, speed = 100, acceleration = 100)
        exactMove(Tile(3469, 3113), delay = 30, direction = Direction.NORTH)
        face(Direction.NORTH)
        delay(3)
        carpetLand()
        delay(4)
        carpetEnd()
        walkOverDelay(Tile(3470, 3114))
        arriveDelay()
    }

    private suspend fun Player.sophanemLand() {
        moveCamera(Tile(3300, 2813), height = 1000, speed = 100, acceleration = 100)
        turnCamera(Tile(3285, 2813), height = 200, speed = 100, acceleration = 100)
        exactMove(Tile(3289, 2817), delay = 30, direction = Direction.NORTH)
        exactMove(Tile(3288, 2813), delay = 30, direction = Direction.NORTH)
        exactMove(Tile(3285, 2813), delay = 30, direction = Direction.NORTH)
        face(Direction.NORTH)
        delay(3)
        carpetLand()
        delay(4)
        carpetEnd()
        walkOverDelay(Tile(3286, 2813))
        arriveDelay()
    }

    private suspend fun Player.menaphosLand() {
        moveCamera(Tile(3254, 2811), height = 1000, speed = 100, acceleration = 100)
        turnCamera(Tile(3245, 2813), height = 200, speed = 100, acceleration = 100)
        exactMove(Tile(3245, 2815), delay = 30, direction = Direction.NORTH)
        exactMove(Tile(3245, 2813), delay = 30, direction = Direction.NORTH)
        face(Direction.NORTH)
        delay(3)
        carpetLand()
        delay(4)
        carpetEnd()
        walkOverDelay(Tile(3246, 2813))
        arriveDelay()
    }

    private suspend fun Player.nardahLand() {
        moveCamera(Tile(3408, 2907), height = 650, speed = 100, acceleration = 100)
        turnCamera(Tile(3399, 2915), height = 475, speed = 100, acceleration = 100)
        exactMove(Tile(3396, 2915), delay = 30, direction = Direction.EAST)
        exactMove(Tile(3399, 2915), delay = 30, direction = Direction.EAST)
        exactMove(Tile(3401, 2916), delay = 30, direction = Direction.EAST)
        face(Direction.EAST)
        delay(3)
        carpetLand()
        delay(4)
        carpetEnd()
        walkOverDelay(Tile(3402, 2916))
        arriveDelay()
    }

    private suspend fun Player.monkeyColony() {
        // https://youtu.be/C2rhZgSNwu0?t=177
    }

    private suspend fun Player.bedabinLand() {
        moveCamera(Tile(3176, 3048), height = 1000, speed = 100, acceleration = 100)
        turnCamera(Tile(3180, 3044), height = 325, speed = 100, acceleration = 100)
        exactMove(Tile(3181, 3046), delay = 30, direction = Direction.EAST)
        exactMove(Tile(3180, 3045), delay = 30, direction = Direction.SOUTH)
        face(Direction.SOUTH)
        delay(4)
        carpetLand()
        delay(4)
        carpetEnd()
        walkOverDelay(Tile(3181, 3045))
        arriveDelay()
    }

    private suspend fun Player.shantayLand() {
        moveCamera(Tile(3298, 3110), height = 1000, speed = 100, acceleration = 100)
        turnCamera(Tile(3308, 3110), height = 200, speed = 100, acceleration = 100)
        exactMove(Tile(3308, 3110), delay = 30, direction = Direction.SOUTH)
        face(Direction.SOUTH)
        delay(1)
        carpetLand()
        delay(4)
        carpetEnd()
        walkOverDelay(Tile(3309, 3110))
        arriveDelay()
    }

    private fun Player.carpetLand() {
        softTimers.stop("magic_carpet_ride")
        anim("magic_carpet_land")
        gfx("magic_carpet_land")
        sound("carpet_land")
    }
}
