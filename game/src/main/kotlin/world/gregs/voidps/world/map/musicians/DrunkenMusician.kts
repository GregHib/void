package world.gregs.voidps.world.map.musicians

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.Drunk
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ npc.id == "drunken_musician" && option == "Talk-to" }) { player: Player ->
    choice()
}

suspend fun Interaction.choice() {
    val choice = choice("""
        Who are you?
        Can I ask you some questions about resting?
        That's all for now
    """)
    when (choice) {
        1 -> {
            player("unsure", "Who are you?")
            npc<Drunk>("""
                Me? I'sh mooshian! Lemme her help youse relaxsh:
                sit down, reshst your weery limz an' stuff.
                You'll feel mush better. Like me, I ffeel great!
            """)
            player("unsure", "You're drunk, aren't you?")
            npc<Drunk>("I'm jus' relaxshed, mate.")
            player("unsure", "I'm not sure I want to be as relaxed as you are.")
            npc<Drunk>("""
                Youze'll never be as relaxshed as as I am,
                I worked hard to get this relaxshed.
            """)
            player("roll_eyes", "Clearly...")
            choice()
        }
        2 -> resting()
        3 -> exit()
    }
}

suspend fun Interaction.resting() {
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
            player("unsure", "So how does resting work?")
            npc<Drunk>("""
                Well, youze sit down and resht.
                Then you feel better. Mush better.
            """)
            npc<Drunk>("""
                If youze are lissening to my relaxshing moozik
                then iss even bettar. Relaxshing moozik, like mine.
            """)
            player("unsure", "Right; that's nice and clear. Thanks.")
            resting()
        }
        2 -> {
            player("happy", "What's special about resting by a musician?")
            npc<Drunk>("""
                Moozik's great! My moozik is the bessht.
                Mush more relaxshing than those else.
            """)
            resting()
        }
        3 -> {
            player("happy", "Can you summarise the effects for me?")
            npc<Drunk>("""
                Yeshh, 'course. 'f youze sit down you resht.
                Moozik make reshting better.
            """)
            resting()
        }
        4 -> exit()
    }
}

suspend fun Interaction.exit() {
    player("unsure", "That's all for now.")
    npc<Drunk>("Fanks. Sshtay relaxshed!")
}