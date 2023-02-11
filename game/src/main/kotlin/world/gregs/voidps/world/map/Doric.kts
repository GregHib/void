package world.gregs.voidps.world.map

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.incVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.contain.add
import world.gregs.voidps.engine.contain.contains
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.quest.refreshQuestJournal
import world.gregs.voidps.world.activity.quest.sendQuestComplete
import world.gregs.voidps.world.interact.dialogue.type.*
import world.gregs.voidps.world.interact.entity.sound.playJingle

on<NPCOption>({ npc.id == "doric" && option == "Talk-to" }) { player: Player ->
    when (player.getVar("dorics_quest", "unstarted")) {
        "started" -> {
            npc("unsure", "Have you got my materials yet, traveller?")
            if (player.inventory.contains("clay" to 6, "copper_ore" to 4, "iron_ore" to 2)) {
                player("cheerful", "I have everything you need!")
                npc("cheerful", """
                    Many thanks! Pass them here, please. I can spare you
                    some coins for your trouble, and please use my anvils
                    any time you want.
                """)
                takeOre()
            } else {
                noOre()
            }
        }
        "completed" -> {
            npc("talking", "Hello traveller, how is your metalworking coming along?")
            player("talking", "Not too bad, Doric.")
            npc("cheerful", "Good, the love of metal is a thing close to my heart.")
        }
        else -> unstarted()
    }
}

suspend fun Interaction.noOre() {
    player("sad", "Sorry, I don't have them all yet.")
    npc("talking", """
        Not to worry, stick at it. Remember, I need 6 clay, 4
        copper ore, and 2 iron ore.
    """)
    val choice = choice("""
        Where can I find those?
        Certainly, I'll be right back!
    """)
    when (choice) {
        1 -> {
            player("unsure", "Where can I find those?")
            npc("cheerful", """
                You'll be able to find all those ores in the rocks just
                inside the Dwarven Mine. Head east from here and
                you'll find the entrance in the side of Ice Mountain.
            """)
            if (player.levels.get(Skill.Mining) < 15) {
                player("sad", "But I'm not a good enough miner to get iron ore.")
                npc("talking", """
                    Oh well, you could practice mining until you can. Can't
                    beat a bit of mining - it's a useful skill. Failing that, you
                    might be able to find a more experienced adventurer to
                    buy the iron ore off.
                """)
            }
        }
        2 -> {
            player("cheerful", "Certainly, I'll be right back!")
        }
    }
}

suspend fun Interaction.unstarted() {
    npc("unsure", "Hello traveller, what brings you to my humble smithy?")
    var choice = choice("""
        I wanted to use your anvils.
        I want to use your whetstone.
        Mind your own business, shortstuff!
        I was just checking out the landscape.
        What do you make here?
    """)
    when (choice) {
        1 -> {
            player("talking", "I wanted to use your anvils.")
            npc("talking", """
                My anvils get enough work with my own use. I make
                pickaxes, and it takes a lot of hard work. If you could
                get me some more materials, then I could let you use<br>them.
            """)
            startQuest()
        }
        2 -> {
            player("talking", "I wanted to use your whetstone.")
            npc("talking", """
                The whetstone is for more advanced smithing, but I
                could let you use it as well as my anvils if you could
                get me some more materials.
            """)
            startQuest()
        }
        3 -> {
            player("angry", "Mind your own business, shortstuff!")
            npc("angry", """
                How nice to meet someone with such pleasant manners.
                Do come again when you need to shout at someone
                smaller than you!
            """)
        }
        4 -> {
            player("talking", "I was just checking out the landscape.")
            npc("cheerful", """
                Hope you like it. I do enjoy the solitude of my little
                home. If you get time, please say hi to my friends in
                the Dwarven Mine.
            """)
            choice = choice("""
                Dwarven Mine?
                Will do!
            """)
            when (choice) {
                1 -> {
                    player("unsure", "Dwarven Mine?")
                    npc("cheerful", """
                        Yep, the entrance is in the side of Ice Mountain just to
                        the east of here. They're a friendly bunch. Stop in at
                        Nurmof's store and buy one of my pickaxes!
                    """)
                }
                2 -> player("cheerful", "Will do!")
            }
        }
        5 -> {
            player("unsure", "What do you make here?")
            npc("cheerful", """
                I make pickaxes. I am the best maker of pickaxes in the
                whole of Gielinor.
            """)
            player("unsure", "Do you have any to sell?")
            npc("talking", "Sorry, but I've got a running order with Nurmof.")
            choice = choice("""
                Who's Nurmof?
                Ah, fair enough.
            """)
            when (choice) {
                1 -> {
                    player("unsure", "Who's Nurmof?")
                    npc("cheerful", """
                        Nurmof has a store over in the Dwarven Mine. You
                        can find the entrance on the side of Ice Mountain to
                        the east of here.
                    """)
                }
                2 -> player("talking", "Ah, fair enough.")
            }
        }
    }
}

suspend fun Interaction.startQuest() {
    if (player.levels.get(Skill.Mining) < 15) {
        statement("""
            Before starting this quest, be aware that one or more of your skill
            levels are lower than recommended.
        """)
    }
    val choice = choice(title = "Start Doric's Quest?", text = """
        Yes, I will get you the materials.
        No, hitting rocks is for the boring people, sorry.
    """)
    when (choice) {
        1 -> {
            player("cheerful", "Yes, I will get you the materials.")
            player.setVar("dorics_quest", "started")
            player.inventory.add("bronze_pickaxe")
            npc("talking", """
                Clay is what I use more than anything, to make casts.
                Could you get me 6 clay, 4 copper ore, and 2 iron ore,
                please? I could pay a little, and let you use my anvils.
                Take this pickaxe with you just in case you need it.
            """)
            player.refreshQuestJournal()
            if (player.inventory.contains("clay" to 6, "copper_ore" to 4, "iron_ore" to 2)) {
                player("cheerful", """
                    You know, it's funny you should require those exact
                    things!
                """)
                npc("unsure", "What do you mean?")
                player("cheerful", """
                    I can usually fit 28 things in my backpack and in a
                    world full of quite literally limitless possibilities, a complete
                    coincidence has occurred!
                """)
                npc("unsure", "I don't quite understand what you're saying?")
                player("cheerful", """
                    Well, out of pure coincidence, despite definitely not
                    knowing what you were about to request, I just so
                    happened to have carried those exact items!
                """)
                npc("surprised", """
                    Oh my, that is a coincidence! Pass them here, please. I
                    can spare you some coins for your trouble, and please
                    use my anvils any time you want.
                """)
                takeOre()
            }
        }
        2 -> {
            player("roll_eyes", "No, hitting rocks is for the boring people, sorry.")
            npc("uncertain", "That is your choice. Nice to meet you anyway.")
        }
    }
}

suspend fun Interaction.takeOre() {
    item("You hand the clay, copper, and iron to Doric.", "copper_ore", 600)
    player.inventory.transaction {
        remove("clay", 6)
        remove("copper_ore", 4)
        remove("iron_ore", 2)
    }
    questComplete()
}

fun Interaction.questComplete() {
    player.setVar("dorics_quest", "completed")
    player.playJingle("quest_complete_1")
    player.experience.add(Skill.Mining, 1300.0)
    player.inventory.add("coins", 180)
    player.refreshQuestJournal()
    player.incVar("quest_points")
    val lines = listOf(
        "1 Quest Point",
        "1300 Mining XP",
        "180 coins",
        "Use of Doric's Anvils"
    )
    player.sendQuestComplete("Doric's Quest", lines, Item("steel_pickaxe"))
}
