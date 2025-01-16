package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.activity.quest.refreshQuestJournal
import world.gregs.voidps.world.activity.quest.sendQuestComplete
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*
import world.gregs.voidps.world.interact.entity.sound.playJingle

npcOperate("Talk-to", "cook_lumbridge") {
    when (player.quest("cooks_assistant")) {
        "unstarted" -> {
            npc<Sad>("What am I to do?")
            choice {
                option("What's wrong?") {
                    startQuest()
                }
                option<Happy>("Can you make me a cake?") {
                    npc<Sad>("*sniff* Don't talk to me about cakes...")
                    startQuest()
                }
                option<Neutral>("You don't look very happy.") {
                    dontLookHappy()
                }
                option<Happy>("Nice hat!") {
                    niceHat()
                }
            }
        }
        "started" -> started()
        else -> completed()
    }
}

suspend fun CharacterContext<Player>.started() {
    npc<Upset>("how are you getting on with finding the ingredients?")
    if (player.holdsItem("top_quality_milk")) {
        item("top_quality_milk", 500, "You give the top-quality milk to the cook.")
        player.inventory.remove("top_quality_milk")
        player["cooks_assistant_milk"] = 1
        player<Happy>("Here's some top-quality milk.")
    }
    if (player.holdsItem("extra_fine_flour")) {
        item("extra_fine_flour", 500, "You give the extra fine flour to the cook.")
        player.inventory.remove("extra_fine_flour")
        player["cooks_assistant_flour"] = 1
        player<Happy>("Here's the extra fine flour.")
    }
    if (player.holdsItem("super_large_egg")) {
        item("super_large_egg", 500, "You give the super large egg to the cook.")
        player.inventory.remove("super_large_egg")
        player["cooks_assistant_egg"] = 1
        player<Happy>("Here's a super large egg.")
    }
    if (player.holdsItem("egg") && (player["cooks_assistant_egg", 0] == 0)) {
        player<Talk>("I've this egg.")
        npc<Talk>("No, I need a super large egg. You'll probably find one near the local chickens.")
    }
    if (player.holdsItem("pot_of_flour") && (player["cooks_assistant_flour", 0] == 0)) {
        player<Talk>("I've this flour.")
        npc<Talk>("That's not fine enough. I imagine if you speak with Millie at the mill to the north she'll help you out.")
    }
    if (player.holdsItem("bucket_of_milk") && (player["cooks_assistant_milk", 0] == 0)) {
        player<Talk>("I've this milk.")
        npc<Talk>("Not bad, but not good enough. There's a milk maid that looks after the cows to the north-east. She might have some advice.")
    }
    if ((player["cooks_assistant_egg", 0] == 1) && (player["cooks_assistant_flour", 0] == 1) && player["cooks_assistant_milk", 0] == 1) {
        npc<Happy>("You've brought me everything I need I am saved! Thank you!")
        player<Happy>("So, do I get to go to the Duke's party?")
        npc<Upset>("I'm afraid not. Only the big cheeses get to dine with the Duke.")
        player<Talk>("Well, maybe one day, I'll be important enough to sit at the Duke's table.")
        npc<Talk>("Maybe, but I won't be holding my breath.")
        if (player.inventory.spaces < 2) {
            npc<Talk>("Ah, I have some rewards for you but your inventory seems to be full.")
        } else {
            questComplete()
        }
    } else if ((player["cooks_assistant_egg", 0] == 1) || (player["cooks_assistant_flour", 0] == 1) || player["cooks_assistant_milk", 0] == 1) {
        npc<Upset>("Thanks for the ingredients you have got so far. Please get the rest quickly. I'm running out of time! The Duke will throw me out onto the street!")
        stillNeed()
    } else {
        player<Talk>("I haven't got any of them yet, I'm still looking.")
        npc<Upset>("Please get the ingredients quickly. I'm running out of time! The Duke will throw me out onto the street!")
        stillNeed()
    }
}

suspend fun CharacterContext<Player>.completed() {
    npc<Happy>("Hello, friend, how is the adventuring going?")
    choice {
        option("I'm getting strong and mighty.") {
            player<Happy>("I'm getting strong and mighty. Grr.")
            npc<Happy>("Glad to hear it.")
        }
        option<Upset>("I keep on dying.") {
            npc<Happy>("Ah, well, at least you keep coming back to life too!")
        }
        option<Talk>("Can I use your range?") {
            canIUseRange()
        }
    }
}

