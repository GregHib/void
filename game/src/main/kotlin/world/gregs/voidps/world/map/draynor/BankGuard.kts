import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Surprised
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ npc.id == "bank_guard" && option == "Talk-to" }) { player: Player ->
    npc<Talk>("Good day, sir.")
    npc<Talk>("Yes?")
    val choice = choice("""
        Can I deposit my stuff here?
        That wall doesn't look very good.
        Sorry, I don't want anything.
    """)
    when (choice) {
        0 -> {
            player<Cheerful>("Hello. Can I deposit my stuff here?")
            npc<Talk>("No. I'm a security guard, not a bank clerk.")
            var choice = choice("""
                That wall doesn't look very good.
                Alright, I'll stop bothering you now.
            """)
            when(choice) {
                0 -> {
                    player<Talk>("That wall doesn't look very good.")
                    npc<Talk>("No, it doesn't.")
                    val choice = choice("""
                        Are you going to tell me what happened?
                        Alright, I'll stop bothering you now.
                    """)
                    when(choice) {
                        0 -> {
                            player<Cheerful>("Are you going to tell me what happened?")
                            npc<Talk>("I could do.")
                            npc<Cheerful>("Ok, go on!")
                            player<Talk>("""
                                Someone smashed the wall when
                                they were robbing the bank.
                            """)
                            player<Surprised>("Someone's robbed the bank?")
                            npc<Talk>("Yes.")
                            player<Surprised>("""
                                But... was anyone hurt?
                                Did they get anything valuable?
                            """)
                            npc<Talk>("""
                                Yes, but we were able to get more staff and mend the
                                wall easily enough.
                            """)
                            npc<Talk>("""
                                The Bank has already replaced all the stolen items that
                                belonged to customers.
                            """)
                            player<Surprised>("Oh, good... but the bank staff got hurt?")
                            npc<Talk>("Yes, but the new ones are just as good.")
                            player<Talk>("You're not very nice, are you?")
                            npc<Talk>("No-one's expecting me to be nice.")
                        }
                    }
                }
                1 -> player<Talk>("Alright, I'll stop bothering you now.")
            }

        }
        1 -> {
            player<Talk>("")
        }
        2 -> {
            player<Talk>("Sorry, I don't want anything.")
            npc<Talk>("Ok.")
        }

    }
}