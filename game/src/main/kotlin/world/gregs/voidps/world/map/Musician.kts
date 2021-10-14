import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ npc.id.startsWith("musician") && option == "Talk-to" }) { player: Player ->
    player.dialogue(npc) {
        choice()
    }
}

suspend fun DialogueContext.choice() {
    val choice = choice("""
        Who are you?
        Can I ask you some questions about resting?
        That's all for now
    """)
    when (choice) {
        1 -> {
            player("think", "Who are you?")
            npc("cheerful", """
                Me? I'm a musician Let me help you relax: sit down,
                rest your weary limbs and allow me to wash away the
                troubles of the day.
            """)
            npc("cheerful", """
                After a long trek, what could be better than some
                music to give you the energy to continue? 
            """)
            choice()
        }
        2 -> resting()
        3 -> exit()
    }
}

suspend fun DialogueContext.resting() {
    val choice = choice(
        title = "Can I ask you some questions about resting?",
        text = """
            How does resting work?
            What's special about resting by a musician?
            Can you summarise the effects for me?
            That's all for now.
        """
    )
    when (choice) {
        1 -> {
            player("think", "So how does resting work?")
            npc("cheerful", """
                Have you ever been on a long journey, and simply
                wanted to have a rest? When you're running from
                city to city, it's so easy to run out of breath, don't you
                find?
            """)
            player("disregard", "Yes, I can never run as far as I'd like.")
            npc("cheerful", """
                Well, you may rest anywhere, simply choose the Rest
                option on the run buttons.
            """)
            npc("cheerful", """
                When you are nice and relaxed, you will recharge your
                run energy more quickly and your life points twice as fast
                as you would do so normally.
            """)
            npc("talk", """
                Of course, you can't do anything else while you're
                resting, other than talk.
            """)
            player("disregard", "Why not?")
            npc("cheerful", """
                Well, you wouldn't be resting, now would you?
                Also, you should know that resting by a musician, has
                a similar effect but the benefits are greater.
            """)
            resting()
        }
        2 -> {
            player("happy", "What's special about resting by a musician?")
            npc("cheerful", """
                The effects of resting are enhanced by music. Your
                run energy will recharge many times the normal rate,
                and your life points three times as fast.
            """)
            npc("cheerful", """
                Simply sit down and rest as you would normally, nice
                and close to the musician. You'll turn to face the
                musician and hear the music. Like resting anywhere, if
                you do anything other than talk, you will stop resting.
            """)
            resting()
        }
        3 -> {
            player("happy", "Can you summarise the effects for me?")
            npc("cheerful", """
                Certainly. You can rest anywhere, simply choose the Rest
                option on the run buttons.
            """)
            npc("cheerful", """
                Resting anywhere will replenish your run energy more
                quickly than normal, your life points will replenish
                twice as fast as well! 
            """)
            npc("cheerful", """
                Resting by a musician will replenish your run energy
                many times faster than normal, and your life points will
                also replenish three times as fast.
            """)
            resting()
        }
        4 -> exit()
    }
}

suspend fun DialogueContext.exit() {
    player("disregard", "That's all for now.")
    npc("cheerful", "Well, don't forget to have a rest every now and again.")
}