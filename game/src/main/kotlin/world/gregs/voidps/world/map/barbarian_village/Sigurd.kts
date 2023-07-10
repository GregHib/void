package world.gregs.voidps.world.map.barbarian_village

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.minimumCanoeLevel

on<NPCOption>({ operate && npc.id == "sigurd" && option == "Talk-To" }) { player: Player ->
    player<Talking>("Hello there.")
    npc<Drunk>("Ha Ha! Hello!")
    choice {
        option<Talking>("Who are you?") {
            npc<Drunk>("I'm Sigurd the Great and Brainy.")
            player<Unsure>("Why do they call you the Great and Brainy?")
            npc<Drunk>("Because I invented the Log Canoe!")
            player<Unsure>("Log Canoe?")
            npc<Asleep>("""
                Yeash! Me and my cousins were having a great party
                by the river when we decided to have a game of 'Smack
                The Tree'
            """)
            player<Uncertain>("Smack the Tree?")
            npc<Asleep>("""
                It's a game were you take it in turnsh shmacking a
                tree. First one to uproot the tree winsh!
            """)
            npc<Asleep>("""
                Anyway, I won the game with a flying tackle. The tree
                came loose and down the river bank I went, still holding
                the tree.
            """)
            npc<Asleep>("""
                I woke up a few hours later and found myself several
                miles down river. And thatsh how I invented the log
                canoe!
            """)
            player<Laugh>("""
                So you invented the 'Log Canoe' by falling into a river
                hugging a tree?
            """)
            npc<Angry>("Well I refined the design from the original you know!")
            npc<Asleep>("""
                I cut all the branches off to make it more comfortable.
                I could tell you how to if you like?
            """)
            choice {
                option("Yes") {
                    canoeing()
                }
                option("No") {
                    player<Talking>("No thanks, not right now.")
                }
            }
        }
        option("Can you teach me about Canoeing?") {
            canoeing()
        }
    }
}

suspend fun Interaction.canoeing() {
    if (minimumCanoeLevel()) {
        return
    }
    npc<Asleep>("""
        It's really quite simple to make. Just walk down to that
        tree on the bank and chop it down.
    """)
    npc<Asleep>("Then take your axe to it and shape it how you like!")
    when (player.levels.get(Skill.Woodcutting)) {
        in 12..26 -> {
            npc<Asleep>("""
                You can make a log canoe like mine! It'll get you 1
                stop down the river.
            """)
            npc<Asleep>("""
                There's some snooty fella down near the Champions
                Guild who reckons his canoes are better than mine. He's
                never said it to my face though.
            """)
        }
        in 27..41 -> {
            npc<Asleep>("""
                You could make a Dugout canoe with your woodcutting
                skill, but I don't see why you would want to.
            """)
        }
        in 42..56 -> {
            npc<Asleep>("Well, you're pretty handy with an axe!")
            npc<Angry>("""
                You could make Stable Dugout canoes, like that snooty
                fella Tarquin.
            """)
            npc<Asleep>("""
                He reckons his canoes are better than mine. He's never
                said it to my face though.
            """)
        }
        else -> {
            npc<Asleep>("""
                You look like you know your way around a tree, you
                can make a Waka canoe.
            """)
            player<Unsure>("What's a Waka?")
            npc<Asleep>("""
                I've only ever seen Hari using them. People say he's
                found a way to canoe the river underground and into
                the Wilderness
            """)
            npc<Asleep>("Hari hangs around up near Edgeville")
            npc<Asleep>("He's a nice bloke.")
        }
    }
}
