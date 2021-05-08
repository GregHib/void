import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.dialogue.Expression
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ npc.name == "drunken_musician" && option == "Talk-to" }) { player: Player ->
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
            player("Who are you?", Expression.Think)
            npc("""
                    Me? I'sh mooshian! Lemme her help youse relaxsh:
                    sit down, reshst your weery limz an' stuff.
                    You'll feel mush better. Like me, I ffeel great!
                """, Expression.Agree)
            player("You're drunk, aren't you?", Expression.Think)
            npc("I'm jus' relaxshed, mate.", Expression.Agree)
            player("I'm not sure I want to be as relaxed as you are.", Expression.Think)
            npc("""
                Youze'll never be as relaxshed as as I am,
                I worked hard to get this relaxshed.
            """, Expression.Agree)
            player("Clearly...", Expression.Disregard)
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
            player("So how does resting work?", Expression.Think)
            npc("""
                    Well, youze sit down and resht.
                    Then you feel better. Mush better.
                """, Expression.Agree)
            npc("""
                    If youze are lissening to my relaxshing moozik
                    then iss even bettar. Relaxshing moozik, like mine.
                """, Expression.Agree)
            player("Right; that's nice and clear. Thanks.", Expression.Disregard)
            resting()
        }
        2 -> {
            player("What's special about resting by a musician?", Expression.Cheerful)
            npc("""
                    Moozik's great! My moozik is the bessht.
                    Mush more relaxshing than those else.
                """, Expression.Agree)
            resting()
        }
        3 -> {
            player("Can you summarise the effects for me?", Expression.Cheerful)
            npc("""
                Yeshh, 'course. 'f youze sit down you resht.
                Moozik make reshting better.
            """, Expression.Agree)
            resting()
        }
        4 -> exit()
    }
}

suspend fun DialogueContext.exit() {
    player("That's all for now.", Expression.Disregard)
    npc("Fanks. Sshtay relaxshed!", Expression.Agree)
}