package world.gregs.voidps.world.map.wilderness

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.suspend.delay
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.obj.teleportLand
import world.gregs.voidps.world.interact.entity.obj.teleportTakeOff
import world.gregs.voidps.world.interact.entity.sound.playSound

objectOperate("Pull", "lever_*", override = false) {
    if (target.id == "lever_ardougne_edgeville" && player["wilderness_lever_warning", true]) {
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
        return@objectOperate
    }
    pullLever(player)
}

suspend fun SuspendableContext<Player>.pullLever(player: Player) {
    player.message("You pull the lever...", ChatType.Filter)
    player.setAnimation("pull_lever")
    player.start("movement_delay", 3)
    delay(1)
}

teleportTakeOff("Pull", "lever_*") {
    delay = 3
    player.playSound("teleport")
    player.setGraphic("teleport_modern")
    player.setAnimation("teleport_modern")
}

teleportLand("Pull", "lever_*") {
    player.playSound("teleport_land")
    player.setGraphic("teleport_land_modern")
    player.setAnimation("teleport_land_modern")
    val message: String = obj.getOrNull("land_message") ?: return@teleportLand
    player.message(message, ChatType.Filter)
}
