package world.gregs.voidps.world.map.varrock

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.minimumCanoeLevel

on<NPCOption>({ npc.id == "tarquin" && option == "Talk-To" }) { player: Player ->
    player("talking", "Hello there.")
    npc("roll_eyes", "Hello old bean. Is there something I can help you with?")
    var choice = choice("""
        Who are you?
        Can you teach me about Canoeing?
    """)
    when (choice) {
        1 -> {
            player("talking", "Who are you?")
            npc("roll_eyes", "My name is Tarquin Marjoribanks.")
            npc("talking", "I'd be surprised if you haven't already heard of me?")
            player("unsure", "Why would I have heard of you Mr. Marjoribanks?")
            npc("angry", "It's pronounced 'Marchbanks'!")
            npc("talking", """
                You should know of me because I am a member of the
                royal family of Misthalin!
            """)
            player("unsure", "Are you related to King Roald?")
            npc("cheerful", "Oh yes! Quite closely actually")
            npc("talking", "I'm his 4th cousin, once removed on his mothers side.")
            player("uncertain", "Er... Okay. What are you doing here then?")
            npc("talking", """
                I'm canoeing on the river! It's enormous fun!  Would
                you like to know how?
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
    npc("cheerful", """
        It's really quite simple to make. Just walk down to that
        tree on the bank and chop it down.
    """)
    npc("cheerful", """
        When you have done that you can shape the log
        further with your axe to make a canoe.
    """)
    npc("cheerful", """
        My personal favourite is the Stable Dugout canoe. A
        finer craft you'll never see old bean!
    """)
    npc("cheerful", """
        A Stable Dugout canoe will take you pretty much the
        length of the Lum river.
    """)
    npc("roll_eyes", "Of course there are other canoes.")
    when (player.levels.get(Skill.Woodcutting)) {
        in 12..26 -> {
            npc("surprised", """
                Further up river, near the Barbarian Village, I saw
                some darned fool 'canoeing' on a log!
            """)
            npc("roll_eyes", """
                Unfortunately, you don't have the skill to create
                anything more than one of those logs. I dare say it will
                only get 1 stop down the river!
            """)
            npc("roll_eyes", "Still, I'm sure it will satisfy one such as yourself.")
            player("angry", "What's that supposed to mean?")
            npc("angry", """
                Do not profane the royal house of Varrock by
                engaging me in further discourse you knave!
            """)
            player("surprised", "Pfft! I doubt he even knows the King!")
        }
        in 27..41 -> {
            npc("cheerful", "You seem to be quite handy with an axe though!")
            npc("roll_eyes", """
                I'm sure you can build a Dugout canoe. Not as fine
                as a Stable Dugout but it will carry you 2 stops on the
                river.
            """)
            npc("roll_eyes", "I should imagine it would suit your limited means.")
            player("angry", "What do you mean when you say 'limited means'?")
            npc("surprised", "Well, you're just an itinerant adventurer!")
            npc("angry", """
                What possible reason would you have for cluttering up
                my river with your inferior water craft!
            """)
        }
        in 42..56 -> {
            npc("cheerful", """
                Ah! Perfect! You can make a Stable Dugout canoe!
                One of those will carry you to any civilised place on the
                river.
            """)
            npc("talking", """
                If you were of good pedigree I'd let you join my boat
                club. You seem to be one of those vagabond
                adventurers though.
            """)
            player("angry", "Charming!")
            npc("angry", "Be off with you rogue!")
        }
        else -> {
            npc("cheerful", """
                My personal favourite is the Stable Dugout canoe. A
                finer craft you'll never see old bean!
            """)
            npc("cheerful", """
                A Stable Dugout canoe will take you pretty much the
                length of the Lum river.
            """)
            npc("roll_eyes", "Of course there are other canoes.")
            npc("surprised", "Well ... erm. You seem to be able to make a Waka!")
            player("cheerful", "Sounds fun, what's a Waka.")
            npc("talking", """
                I've only ever seen one man on the river who uses a
                Waka. A big, fearsome looking fellow up near Edgeville.
            """)
            npc("unsure", """
                People say he was born in the Wilderness and that he
                is looking for a route back.
            """)
            player("surprised", "Is that true!")
            npc("roll_eyes", """
                How should I know? I would not consort with such a
                base fellow!
            """)
        }
    }
}