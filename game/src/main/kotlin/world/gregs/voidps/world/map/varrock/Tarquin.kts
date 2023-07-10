package world.gregs.voidps.world.map.varrock

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

on<NPCOption>({ operate && npc.id == "tarquin" && option == "Talk-To" }) { player: Player ->
    player<Talking>("Hello there.")
    npc<RollEyes>("Hello old bean. Is there something I can help you with?")
    choice {
        option<Talking>("Who are you?") {
            npc<RollEyes>("My name is Tarquin Marjoribanks.")
            npc<Talking>("I'd be surprised if you haven't already heard of me?")
            player<Unsure>("Why would I have heard of you Mr. Marjoribanks?")
            npc<Angry>("It's pronounced 'Marchbanks'!")
            npc<Talking>("""
                You should know of me because I am a member of the
                royal family of Misthalin!
            """)
            player<Unsure>("Are you related to King Roald?")
            npc<Cheerful>("Oh yes! Quite closely actually")
            npc<Talking>("I'm his 4th cousin, once removed on his mothers side.")
            player<Uncertain>("Er... Okay. What are you doing here then?")
            npc<Talking>("""
                I'm canoeing on the river! It's enormous fun!  Would
                you like to know how?
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
    npc<Cheerful>("""
        It's really quite simple to make. Just walk down to that
        tree on the bank and chop it down.
    """)
    npc<Cheerful>("""
        When you have done that you can shape the log
        further with your axe to make a canoe.
    """)
    npc<Cheerful>("""
        My personal favourite is the Stable Dugout canoe. A
        finer craft you'll never see old bean!
    """)
    npc<Cheerful>("""
        A Stable Dugout canoe will take you pretty much the
        length of the Lum river.
    """)
    npc<RollEyes>("Of course there are other canoes.")
    when (player.levels.get(Skill.Woodcutting)) {
        in 12..26 -> {
            npc<Surprised>("""
                Further up river, near the Barbarian Village, I saw
                some darned fool 'canoeing' on a log!
            """)
            npc<RollEyes>("""
                Unfortunately, you don't have the skill to create
                anything more than one of those logs. I dare say it will
                only get 1 stop down the river!
            """)
            npc<RollEyes>("Still, I'm sure it will satisfy one such as yourself.")
            player<Angry>("What's that supposed to mean?")
            npc<Angry>("""
                Do not profane the royal house of Varrock by
                engaging me in further discourse you knave!
            """)
            player<Surprised>("Pfft! I doubt he even knows the King!")
        }
        in 27..41 -> {
            npc<Cheerful>("You seem to be quite handy with an axe though!")
            npc<RollEyes>("""
                I'm sure you can build a Dugout canoe. Not as fine
                as a Stable Dugout but it will carry you 2 stops on the
                river.
            """)
            npc<RollEyes>("I should imagine it would suit your limited means.")
            player<Angry>("What do you mean when you say 'limited means'?")
            npc<Surprised>("Well, you're just an itinerant adventurer!")
            npc<Angry>("""
                What possible reason would you have for cluttering up
                my river with your inferior water craft!
            """)
        }
        in 42..56 -> {
            npc<Cheerful>("""
                Ah! Perfect! You can make a Stable Dugout canoe!
                One of those will carry you to any civilised place on the
                river.
            """)
            npc<Talking>("""
                If you were of good pedigree I'd let you join my boat
                club. You seem to be one of those vagabond
                adventurers though.
            """)
            player<Angry>("Charming!")
            npc<Angry>("Be off with you rogue!")
        }
        else -> {
            npc<Cheerful>("""
                My personal favourite is the Stable Dugout canoe. A
                finer craft you'll never see old bean!
            """)
            npc<Cheerful>("""
                A Stable Dugout canoe will take you pretty much the
                length of the Lum river.
            """)
            npc<RollEyes>("Of course there are other canoes.")
            npc<Surprised>("Well ... erm. You seem to be able to make a Waka!")
            player<Cheerful>("Sounds fun, what's a Waka.")
            npc<Talking>("""
                I've only ever seen one man on the river who uses a
                Waka. A big, fearsome looking fellow up near Edgeville.
            """)
            npc<Unsure>("""
                People say he was born in the Wilderness and that he
                is looking for a route back.
            """)
            player<Surprised>("Is that true!")
            npc<RollEyes>("""
                How should I know? I would not consort with such a
                base fellow!
            """)
        }
    }
}