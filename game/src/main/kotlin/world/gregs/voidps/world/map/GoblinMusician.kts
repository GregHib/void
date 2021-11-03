import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ npc.id == "goblin_musician" && option == "Talk-to" }) { player: Player ->
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
            player("unsure", "Who are you?")
            npc("cheerful_old", """
                Me? Thump-Thump.
                Me make thump-thumps with thump-thump drum.
                Other goblins listen.
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
            player("unsure", "So how does resting work?")
            npc("talk_old", """
                You stoopid. Goblin sit down, goblin rest,
                goblin feel better.
            """)
            resting()
        }
        2 -> {
            player("happy", "What's special about resting by a musician?")
            npc("talk_old", """
                Drumming good! Make you feel better,
                boom booms make you run longer!
            """)
            resting()
        }
        3 -> {
            player("happy", "Can you summarise the effects for me?")
            npc("talk_old", """
                Wot? You sit down, you rest.
                Listen to Thump-Thump is better.
            """)
            resting()
        }
        4 -> exit()
    }
}

suspend fun DialogueContext.exit() {
    player("unsure", "That's all for now.")
    npc("cheerful_old", "You listen to boom boom. Good!")
}