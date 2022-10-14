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


on<NPCOption>({ npc.id == "tarquin" && option == "Talk-To" }) { player: Player ->
    player.talkWith(npc) {
        player("talking", "Hello there.")
        npc("roll_eyes", "Hello old bean. Is there something I can help you with?")
        val choice = choice("""
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
        npc("talking", """
          Well, you don't look like you have the skill to make a
          canoe.
        """)
        npc("talking", "You need to have at least level 12 woodcutting.")
        npc("talking", """
          Once you are able to make a canoe it makes travel
          along the river much quicker!
        """)
    } else{
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
        if (player.levels.get(Skill.Woodcutting) > 26 && player.levels.get(Skill.Woodcutting) < 42) {
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
        } else if (player.levels.get(Skill.Woodcutting) > 41 && player.levels.get(Skill.Woodcutting) < 57) {
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
        } else if (player.levels.get(Skill.Woodcutting) > 56) {
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
        } else {
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
    }
}