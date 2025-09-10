package content.area.wilderness

import content.entity.obj.ObjectTeleport
import content.entity.obj.ObjectTeleports
import content.entity.obj.objTeleportLand
import content.entity.obj.objTeleportTakeOff
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import content.entity.sound.sound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.event.Script
@Script
class WildernessLevers {

    val teleports: ObjectTeleports by inject()
    
    init {
        objTeleportTakeOff("Pull", "lever_*") {
            if (obj.stringId == "lever_ardougne_edgeville" && player["wilderness_lever_warning", true]) {
                cancel()
                player.strongQueue("wilderness_lever_warning") {
                    statement("Warning! Pulling the lever will teleport you deep into the Wilderness.")
                    choice("Are you sure you wish to pull it?") {
                        option("Yes I'm brave.") {
                            pullLever(this@objTeleportTakeOff, target)
                        }
                        option("Eeep! The Wilderness... No thank you.") {
                            return@option
                        }
                        option("Yes please, don't show this message again.") {
                            player["wilderness_lever_warning"] = false
                            pullLever(this@objTeleportTakeOff, target)
                        }
                    }
                }
                return@objTeleportTakeOff
            }
            pullLever(player)
            move = { tile ->
                delay(1)
                player.anim("teleport_modern")
                player.sound("teleport")
                player.gfx("teleport_modern")
                delay(3)
                player.tele(tile)
            }
        }

        objTeleportLand("Pull", "lever_*") {
            player.sound("teleport_land")
            player.gfx("teleport_land_modern")
            player.anim("teleport_land_modern")
            val message: String = obj.getOrNull("land_message") ?: return@objTeleportLand
            player.message(message, ChatType.Filter)
        }

    }

    fun pullLever(player: Player) {
        player.message("You pull the lever...", ChatType.Filter)
        player.anim("pull_lever")
        player.start("movement_delay", 3)
    }
    
    suspend fun SuspendableContext<Player>.pullLever(teleport: ObjectTeleport, target: GameObject) {
        pullLever(player)
        delay(2)
        player.anim("teleport_modern")
        player.sound("teleport")
        player.gfx("teleport_modern")
        teleport.delay = 3
        teleport.cancelled = false
        val definition = teleports.get("Pull")[target.tile.id]!!
        teleports.teleportContinue(this, player, definition, teleport)
    }
    
}
