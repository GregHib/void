import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.choice

on<InterfaceOnObject>({ obj.id.startsWith("cooking_range") && item.id == "raw_beef" }, Priority.HIGH) { player: Player ->
    player.action(ActionType.Cooking) {
        withContext(NonCancellable) {
            player.dialogue {
                val choice = choice("""
                    Dry the meat into sinew.
                    Cook the meat.
                """)
                player["sinew"] = choice == 1
            }
            delay(1)
            player.awaitDialogues()
        }
    }
}