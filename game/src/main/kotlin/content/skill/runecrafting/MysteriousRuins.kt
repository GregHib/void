package content.skill.runecrafting

import content.entity.obj.ObjectTeleports
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inject
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

        itemAdded("*_tiara", "worn_equipment", EquipSlot.Hat) {
            updateAltarVars(this)
        }

        itemRemoved("*_tiara", "worn_equipment", EquipSlot.Hat) {
            updateAltarVars(this)
        }

        itemAdded("omni_talisman_staff", "worn_equipment", EquipSlot.Weapon) {
            updateAltarVars(this)
        }

        itemRemoved("omni_talisman_staff", "worn_equipment", EquipSlot.Weapon) {
            updateAltarVars(this)
        }

        itemOnObjectOperate("*_talisman", "*_altar_ruins") { (target, item) ->
            if (target.id != "${item.id.removeSuffix("_talisman")}_altar_ruins") {
                return@itemOnObjectOperate
            }
            message("You hold the ${item.id.toSentenceCase()} towards the mysterious ruins.")
            anim("human_pickupfloor")
            delay(2)
            set("${item.id.removeSuffix("_talisman")}_altar_ruins", refresh = false, value = true)
            interactObject(target, "Enter", approachRange = -1)
            softQueue("clear_alter_varbit", 5) {
                set("${item.id.removeSuffix("_talisman")}_altar_ruins", refresh = false, value = false)
            }
        }

        objTeleportTakeOff("Enter", "*_altar_ruins_enter") { _, _ ->
            clearAnim()
            sound("teleport")
            message("You feel a powerful force talk hold of you...")
            Teleport.CONTINUE
        }

        objTeleportTakeOff("Enter", "*_altar_portal") { target, option ->
            if (target.id == "chaos_altar_portal" && !hasClock("chaos_altar_skip")) {
                softQueue("chaos_altar_check") {
                    statement("Warning! This portal will teleport you into the Wilderness.")
                    choice("Are you sure you wish to use this portal?") {
                        option("Yes, I'm brave.") {
                            start("chaos_altar_skip", 1)
                            teleports.teleport(player, target, option, target.def(player))
                        }
                        option("Eeep! The Wilderness... No thank you.") {
                            message("You decide not to use this portal.")
                            cancel()
                        }
                    }
                }
                return@objTeleportTakeOff Teleport.CANCEL
            }
            clearAnim()
            sound("teleport")
            message("You step through the portal...")
            return@objTeleportTakeOff Teleport.CONTINUE
        }

        objTeleportTakeOff("Climb-down", "chaos_altar_ladder_down") { target, _ ->
            if (target.tile.equals(2259, 4845, 1)) {
                message("The ladder is broken, I can't climb it.")
                return@objTeleportTakeOff Teleport.CANCEL
            }
            return@objTeleportTakeOff Teleport.CONTINUE
        }

        objTeleportTakeOff("Climb-up", "chaos_altar_ladder_up") { target, _ ->
            if (target.tile.equals(2259, 4845)) {
                message("The ladder is broken, I can't climb it.")
                return@objTeleportTakeOff Teleport.CANCEL
            }
            return@objTeleportTakeOff Teleport.CONTINUE
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
