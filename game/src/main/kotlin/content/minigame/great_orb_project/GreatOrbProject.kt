package content.minigame.great_orb_project

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class GreatOrbProject : Script {
    init {
        exited("runecrafting_guild") {
            // TODO check target isn't part of the minigame
            close("rc_guild_overlay")
        }

        itemOption("Make-barrier", "yellow_barrier_generator,green_barrier_generator") {
            message("You cannot create a barrier here.")
        }

        playerDespawn {
            leave(this)
        }
    }

    companion object {
        fun cantJoin(player: Player): Boolean {
            return player.inventory.spaces < 2 || player.equipped(EquipSlot.Hat).isNotEmpty() || player.equipped(EquipSlot.Hands).isNotEmpty()
        }

        fun join(player: Player, team: String) {
            val success = player.equipment.transaction {
                set(EquipSlot.Hat.index, Item("runecrafter_hat_the_great_orb_project_${team}"))
                set(EquipSlot.Weapon.index, Item("${team}_attractor"))
            }
            if (!success) {
                return
            }
            player["gop_team"] = team
            player.open("rc_guild_overlay")
            player.interfaces.sendVisibility("rc_guild_overlay", "green_layer", team == "green")
            player.interfaces.sendVisibility("rc_guild_overlay", "yellow_layer", team == "yellow")
            player.inventory.transaction {
                add("${team}_repeller")
                add("${team}_barrier_generator")
            }
        }

        fun leave(player: Player) {
            player.clear("gop_team")
            player.close("rc_guild_overlay")
            player.equipment.transaction {
                removeToLimit("green_repeller")
                removeToLimit("yellow_repeller")
                removeToLimit("green_attractor")
                removeToLimit("yellow_attractor")
                removeToLimit("runecrafter_hat_the_great_orb_project_green")
                removeToLimit("runecrafter_hat_the_great_orb_project_yellow")
            }
            player.inventory.transaction {
                removeToLimit("green_repeller")
                removeToLimit("yellow_repeller")
                removeToLimit("green_attractor")
                removeToLimit("yellow_attractor")
                removeToLimit("green_barrier_generator")
                removeToLimit("yellow_barrier_generator")
            }
        }
    }
}
