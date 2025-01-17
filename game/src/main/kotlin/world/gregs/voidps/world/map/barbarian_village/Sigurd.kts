package world.gregs.voidps.world.map.barbarian_village

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.minimumCanoeLevel

npcOperate("Talk-To", "sigurd") {
    player<Neutral>("Hello there.")
    npc<Drunk>("Ha Ha! Hello!")
    choice {
        option<Neutral>("Who are you?") {
            npc<Drunk>("I'm Sigurd the Great and Brainy.")
            player<Quiz>("Why do they call you the Great and Brainy?")
            npc<Drunk>("Because I invented the Log Canoe!")
            player<Quiz>("Log Canoe?")
            npc<Drunk>("Yeash! Me and my cousins were having a great party by the river when we decided to have a game of 'Smack The Tree'")
            player<Uncertain>("Smack the Tree?")
            npc<Drunk>("It's a game were you take it in turnsh shmacking a tree. First one to uproot the tree winsh!")
            npc<Drunk>("Anyway, I won the game with a flying tackle. The tree came loose and down the river bank I went, still holding the tree.")
            npc<Drunk>("I woke up a few hours later and found myself several miles down river. And thatsh how I invented the log canoe!")
            player<Chuckle>("So you invented the 'Log Canoe' by falling into a river hugging a tree?")
            npc<Frustrated>("Well I refined the design from the original you know!")
            npc<Drunk>("I cut all the branches off to make it more comfortable. I could tell you how to if you like?")
            choice {
                option("Yes") {
                    canoeing()
                }
                option("No") {
                    player<Neutral>("No thanks, not right now.")
                }
            }
        }
        option("Can you teach me about Canoeing?") {
            canoeing()
        }
    }
}

suspend fun SuspendableContext<Player>.canoeing() {
    if (minimumCanoeLevel()) {
        return
    }
    npc<Drunk>("It's really quite simple to make. Just walk down to that tree on the bank and chop it down.")
    npc<Drunk>("Then take your axe to it and shape it how you like!")
    when (player.levels.get(Skill.Woodcutting)) {
        in 12..26 -> {
            npc<Drunk>("You can make a log canoe like mine! It'll get you 1 stop down the river.")
            npc<Drunk>("There's some snooty fella down near the Champions Guild who reckons his canoes are better than mine. He's never said it to my face though.")
        }
        in 27..41 -> {
            npc<Drunk>("You could make a Dugout canoe with your woodcutting skill, but I don't see why you would want to.")
        }
        in 42..56 -> {
            npc<Drunk>("Well, you're pretty handy with an axe!")
            npc<Frustrated>("You could make Stable Dugout canoes, like that snooty fella Tarquin.")
            npc<Drunk>("He reckons his canoes are better than mine. He's never said it to my face though.")
        }
        else -> {
            npc<Drunk>("You look like you know your way around a tree, you can make a Waka canoe.")
            player<Quiz>("What's a Waka?")
            npc<Drunk>("I've only ever seen Hari using them. People say he's found a way to canoe the river underground and into the Wilderness")
            npc<Drunk>("Hari hangs around up near Edgeville")
            npc<Drunk>("He's a nice bloke.")
        }
    }
}
