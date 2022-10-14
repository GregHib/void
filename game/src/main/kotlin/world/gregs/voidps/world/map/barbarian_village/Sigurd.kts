package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.dialogue.type.choice


on<NPCOption>({ npc.id == "sigurd" && option == "Talk-To" }) { player: Player ->
    player.talkWith(npc) {
        player("talking", "Hello there.")
        npc("drunk", "Ha Ha! Hello!")
        val choice = choice("""
          Who are you?
          Can you teach me about Canoeing?
        """)
        when (choice) {
            1 -> {
                player("talking", "Who are you?")
                npc("drunk", "I'm Sigurd the Great and Brainy.")
                player("unsure", "Why do they call you the Great and Brainy?")
                npc("drunk", "Because I invented the Log Canoe!")
                player("unsure", "Log Canoe?")
                npc("asleep", """
                    Yeash! Me and my cousins were having a great party
                    by the river when we decided to have a game of 'Smack
                    The Tree'
                """)
                player("uncertain", "Smack the Tree?")
                npc("asleep", """
                    It's a game were you take it in turnsh shmacking a
                    tree. First one to uproot the tree winsh!
                """)
                npc("asleep", """
                    Anyway, I won the game with a flying tackle. The tree
                    came loose and down the river bank I went, still holding
                    the tree.
                """)
                npc("asleep", """
                    I woke up a few hours later and found myself several
                    miles down river. And thatsh how I invented the log
                    canoe!
                """)
                player("laugh", """
                    So you invented the 'Log Canoe' by falling into a river
                    hugging a tree?
                """)
                npc("angry", "Well I refined the design from the original you know!")
                npc("asleep", """
                    I cut all the branches off to make it more comfortable.
                    I could tell you how to if you like?
                """)
                val choice = choice("""
                  Yes
                  No
                """)
                when (choice) {
                    1-> {
                        canoeing()
                    }
                    2 -> {
                        player("talking", "No thanks, not right now.")
                    }
                }
            }
            2 -> {
                canoeing()
            }
        }
    }
}

suspend fun DialogueContext.canoeing() {
    player("unsure", "Could you teach me about canoes?")
    if (player.levels.get(Skill.Woodcutting) < 12) {
        npc("asleep", """
          Well, you don't look like you have the skill to make a
          canoe.
        """)
        npc("talking", "You need to have at least level 12 woodcutting.")
        npc("asleep", """
          Once you are able to make a canoe it makes travel
          along the river much quicker!
        """)
    } else{
        npc("asleep", """
            It's really quite simple to make. Just walk down to that
            tree on the bank and chop it down.
        """)
        npc("asleep", "Then take your axe to it and shape it how you like!")
        if (player.levels.get(Skill.Woodcutting) > 26 && player.levels.get(Skill.Woodcutting) < 42) {
            npc("asleep", """
                You could make a Dugout canoe with your woodcutting
                skill, but I don't see why you would want to.
            """)
        } else if (player.levels.get(Skill.Woodcutting) > 41 && player.levels.get(Skill.Woodcutting) < 57) {
            npc("asleep", "Well, you're pretty handy with an axe!")
            npc("angry", """
                You could make Stable Dugout canoes, like that snooty
                fella Tarquin.
            """)
            npc("asleep", """
                He reckons his canoes are better than mine. He's never
                said it to my face though.
            """)
        } else if (player.levels.get(Skill.Woodcutting) > 56) {
            npc("asleep", """
                You look like you know your way around a tree, you
                can make a Waka canoe.
            """)
            player("unsure", "What's a Waka?")
            npc("asleep", """
                I've only ever seen Hari using them. People say he's
                found a way to canoe the river underground and into
                the Wilderness
            """)
            npc("asleep", "Hari hangs around up near Edgeville")
            npc("asleep", "He's a nice bloke.")
        } else {
            npc("asleep", """
                You can make a log canoe like mine! It'll get you 1
                stop down the river.
            """)
            npc("asleep", """
                There's some snooty fella down near the Champions
                Guild who reckons his canoes are better than mine. He's
                never said it to my face though.
            """)
        }
    }
}
