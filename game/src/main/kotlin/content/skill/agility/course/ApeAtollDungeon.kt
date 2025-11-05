package content.skill.agility.course

import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction

class ApeAtollDungeon : Script {

    init {
        objTeleportTakeOff("Enter", "ape_atoll_hole") { _, _ ->
            val weapon = equipped(EquipSlot.Weapon).id
            if (!weapon.endsWith("_greegree")) {
                return@objTeleportTakeOff Teleport.CANCEL
            }
            if (weapon == "small_ninja_monkey_greegree") {
                message("You scamper through the vine choked hole...")
                return@objTeleportTakeOff Teleport.CONTINUE
            } else {
                message("Only the stealthiest and most agile monkey can use this!")
                return@objTeleportTakeOff Teleport.CANCEL
            }
        }

        objTeleportLand("Enter", "ape_atoll_hole") { _, _ ->
            if (equipped(EquipSlot.Weapon).id != "small_ninja_monkey_greegree") {
                message("You slip climbing down the hole and land hard on the floor.")
                anim("stand")
                sound("land_flat", delay = 5)
                face(Direction.WEST)
            } else {
                message("...and find yourself in front of a magnificent Monkey Nut bush.")
            }
        }

        objectOperate("Pick", "ape_atoll_monkey_nut_bush") {
            if (questCompleted("recipe_for_disaster")) {
                message("You have already made the King's meal, you don't need any more of these.")
            }
        }

        objTeleportTakeOff("Climb-up", "ape_atoll_hole_exit") { _, _ ->
            message("You climb back out of the cavern.")
            Teleport.CONTINUE
        }
    }
}
