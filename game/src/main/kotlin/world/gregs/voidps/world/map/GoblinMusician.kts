import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.dialogue.Expression
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ npc.name == "goblin_musician" && option == "Talk-to" }) { player: Player ->
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
            player(Expression.Think, "Who are you?")
            npc(Expression.Agree, """
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
            player(Expression.Think, "So how does resting work?")
            npc(Expression.Agree, """
                    You stoopid. Goblin sit down, goblin rest,
                    goblin feel better.
                """)
            resting()
        }
        2 -> {
            player(Expression.Cheerful, "What's special about resting by a musician?")
            npc(Expression.Agree, """
                    Drumming good! Make you feel better,
                    boom booms make you run longer!
                """)
            resting()
        }
        3 -> {
            player(Expression.Cheerful, "Can you summarise the effects for me?")
            npc(Expression.Agree, """
                Wot? You sit down, you rest.
                Listen to Thump-Thump is better.
            """)
            resting()
        }
        4 -> exit()
    }
}

suspend fun DialogueContext.exit() {
    player(Expression.Disregard, "That's all for now.")
    npc(Expression.Agree, "You listen to boom boom. Good!")
}