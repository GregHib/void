package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.minimumCanoeLevel

on<NPCOption>({ npc.id == "barfy_bill" && option == "Talk-To" }) { player: Player ->
    player("talking", "Hello there.")
    npc("talking", "Oh! Hello there.")
    var choice = choice("""
        Who are you?
        Can you teach me about Canoeing?
    """)
    when (choice) {
        1 -> {
            player("talking", "Who are you?")
            npc("talking", "My name is Ex Sea Captain Barfy Bill.")
            player("unsure", "Ex sea captain?")
            npc("sad", """
                Yeah, I bought a lovely ship and was planning to make
                a fortune running her as a merchant vessel.
            """)
            player("unsure", "Why are you not still sailing?")
            npc("sad", """
                Chronic sea sickness. My first, and only, voyage was
                spent dry heaving over the rails.
            """)
            npc("talking", """
                If I had known about the sea sickness I could have
                saved myself a lot of money.
            """)
            player("uncertain", "What are you up to now then?")
            npc("suspicious", """
                Well my ship had a little fire related problem.
                Fortunately it was well insured.
            """)
            npc("talking", """
                Anyway, I don't have to work anymore so I've taken to
                canoeing on the river.
            """)
            npc("cheerful", "I don't get river sick!")
            npc("unsure", "Would you like to know how to make a canoe?")
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
                Hah! I can tell just by looking that you lack talent in
                woodcutting.
            """)
            player("unsure", "What do you mean?")
            npc("cheerful", """
                No Callouses! No Splinters!  No camp fires littering the
                trail behind you.
            """)
            npc("cheerful", """
                Anyway, the only 'canoe' you can make is a log. You'll
                be able to travel 1 stop along the river with a log canoe.
            """)
        }
        in 27..41 -> {
            npc("cheerful", """
                With your skill in woodcutting you could make my
                favourite canoe, the Dugout. They might not be the
                best canoe on the river, but they get you where you're
                going.
            """)
            player("unsure", "How far will I be able to go in a Dugout canoe?")
            npc("cheerful", "You will be able to travel 2 stops on the river.")
        }
        in 42..56 -> {
            npc("cheerful", """
                The best canoe you can make is a Stable Dugout, one
                step beyond a normal Dugout.
            """)
            npc("cheerful", """
                With a Stable Dugout you can travel to any place on
                the river.
            """)
            player("unsure", "Even into the Wilderness?")
            npc("cheerful", """
                Not likely! I've heard tell of a man up near Edgeville
                who claims he can use a Waka to get up into the
                Wilderness.
            """)
            npc("unsure", """
                I can't think why anyone would wish to venture into
                that hellish landscape though.
            """)
        }
        else -> {
            npc("cheerful", """
                Hoo! You look like you know which end of an axe is
                which!
            """)
            npc("talking", """
                You can easily build one of those Wakas. Be careful if
                you travel into the Wilderness though.
            """)
            npc("afraid", "I've heard tell of great evil in that blasted wasteland.")
            player("talking", "Thanks for the warning Bill.")
        }
    }
}