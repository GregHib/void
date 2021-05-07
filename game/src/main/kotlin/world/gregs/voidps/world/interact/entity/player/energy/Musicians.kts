import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.dialogue.Expression
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ npc.def.name == "Musician" && option == "Talk-to" }) { player: Player ->
    player.talkWith(npc) {
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
            npc("""
                    Me? I'm a musician Let me help you relax: sit down,
                    rest your weary limbs and allow me to wash away the
                    troubles of the day.
                """, Expression.Agree)
            npc("""
                    After a long trek, what could be better than some
                    music to give you the energy to continue? 
                """, Expression.Agree)
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
        """,
        saySelection = false
    )
    when (choice) {
        1 -> {
            player("So how does resting work?", Expression.Think)
            npc("""
                    Have you ever been on a long journey, and simply
                    wanted to have a rest? When you're running from
                    city to city, it's so easy to run out of breath, don't you
                    find?
                """, Expression.Agree)
            player("Yes, I can never run as far as I'd like.", Expression.Disregard)
            npc("""
                    Well, you may rest anywhere, simply choose the Rest
                    option on the run buttons.
                """, Expression.Agree)
            npc("""
                    When you are nice and relaxed, you will recharge your
                    run energy more quickly and your life points twice as fast
                    as you would do so normally.
                """, Expression.Agree)
            npc("""
                    Of course, you can't do anything else while you're
                    resting, other than talk.
                """, Expression.Talking)
            player("Why not?", Expression.Disregard)
            npc("""
                    Well, you wouldn't be resting, now would you?
                    Also, you should know that resting by a musician, has
                    a similar effect but the benefits are greater.
                """, Expression.Agree)
            resting()
        }
        2 -> {
            player("What's special about resting by a musician?", Expression.Cheerful)
            npc("""
                    The effects of resting are enhanced by music. Your
                    run energy will recharge many times the normal rate,
                    and your life points three times as fast.
                """, Expression.Agree)
            npc("""
                    Simply sit down and rest as you would normally, nice
                    and close to the musician. You'll turn to face the
                    musician and hear the music. Like resting anywhere, if
                    you do anything other than talk, you will stop resting.
                """, Expression.Agree)
            resting()
        }
        3 -> {
            npc("""
                Certainly. You can rest anywhere, simply choose the Rest
                option on the run buttons.
            """, Expression.Agree)
            npc("""
                Resting anywhere will replenish your run energy more
                quickly than normal, your life points will replenish
                twice as fast as well! 
            """, Expression.Agree)
            npc("""
                Resting by a musician will replenish your run energy
                many times faster than normal, and your life points will
                also replenish three times as fast.
            """, Expression.Agree)
            resting()
        }
        4 -> exit()
    }
}

suspend fun DialogueContext.exit() {
    player("That's all for now.", Expression.Disregard)
    npc("Well, don't forget to have a rest every now and again.", Expression.Agree)
}