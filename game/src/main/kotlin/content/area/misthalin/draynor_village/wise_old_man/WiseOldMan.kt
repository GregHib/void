package content.area.misthalin.draynor_village.wise_old_man

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.items
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.removeToLimit
import world.gregs.voidps.type.random

class WiseOldMan : Script {
    init {
        npcOperate("Talk-to", "wise_old_man_draynor") { (target) ->
            npc<Happy>("Greetings, $name.")
            if (get("wise_old_man_met", false)) {
                checkTaskNpcs(target)
                checkTaskItems()
                choice("What would you like to say?") {
                    anyHelp(this@choice)
                    findJunk()
                    ask()
                }
                return@npcOperate
            }
            intro()
        }
    }

    suspend fun Player.checkTaskNpcs(npc: NPC) {
        if (get("wise_old_man_npc", "") != "thing_under_the_bed") {
            return
        }
        if (get("wise_old_man_remaining", 0) != 0) {
            return
        }
        player<Happy>("I've killed a creature that was under your bed.")
        npc<Happy>("Ah, thank you very much! Now I shall be able to sleep in peace.")
        npc<Happy>("Allow me to offer you an appropriate reward for your assistance...")
        npc.anim("bind")
        clear("wise_old_man_npc")
        clear("wise_old_man_remaining")
        exp(Skill.Constitution, (280..300).random(random).toDouble())
    }

