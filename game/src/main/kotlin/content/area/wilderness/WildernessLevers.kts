package content.area.wilderness

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.suspend.SuspendableContext
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import content.entity.obj.objTeleportLand
import content.entity.obj.objTeleportTakeOff
import content.entity.sound.sound

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
    player.anim("pull_lever")
    player.start("movement_delay", 3)
    delay(1)
}

objTeleportTakeOff("Pull", "lever_*") {
    delay = 3
    player.sound("teleport")
    player.gfx("teleport_modern")
    player.anim("teleport_modern")
}

objTeleportLand("Pull", "lever_*") {
    player.sound("teleport_land")
    player.gfx("teleport_land_modern")
    player.anim("teleport_land_modern")
    val message: String = obj.getOrNull("land_message") ?: return@objTeleportLand
    player.message(message, ChatType.Filter)
}
