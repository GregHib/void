package world.gregs.voidps.world.map.wilderness

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.engine.suspend.delay
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.obj.Teleport
import world.gregs.voidps.world.interact.entity.sound.playSound

on<ObjectOption>({ operate && def.stringId.startsWith("lever_") && option == "Pull" }, Priority.HIGH) { _: Player ->
    arriveDelay()
    if (def.stringId == "lever_ardougne_edgeville" && player["wilderness_lever_warning", true]) {
        statement("Warning! Pulling the lever will teleport you deep into the Wilderness.")
        choice("Are you sure you wish to pull it?") {
            option("Yes I'm brave.") {
                pullLever(player)
            }
            option("Eeep! The Wilderness... No thank you.") {
                cancel()
                return@option
            }
            option("Yes please, don't show this message again.") {
                player["wilderness_lever_warning"] = false // TODO proper doomsayer variable
                pullLever(player)
            }
        }
        return@on
    }
    pullLever(player)
}

suspend fun CharacterContext.pullLever(player: Player) {
    player.message("You pull the lever...", ChatType.Filter)
    player.setAnimation("pull_lever")
    player.start("movement_delay", 3)
    delay(1)
}

on<Teleport>({ takeoff && obj.stringId.startsWith("lever_") && option == "Pull" }) { player: Player ->
    delay = 3
    teleport(player)
}

fun teleport(player: Player) {
    player.playSound("teleport")
    player.setGraphic("teleport_modern")
    player.setAnimation("teleport_modern")
}

on<Teleport>({ land && obj.stringId.startsWith("lever_") && option == "Pull" }) { player: Player ->
    player.playSound("teleport_land")
    player.setGraphic("teleport_land_modern")
    player.setAnimation("teleport_land_modern")
    val message: String = obj.getOrNull("land_message") ?: return@on
    player.message(message, ChatType.Filter)
}