    suspend fun Player.checkTaskItems() {
        val item: String = get("wise_old_man_task") ?: return
        if (!inventory.contains(item)) {
            if (inventory.contains("${item}_noted")) {
                player<Happy>("Here, I've got the items you wanted.")
                npc<Neutral>("Those are banknotes! I can't use those!")
            }
            return
        }
        val remaining: Int = get("wise_old_man_remaining") ?: return
        if (inventory.count(item) < remaining) {
            player<Happy>("I've got some of the stuff you wanted.")
        } else {
            player<Happy>("I've got all the stuff you asked me to fetch.")
        }
        val removed = inventory.removeToLimit(item, remaining)
        if (removed != remaining) {
            set("wise_old_man_remaining", remaining - removed)
            npc<Happy>("Ahh, you are very kind.")
            player<Happy>("I'll come back when I've got the rest.")
            return
        }
        clear("wise_old_man_remaining")
        clear("wise_old_man_task")
        inc("wise_old_man_tasks_completed")
        when (val reward = OldMansMessage.reward(this, hard.contains(item))) {
            "runes" -> {
                items("nature_rune", "water_rune", "The Wise Old Man gives you some runes.")
                npc<Happy>("Thank you, thank you! Please take these runes as a sign of my gratitude.")
            }
            "herbs" -> {
                item("grimy_tarromin", 400, "<navy>The Wise Old Man gives you some backnotes that can be exchanged for herbs.")
                npc<Happy>("Thank you, thank you! Please take these herbs as a sign of my gratitude.")
            }
            "seeds" -> {
                item("potato_seed", 400, "<navy>The Wise Old Man gives you some seeds.")
                npc<Happy>("Thank you, thank you! Please take these seeds as a sign of my gratitude.")
            }
            "prayer" -> {
                item(167, "<navy>The Wise Old Man blesses you.<br>You gain some Prayer xp.")
                npc<Happy>("Thank you, thank you! In thanks, I shall bestow on you a simple blessing.")
            }
            "coins" -> {
                item("coins_8", 400, "<navy>The Wise Old Man gives you some coins.")
                npc<Happy>("Thank you, thank you! Please take this money as a sign of my gratitude.")
            }
            else -> item(
                reward,
                400,
                "The Wise Old Man gives you an ${reward.toSentenceCase()}${
                    when {
                        reward.endsWith("diamond") || reward.endsWith("ruby") || reward.endsWith("emerald") -> "!"
                        else -> "."
                    }
                }",
            )
        }
    }

    private suspend fun Player.intro() {
        player<Quiz>("So you're a wise old man, huh?")
        npc<Shifty>("Less of the 'old' man, if you please!")
        npc<Happy>("But yes, I suppose you could say that. I prefer to think of myself as a sage.")
        player<Quiz>("So what's a sage doing here?")
        npc<Neutral>("I've spent most of my life studying this world in which we live. I've strode through the depths of the deadliest dungeons, roamed the murky jungles of Karamja, meditated on the glories of Saradomin on Entrana,")
        npc<Neutral>("and read dusty tomes in the Library of Varrock.")
        npc<Happy>("Now I'm not as young as I used to be, I'm settling here where it's peaceful.")
        if (!questCompleted("vampire_slayer")) {
            npc<Sad>("It's a pity about that vampyre that keeps attacking the village. At least Saradomin protects me.")
        }
        player<Neutral>("That's quite an exciting life you've had.")
        npc<Laugh>("Exciting? Yes, I suppose so.")
        npc<Happy>("Now I'm here, perhaps I could offer you the benefit of my experience and wisdom?")
        player<Happy>("Thanks! So how can you help me?")
        set("wise_old_man_met", true)
        npc<Happy>("Well, I imagine you've gathered up quite a lot of stuff on your travels. Things you used for quests a long time ago that you don't need now.")
        npc<Neutral>("If you like, I can look through your bank and see if there's anything you can chuck away.")
        npc<Neutral>("Alternatively, you can bring items here and show them to me. If I see that it's something you don't need, I'll let you know. I might even be willing to buy it.")
        player<Neutral>("So you'll help me clear junk out of my bank?")
        npc<Happy>("Yes, that's right. Or I'd be happy to chat with you about the wonders of this world!")
        choice("What would you like to say?") {
            option<Shifty>("Could I have some free stuff, please?") {
                npc<Sad>("Deary deary me...")
                if (!World.members) {
                    npc<Neutral>("I'm not giving out free money, but if you log into a members' world I'd be glad to reward you if you'd do a little job for me.")
                    return@option
                }
                npc<Neutral>("I'm not giving out free money, but I'd be happy to reward you if you'll do a little job for me.")
                choice("What would you like to say?") {
                    option("Ok, what do you want me to do?")
                    option<Shifty>("Thanks, maybe some other time.") {
                        npc<Neutral>("As you wish. Farewell, $name.")
                    }
                }
            }
            ask()
            anyHelp(this@choice)
            findJunk()
            option<Shifty>("Thanks, maybe some other time.")
        }
    }

    private fun ChoiceOption.ask() {
        option<Happy>("I'd just like to ask you something.") {
            npc<Happy>("Please do!")
            topic()
        }
    }

    private fun Player.anyHelp(option: ChoiceOption) {
        if (contains("wise_old_man_task")) {
            option.option("What did you ask me to do?") {
                checkTask()
            }
        } else {
            option.option<Happy>("Is there anything I can do for you?") {
                task()
            }
        }
    }

    private fun ChoiceOption.findJunk() {
        option<Happy>("Could you check my items for junk, please?") {
            choice {
                option<Happy>("Could you check my bank for junk, please?") {
                    npc<Neutral>("Certainly, but I should warn you that I don't know about all items.")
                    // TODO add junk search
                    npc<Neutral>("There doesn't seem to be any junk in your bank at all.")
                }
                option<Happy>("Could you check my inventory for junk, please?") {
                    npc<Neutral>("Certainly, but I should warn you that I don't know about all items.")
                    // TODO add junk search
                    npc<Neutral>("There doesn't seem to be any junk in your inventory at all.")
                }
                //  if (follower != null) { // TODO and has BoB
                //      option("Could you check my beast of burden for junk, please?")
                //  }
            }
        }
    }

    private suspend fun Player.topic() {
        choice("Pick a topic") {
            option("Distant lands") {
                choice("Pick a topic") {
                    option("The Wilderness") {
                        player<Happy>("Could you tell me about the Wilderness, please?")
                        npc<Neutral>("If Entrana is a land dedicated to the glory of Saradomin, the Wilderness is surely the land of Zamorak.")
                        npc<Happy>("It's a dangerous place, where adventurers such as yourself may attack each other, using all their combat skills in the struggle for survival.")
                        npc<Neutral>("The Wilderness has different levels. In a low level area, you can only fight adventurers whose combat level is close to yours.")
                        npc<Neutral>("But if you venture into the high level areas in the far north, you can be attacked by adventurers who are significantly stronger than you.")
                        npc<Happy>("Of course, you'd be able to attack considerably weaker people too, so it can be worth the risk.")
                        npc<Laugh>("If you dare to go to the far north-west of the Wilderness, there's a building called the Mage Arena where you can learn to summon the power of Saradomin himself!")
                        npc<Neutral>("Is there anything else you'd like to ask?")
                        anythingElse()
                    }
                    option("Misty jungles") {
                        player<Happy>("What can you tell me about jungles?")
                        npc<Happy>("If it's jungle you want, look no further than the southern regions of Karamja.")
                        npc<Happy>("Once you get south of Brimhaven, the whole island is pretty much covered in exotic trees, creepers and shrubs.")
                        npc<Neutral>("There's a small settlement called Tai Bwo Wannai Village in the middle of the island. It's a funny place; the chieftain's an unfriendly chap and his sons are barking mad.")
                        npc<Laugh>("Honestly, one of them asked me to stuff a dead monkey with seaweed so he could EAT it!")
                        npc<Happy>("Further south you'll find Shilo Village. It's been under attack by terrifying zombies in recent months, if my sources are correct.")
                        npc<Happy>("The jungle's filled with nasty creatures. There are vicious spiders that you can hardly see before they try to bite your legs off, and great big jungle ogres.")
                        npc<Neutral>("Is there anything else you'd like to ask?")
                        anythingElse()
                    }
                    option("Underground domains") {
                        player<Happy>("Tell me about what's underground.")
                        npc<Laugh>("Oh, the dwarven realms?")
                        npc<Neutral>("Yes, there was a time, back in the Fourth Age, when we humans wouldn't have been able to venture underground. That was before we had magic; the dwarves were quite a threat.")
                        npc<Happy>("Still, it's much more friendly now. You can visit the vast dwarven mine if you like; the entrance is on the mountain north of Falador.")
                        npc<Happy>("If you go further west you may be able to visit the dwarven city of Keldagrim. But they were a bit cautious about letting humans in, last time I asked.")
                        npc<Happy>("On the other hand, if you go west of Brimhaven, you'll find a huge underground labyrinth full of giants, demons, dogs and dragons to fight. It's even bigger than the caves under Taverley, although the Taverley")
                        npc<Happy>("dungeon's pretty good for training your combat skills.")
                        npc<Neutral>("Is there anything else you'd like to ask?")
                        anythingElse()
                    }
                    option("Mystical realms") {
                        player<Happy>("What mystical realms can I visit?")
                        npc<Happy>("The fabled Lost City of Zanaris has an entrance somewhere near here. Perhaps some day you'll go there.")
                        npc<Neutral>("Also, in my research I came across ancient references to some kind of Abyss. Demons from the Abyss have already escaped into this land; Saradomin be thanked that they are very rare!")
                        npc<Happy>("Is there anything else you'd like to ask?")
                        anythingElse()
                    }
                }
            }
            option("Strange beasts") {
                choice("Pick a topic") {
                    option("Biggest & Baddest") {
                        player<Happy>("What's the biggest monster in the world?")
                        npc<Happy>("There's a mighty fire-breathing dragon living underground in the deep Wilderness, known as the King Black Dragon. It's a fearsome beast, with a breath that can poison you, freeze you to the ground or")
                        npc<Happy>("incinerate you where you stand.")
                        npc<Happy>("But even more deadly is the Queen of the Kalphites. As if her giant mandibles of death were not enough, she also throws her spines at her foes with deadly force. She can even cast rudimentary spells.")
                        npc<Sad>("Some dark power must be protecting her, for she can block attacks using prayer just as humans do.")
                        npc<Neutral>("Another beast that's worthy of a special mention is the Shaikahan. It dwells in the eastern reaches of Karamja, and is almost impossible to kill except with specially prepared weapons.")
                        npc<Neutral>("Is there anything else you'd like to ask?")
                        anythingElse()
                    }
                    option("Poison and how to survive it") {
                        player<Quiz>("What does poison do?")
                        npc<Neutral>("Many monsters use poison against their foes. If you get poisoned, you will not feel it at the time, but later you will begin to suffer its effects, and your life will drain slowly from you.")
                    }
                    option("Wealth through slaughter") {
                        player<Shifty>("What monsters drop good items?")
                        npc<Happy>("As a general rule, tougher monsters drop more valuable items. But even a lowly hobgoblin can drop valuable gems; it just does this extremely rarely.")
                        npc<Happy>("If you can persuade the Slayer Masters to train you as a Slayer, you will be able to fight certain monsters that drop valuable items far more often.")
                        npc<Happy>("You might care to invest in an enchanted dragonstone ring. These are said to make a monster drop its most valuable items a little more often.")
                        npc<Neutral>("Is there anything else you'd like to ask?")
                        anythingElse()
                    }
                    option("Random events") {
                        player<Quiz>("What are these strange monsters that keep appearing out of nowhere and attacking me when I'm training?")
                        npc<Laugh>("Ah, I imagine you see a lot of those.")
                        npc<Happy>("Creatures such as the rock golem, river troll and tree spirit dwell in places where adventurers frequently go to train their skills. While you're training you will often disturb one by accident. It will then get angry and")
                        npc<Happy>("attack you. The safest way to deal with them is to run away immediately, but they sometimes drop valuable items if you kill them.")
                        npc<Neutral>("Is there anything else you'd like to ask?")
                        anythingElse()
                    }
                }
            }
            option("Days gone by") {
                choice("Pick a topic") {
                    option("Heroic figures") {
                        player<Happy>("Tell me about valiant heroes!")
                        npc<Laugh>("Ha ha ha... There are plenty of heroes. Always have been, always will be, until the fall of the world.")
                        npc<Happy>("If you'd do a few more quests, you'd soon become a fairly noted adventurer yourself.")
                        npc<Happy>("But I suppose I could tell you of a couple...")
                        npc<Neutral>("Yes, there was a man called Arrav. No-one knew where he came from, but he was a fearsome fighter, a skillful hunter and a remarkable farmer. He lived in the ancient settlement of Avarrocka, defending it from")
                        npc<Neutral>("goblins, until he went forth in search of some strange artefact long desired by the dreaded Mahjarrat.")
                        npc<Neutral>("Perhaps some day I shall be able to tell you what became of him.")
                        npc<Neutral>("But do not let your head be turned by heroics. Randas was another great man, but he let himself be beguiled into turning to serve Zamorak, and they say he is now a mindless creature deep in the Underground Pass that")
                        npc<Neutral>("leads to Isafdar.")
                        npc<Happy>("Is there anything else you'd like to ask?")
                        anythingElse()
                    }
                    option("The origin of magic") {
                        player<Happy>("Where did humans learn to use magic?")
                        npc<Happy>("Ah, that was quite a discovery! It revolutionised our way of life and jolted us into this Fifth Age of the world.")
                        npc<Neutral>("They say a traveller in the north discovered the key, although no records state exactly what he found. From this he was able to summon the magic of the four elements, using magic as a tool and a weapon.")
                        npc<Neutral>("He and his followers then learnt how to bind the power into runes so that others could use it.")
                        npc<Neutral>("In the land south of here they constructed an immense tower where the power could be studied, but followers of Zamorak destroyed it with fire many years ago, and much of the knowledge was lost.")
                        npc<Neutral>("Perhaps one day those lost secrets will be uncovered once more.")
                        npc<Happy>("Is there anything else you'd like to ask?")
                        anythingElse()
                    }
                    option("Settlements") {
                        player<Neutral>("I suppose you'd know about the history of today's cities?")
                        npc<Bored>("Yes, there are fairly good records of the formation of the cities from primitive settlements.")
                        npc<Neutral>("In the early part of the Fourth Age, of course, there were no permanent settlements. Tribes wandered the lands, staying where they could until the resources were exhausted.")
                        npc<Neutral>("This changed as people learnt to grow crops and breed animals, and now there are very few of the old nomadic tribes. There's at least one tribe roaming between the Troll Stronghold and Rellekka, though.")
                        npc<Happy>("One settlement was Avarrocka, a popular trading centre.")
                        npc<Neutral>("In the west, Ardougne gradually formed under the leadership of the Carnillean family, despite the threat of the Mahjarrat warlord Hazeel who dwelt in that area until his downfall.")
                        npc<Bored>("Is there anything else you'd like to ask?")
                        anythingElse()
                    }
                    option("The Wise Old Man of Draynor Village") {
                        player<Shifty>("Tell me about yourself, old man.")
                        npc<Laugh>("Ah, so you want to know about me, eh?")
                        npc<Shifty>("Mmm... what could I say about myself? Let's see what I've done...")
                        npc<Neutral>("I've delved into the dungeon west of Brimhaven and heard the terrifying CRASH of the steel dragons battling each other for territory.")
                        npc<Happy>("I spent some years on Entrana, where I learnt the techniques of pure meditation.")
                        npc<Neutral>("I've wandered through the vast desert that lies south of Al Kharid and seen the great walls of Menaphos and Sophanem.")
                        npc<Happy>("Apart from all that, I've spent many a happy hour in dusty libraries, searching through ancient scrolls and texts for the wisdom of those who have passed on.")
                        npc<Bored>("Plus plenty of other adventures, quests, journeys... Is there anything else you'd like to know?")
                        anythingElse()
                    }
                }
            }
            option("Gods and demons") {
                choice("Pick a topic") {
                    option("Three gods?")
                    option("The wars of the gods") {
                        player<Happy>("I wanna know about the wars of the gods!")
                        npc<Sad>("Ah, that was a terrible time. The armies of Saradomin fought gloriously against the minions of Zamorak, but many brave warriors and noble cities were overthrown and destroyed utterly.")
                        player<Quiz>("How did it all end?")
                        npc<Shifty>("Before the Zamorakian forces could be utterly routed, Lord Saradomin took pity on them and the battle- scarred world, and allowed a truce.")
                        npc<Neutral>("Is there anything else you'd like to ask?")
                        anythingElse()
                    }
                    option("The Mahjarrat") {
                        player<Shifty>("What are the Mahjarrat?")
                        npc<Sad>("Very little is written about the tribe of the Mahjarrat. They are believed to be from the realm of Freneskae, or Frenaskrae - the spelling in this tongue is only approximate.")
                        npc<Sad>("One of them, the foul Zamorak, has achieved godhood, although none knows how this came about.")
                    }
                    option("Wielding the power of the gods") {
                        player<Happy>("Can I wield the power of Saradomin myself?")
                        npc<Happy>("If you travel to the Mage Arena in the north-west reaches of the Wilderness, the battle mage Kolodion may be willing to let you learn to summon the power of Saradomin, should you be able to pass his test.")
                        npc<Neutral>("Is there anything else you'd like to ask?")
                        anythingElse()
                    }
                }
            }
            option("Your hat!") {
                player<Neutral>("I want to ask you about your hat.")
                npc<Happy>("Why, thank you! I rather like it myself.")
                choice("What would you like to say?") {
                    option<Quiz>("Where did you get it?") {
                        npc<Happy>("Oh, I saw it on the floor when I was out for my morning stroll.")
                        player<Shock>("You found a PARTY HAT on the floor?!")
                        npc<Happy>("Yes, that's right. Would you like to ask me about something else?")
                        choice("What would you like to say?") {
                            option<Quiz>("How can I get a hat like that?") {
                                npc<Neutral>("You could buy one off another player, or wait until they're next made available by the Council.")
                            }
                            option<Happy>("Yes please.")
                            option<Shifty>("Thanks, maybe some other time.")
                        }
                    }
                    option<Quiz>("How can I get a hat like that?")
                }
            }
        }

        itemOnNPCOperate("old_mans_message", "wise_old_man_draynor") {
            player<Quiz>("Do you think I need to keep this?")
            if (contains("wise_old_man_npc")) {
                npc<Confused>("Yes, you're meant to be delivering it for me!")
            } else {
                inventory.remove("old_mans_message")
                npc<Confused>("I asked you to deliver that for me. But I may as well take it back now.")
            }
        }
    }

    private suspend fun Player.anythingElse() {
        choice("What would you like to say?") {
            option<Happy>("Yes please.")
            option<Shifty>("Thanks, maybe some other time.")
        }
    }

    suspend fun Player.checkTask() {
        val npc: String? = get("wise_old_man_npc")
        if (npc != null) {
            val intro = EnumDefinitions.string("wise_old_man_npcs", npc)
            npc<Neutral>(intro)
            if (npc != "thing_under_the_bed" && !ownsItem("old_mans_message")) {
                npc<Neutral>("You seem to have mislaid my letter, so here's another copy.")
                if (!inventory.add("old_mans_message")) {
                    npc<Happy>("Please make room in your inventory to carry the letter.")
                    return
                }
            }
            hintNpc(npc)
        }
        val item: String = get("wise_old_man_task") ?: return
        val remaining: Int = get("wise_old_man_remaining") ?: return
        val intro = EnumDefinitions.string("wise_old_man_items", item)
        npc<Happy>("$intro I still need $remaining.")
        hintItem(item)
    }

    suspend fun Player.task() {
        npc<Happy>("I'm sure I can think of a few little jobs. This won't be a quest, mind you, just a little favour...")
        if (random.nextInt(100) < 16) {
            val npc = setOf(
                "father_aereck",
                "high_priest_entrana",
                "reldo",
                "thurgo",
                "father_lawrence",
                "abbot_langley",
                "oracle",
                "thing_under_the_bed",
            ).random(random)
            val intro = EnumDefinitions.string("wise_old_man_npcs", npc)
            npc<Happy>(intro)
            set("wise_old_man_npc", npc)
            if (npc == "thing_under_the_bed") {
                set("wise_old_man_remaining", 1)
            } else {
                npc<Happy>("Here's the letter")
                if (!inventory.add("old_mans_message")) {
                    npc<Happy>("Please make room in your inventory to carry the letter.")
                    return
                }
            }
            hintNpc(npc)
            return
        }
        val amount = random.nextInt(3, 16)
        val item = tasks().random(random)
        set("wise_old_man_task", item)
        set("wise_old_man_remaining", amount)
        val intro = EnumDefinitions.string("wise_old_man_items", item)
        npc<Happy>("$intro. Please bring me $amount.")
        hintItem(item)
    }

    private suspend fun Player.hintNpc(npc: String) {
        choice("What would you like to say?") {
            option<Quiz>("Where do I need to go?") {
                npc<Neutral>(EnumDefinitions.string("wise_old_man_npc_hints", npc))
                player<Happy>("Right, I'll see you later.")
            }
            option<Happy>("Right, I'll see you later.")
        }
    }

    private suspend fun Player.hintItem(item: String) {
        choice("What would you like to say?") {
            option<Quiz>("Where can I get that?") {
                npc<Neutral>(EnumDefinitions.string("wise_old_man_item_hints", item))
                player<Happy>("Right, I'll see you later.")
            }
            option<Happy>("Right, I'll see you later.")
        }
    }

    private val hard = setOf(
        "ball_of_wool",
        "bowstring",
        "bread",
        "bronze_arrowtips",
        "bronze_knife",
        "bronze_warhammer",
        "bronze_wire",
        "headless_arrow",
        "swamp_paste",
        "iron_arrowtips",
        "iron_knife",
        "iron_warhammer",
        "leather_cowl",
        "pot_of_flour",
        "unfired_pie_dish",
        "unfired_pot",
        "leather_boots",
    )

    private fun Player.tasks(): MutableSet<String> {
        val tasks = mutableSetOf(
            "beer_glass",
            "bones",
            "bronze_arrow",
            "bronze_bar",
            "bronze_dagger",
            "bronze_hatchet",
            "beer",
            "cadava_berries",
            "cooked_chicken",
            "cooked_meat",
            "copper_ore",
            "cowhide",
            "egg",
            "feather",
            "grain",
            "soft_clay",
            "leather_gloves",
            "logs",
            "molten_glass",
            "raw_potato",
            "raw_rat_meat",
            "shrimps",
            "silk",
            "leather",
            "tin_ore",
            "ball_of_wool",
            "bowstring",
            "bread",
            "headless_arrow",
            "swamp_paste",
            "pot_of_flour",
            "unfired_pot",
        )
        if (has(Skill.Fishing, 15)) {
            tasks.add("anchovies")
        }
        if (has(Skill.Smithing, 2)) {
            tasks.add("bronze_mace")
        }
        if (has(Skill.Smithing, 3)) {
            tasks.add("bronze_med_helm")
        }
        if (has(Skill.Smithing, 4)) {
            tasks.add("bronze_wire")
        }
        if (has(Skill.Smithing, 5)) {
            tasks.add("bronze_spear")
            tasks.add("bronze_sword")
            tasks.add("bronze_arrowtips")
        }
        if (has(Skill.Smithing, 7)) {
            tasks.add("bronze_knife")
        }
        if (has(Skill.Smithing, 9)) {
            tasks.add("bronze_warhammer")
        }
        if (has(Skill.Smithing, 15)) {
            tasks.add("iron_bar")
        }
        if (has(Skill.Smithing, 17)) {
            tasks.add("iron_mace")
        }
        if (has(Skill.Smithing, 20)) {
            tasks.add("iron_arrowtips")
        }
        if (has(Skill.Smithing, 20)) {
            tasks.add("iron_knife")
        }
        if (has(Skill.Smithing, 24)) {
            tasks.add("iron_warhammer")
        }
        if (has(Skill.Mining, 17)) {
            tasks.add("iron_ore")
        }
        if (has(Skill.Crafting, 7)) {
            tasks.add("unfired_pie_dish")
            tasks.add("leather_boots")
        }
        if (has(Skill.Crafting, 9)) {
            tasks.add("leather_cowl")
        }
        if (questCompleted("rune_mysteries")) {
            tasks.add("rune_essence")
        }
        return tasks
    }
}
