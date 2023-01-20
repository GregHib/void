import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.choice

on<InterfaceOnObject>({ obj.id.startsWith("cooking_range") && item.id == "raw_beef" }, Priority.HIGH) { player: Player ->
    val choice = choice("""
        Dry the meat into sinew.
        Cook the meat.
    """)
    player["sinew"] = choice == 1
}