fun CharacterContext<Player>.questComplete() {
    player["cooks_assistant"] = "completed"
    player.playJingle("quest_complete_1")
    player.inventory.add("sardine_noted", 20)
    player.experience.add(Skill.Cooking, 300.0)
    player.inventory.add("coins", 500)
    player.inc("quest_points")
    player.message("Congratulations, you've completed a quest: <navy>cook's assistant")
    player.refreshQuestJournal()
    val lines = listOf(
        "1 Quest Point",
        "300 Cooking XP",
        "500 coins",
        "20 sardines",
        "Access to the cook's range"
    )
    player.sendQuestComplete("cook's assistant", lines, Item("cake"))
}

suspend fun CharacterContext<Player>.startQuest() {
    player<Neutral>("What's wrong?")
    npc<Afraid>("Oh dear, oh dear, oh dear, I'm in a terrible terrible mess! It's the Duke's birthday today, and I should be making him a lovely big birthday cake using special ingredients...")
    npc<Afraid>("...but I've forgotten to get the ingredients. I'll never get them in time now. He'll sack me! What will I do? I have four children and a goat to look after. Would you help me? Please?")
    choice("Start the Cook's Assistant quest?") {
        option("Yes.") {
            player<Happy>("I'm always happy to help a cook in distress.")
            player["cooks_assistant"] = "started"
            player.refreshQuestJournal()
            npc<Happy>("Oh thank you, thank you. I must tell you that this is no ordinary cake, though - only the best ingredients will do! I need a super large egg, top-quality milk and some extra fine flour.")
            player.refreshQuestJournal()
            player<Quiz>("Where can I find those, then?")
            whereToFind()
        }
        option("No.") {
            player<Neutral>("No, I don't feel like it. Maybe later.")
            npc<Sad>("Fine. I always knew you Adventurer types were callous beasts. Go on your merry way!")
        }
    }
}

suspend fun CharacterContext<Player>.whereToFind() {
    npc<Quiz>("That's the problem: I don't exactly know. I usually send my assistant to get them for me but he quit.")
    npc<Talk>("I've marked some places on your world map in red. You might want to consider investigating them.")
}

suspend fun CharacterContext<Player>.stillNeed() {
    statement("You still need to get: ${if (player["cooks_assistant_milk", 0] == 0) "Some top-quality milk." else ""}${if (player["cooks_assistant_flour", 0] == 0) " Some extra fine flour." else ""}${if (player["cooks_assistant_egg", 0] == 0) " A super large egg." else ""}")
    choice {
        option<Happy>("I'll get right on it.")
        option("Where can I find the ingredients?") {
            whereToFind()
        }
    }
}

suspend fun CharacterContext<Player>.niceHat() {
    npc<Sad>("Err thank you. It's a pretty ordinary cooks hat really.")
    player<Happy>("Still, suits you. The trousers are pretty special too. ")
    npc<Sad>("Its all standard cook's issue uniform...")
    player<Happy>("The whole hat, apron, stripey trousers ensemble - it works. It make you looks like a real cook.")
    npc<Frustrated>("I am a real cook! I haven't got time to be chatting about Culinary Fashion. I am in desperate need of help!")
    startQuest()
}

suspend fun CharacterContext<Player>.canIUseRange() {
    npc<Happy>("Go ahead! It's very good range; it's better than most other ranges.")
    npc<Happy>("It's called the Cook-o-Matic 25 and it uses a combination of state-of-the-art temperature regulation and magic.")
    player<Talk>("Will it mean my food will burn less often?")
    npc<Happy>("As long as the food is fairly easy to cook in the first place!")
    if (player.holdsItem("cook_o_matic_manual")) {
        npc<Happy>("The manual you have in your inventory should tell you more.")
    } else if (player.inventory.isFull()) {
        npc<Upset>("I'd give you the manual, but you don't have room to take it. Ask me again when you have some space.")
    } else {
        npc<Happy>("Here, take this manual. It should tell you everything you need to know about this range.")
        player.inventory.add("cook_o_matic_manual")
        item("cook_o_matic_manual", 500, "The cook hands you a manual.")
    }
    player<Talk>("Thanks!")
}

suspend fun NPCOption<Player>.dontLookHappy() {
    npc<Sad>("No, I'm not. The world is caving in around me - I am overcome by dark feelings of impending doom.")
    choice {
        option("What's wrong?") {
            startQuest()
        }
        option<Neutral>("I'd take the rest of the day off if I were you.") {
            npc<Sad>("No, that's the worst thing I could do. I'd get in terrible trouble.")
            player<Neutral>("Well maybe you need to take a holiday...")
            npc<Sad>("That would be nice, but the Duke doesn't allow holidays for core staff.")
            player<Neutral>("Hmm, why not run away to the sea and start a new life as a Pirate?")
            npc<Sad>("My wife gets sea sick, and I have an irrational fear of eyepatches. I don't see it working myself.")
            player<Neutral>("I'm afraid I've run out of ideas.")
            npc<Sad>("I know I'm doomed.")
            startQuest()
        }
    }
}