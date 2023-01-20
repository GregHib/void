package world.gregs.voidps.world.map.edgeville

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.minimumCanoeLevel

on<NPCOption>({ npc.id == "hari" && option == "Talk-To" }) { player: Player ->
    player("talking", "Hello there.")
    npc("talking", "Hello.")
    var choice = choice("""
        Who are you?
        Can you teach me about Canoeing?
    """)
    when (choice) {
        1 -> {
            player("talking", "Who are you?")
            npc("talking", "My name is Hari.")
            player("unsure", "And what are you doing here Hari?")
            npc("talking", """
                Like most people who come to Edgeville, I am here to
                seek adventure in the Wilderness.
            """)
            npc("talking", """
                I found a secret underground river that will take me
                quite a long way north.
            """)
            player("unsure", "Underground river?  Where does it come out?")
            npc("talking", "It comes out in a pond located deep in the Wilderness.")
            npc("talking", """
                I had to find a very special type of canoe to get me up
                the river though, would you like to know more?
            """)
            choice = choice("""
                Yes
                No
            """)
            when (choice) {
                1 -> canoeing()
                2 -> player("talking", "No thanks, not right now.")
            }
        }
        2 -> canoeing()
    }
}

suspend fun Interaction.canoeing() {
    if (minimumCanoeLevel()) {
        return
    }
    npc("talking", """
        It's really quite simple to make. Just walk down to that
        tree on the bank and chop it down.
    """)
    npc("talking", """
        When you have done that you can shape the log
        further with your axe to make a canoe.
    """)
    when (player.levels.get(Skill.Woodcutting)) {
        in 12..26 -> {
            npc("talking", """
                I can sense you're still a novice woodcutter, you will
                only be able to make a log canoe at present.
            """)
            player("unsure", "Is that good?")
            npc("talking", """
                A log will take you one stop along the river. But you
                won't be able to travel into the Wilderness on it.
            """)
        }
        in 27..41 -> {
            npc("talking", """
                You are an average woodcutter. You should be able to
                make a Dugout canoe quite easily. It will take you 2
                stops along the river.
            """)
            player("unsure", "Can I take a dugout canoe to reach the Wilderness?")
            npc("laugh", "You would never make it there alive.")
            player("sad", "Best not to try then.")
        }
        in 42..56 -> {
            npc("talking", """
                You seem to be an accomplished woodcutter. You will
                easily be able to make a Stable Dugout
            """)
            npc("talking", """
                They are reliable enough to get you anywhere on this
                river, except to the Wilderness of course.
            """)
            npc("talking", "Only a Waka can take you there.")
            player("unsure", "A Waka? What's that?")
            npc("cheerful", """
                Come and ask me when you have improved your skills
                as a woodcutter.
            """)
        }
        else -> {
            npc("cheerful", """
                Your skills rival mine friend. You will certainly be able
                to build a Waka.
            """)
            player("unsure", "A Waka? What's that?")
            npc("cheerful", """
                A Waka is an invention of my people, it's an incredible
                strong and fast canoe and will carry you safely to any
                destination on the river.
            """)
            player("unsure", "Any destination?")
            npc("cheerful", """
                Yes, you can take a waka north through the
                underground portion of this river.
            """)
            npc("sad", """
                It will bring you out at a pond in the heart of the
                Wilderness. Be careful up there, many have lost more
                than their lives in that dark and twisted place.
            """)
        }
    }
}