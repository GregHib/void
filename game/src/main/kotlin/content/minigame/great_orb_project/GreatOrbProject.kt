package content.minigame.great_orb_project

import content.entity.player.equip.Equipping
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class GreatOrbProject : Script {
    init {
        exited("runecrafting_guild") {
            if (tile !in Areas["air_altar"]) {
                close("rc_guild_overlay")
            }
        }

        itemOption("Make-barrier", "yellow_barrier_generator,green_barrier_generator") {
            message("You cannot create a barrier here.")
        }

        playerDespawn {
            leave(this)
        }

        itemAdded("*_attractor", "worn_equipment") {
            set("gop_wand", "attractor")
        }

        itemAdded("*_repeller", "worn_equipment") {
            set("gop_wand", "repeller")
        }

        npcApproach("Repel", "yellow_orb,green_orb") {
//            message("The yellow orbs have stopped responding to green wands.")
//            message("You fail to remove the barrier.") // https://youtu.be/01EWnc60K6o?t=235
//            message("You remove the barrier.") // https://youtu.be/01EWnc60K6o?t=235
        }

        npcApproach("Attract", "yellow_orb,green_orb") {
        }

        npcApproach("Change-wand", "yellow_orb,green_orb") {
            val wand = equipped(EquipSlot.Weapon).id
            val item = Item(if (wand.endsWith("_attractor")) wand.replace("_attractor", "_repeller") else wand.replace("_repeller", "_attractor"))
            val index = inventory.indexOf(item.id)
            if (index == -1) {
                return@npcApproach
            }
            Equipping.equip(this, item, index)
            steps.clear() // TODO correct?
            message("Swapping wands.")
        }

        destroyed("*_repeller,*_attractor,*_barrier_generator") {
            leave(this)
        }

        /*
            lobby portal = rcguild_portal_teleporter

            https://youtu.be/On1sAphh1Bo?t=83
            "Waiting for orbs to appear."
            npc<Neutral>("wizard_acantha", "Stop dawdling and enter the portal to the Air Altar.")
            message("You enter the portal.")
            message("You are pulled through the portal.") // When you wait around too long after a game
            TODO questions:
                - One timer for all?
                - Are projectiles from player -> orb or orb -> player or depends on attract/repell?

            npc<Happy>("wizard_vief", "Excellent! We claimed 5 altars and Acantha's followers only got 3! This should really put our efforts beyond that of Wizard Acantha's.")
            message("You did not have enough players to continue the game.")
            message("Wizard Vief gives you $tokens tokens.", Filter)
            npc<Unsure>("wizard_vief", "Hmmm, our team dill well, but you didn't make much of an effort did you? You must contribute at each altar!")
            https://youtu.be/Uaii7rWxnP4?t=263
            message("You cannot create a barrier this close to the altar.")
            message("<red>Warning<black>: Your activity bar is getting low! Help out with the projec tot refill it!")


            https://youtu.be/01EWnc60K6o?t=419
            npc<Upset>("wizard_acantha", "A stalemate? I could have done better blindfolded. How can you expect to save the altars when both teams claimed 2 altars?")
         */
    }

    companion object {
        fun cantJoin(player: Player): Boolean = player.inventory.spaces < 2 || player.equipped(EquipSlot.Hat).isNotEmpty() || player.equipped(EquipSlot.Hands).isNotEmpty()

        fun join(player: Player, team: String) {
            val success = player.equipment.transaction {
                set(EquipSlot.Hat.index, Item("runecrafter_hat_the_great_orb_project_$team"))
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
            if (player.tile !in Areas["runecrafting_guild"]) {
                player.tele(1696, 5464, 2)
            }
            player.clear("gop_team")
            player.clear("gop_wand")
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
