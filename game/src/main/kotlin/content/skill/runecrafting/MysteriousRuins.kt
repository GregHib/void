package content.skill.runecrafting

import content.entity.obj.ObjectTeleports
import content.entity.obj.objTeleportTakeOff
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import content.entity.sound.sound
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.equals

class MysteriousRuins : Script {

    val teleports: ObjectTeleports by inject()

    val omni = listOf("air", "mind", "water", "earth", "fire", "body", "cosmic", "law", "nature", "chaos", "death", "blood")

    init {
        playerSpawn {
            if (equipped(EquipSlot.Hat).id.endsWith("_tiara") || equipped(EquipSlot.Weapon).id == "omni_talisman_staff") {
                updateAltarVars(this)
            }
        }

        itemAdded("*_tiara", EquipSlot.Hat, "worn_equipment") { player ->
            updateAltarVars(player)
        }

        itemRemoved("*_tiara", EquipSlot.Hat, "worn_equipment") { player ->
            updateAltarVars(player)
        }

        itemAdded("omni_talisman_staff", EquipSlot.Weapon, "worn_equipment") { player ->
            updateAltarVars(player)
        }

        itemRemoved("omni_talisman_staff", EquipSlot.Weapon, "worn_equipment") { player ->
            updateAltarVars(player)
        }

        itemOnObjectOperate("*_talisman", "*_altar_ruins") {
            if (target.id != "${item.id.removeSuffix("_talisman")}_altar_ruins") {
                return@itemOnObjectOperate
            }
            player.message("You hold the ${item.id.toSentenceCase()} towards the mysterious ruins.")
            player.anim("human_pickupfloor")
            delay(2)
            player.interactObject(target, "Enter", approachRange = -1)
        }

        objTeleportTakeOff("Enter", "*_altar_ruins_enter") {
            player.clearAnim()
            player.sound("teleport")
            player.message("You feel a powerful force talk hold of you...")
        }

        objTeleportTakeOff("Enter", "*_altar_portal") {
            if (target.id == "chaos_altar_portal" && !player.hasClock("chaos_altar_skip")) {
                player.softQueue("chaos_altar_check") {
                    statement("Warning! This portal will teleport you into the Wilderness.")
                    choice("Are you sure you wish to use this portal?") {
                        option("Yes, I'm brave.") {
                            player.start("chaos_altar_skip", 1)
                            teleports.teleport(player, target, option, obj)
                        }
                        option("Eeep! The Wilderness... No thank you.") {
                            player.message("You decide not to use this portal.")
                            cancel()
                        }
                    }
                }
                cancel()
                return@objTeleportTakeOff
            }
            player.clearAnim()
            player.sound("teleport")
            player.message("You step through the portal...")
        }

        objTeleportTakeOff("Climb-down", "chaos_altar_ladder_down") {
            if (target.tile.equals(2259, 4845, 1)) {
                player.message("The ladder is broken, I can't climb it.")
                cancel()
                return@objTeleportTakeOff
            }
        }

        objTeleportTakeOff("Climb-up", "chaos_altar_ladder_up") {
            if (target.tile.equals(2259, 4845)) {
                player.message("The ladder is broken, I can't climb it.")
                cancel()
                return@objTeleportTakeOff
            }
        }
    }

    fun updateAltarVars(player: Player) {
        val tiara = player.equipped(EquipSlot.Hat).id.removeSuffix("_tiara")
        val staff = player.equipped(EquipSlot.Weapon).id
        for (type in omni) {
            player["${type}_altar_ruins"] = type == tiara || tiara == "omni" || staff == "omni_talisman_staff"
        }
    }
}
