package content.skill.agility.course

import content.entity.obj.objTeleportLand
import content.entity.obj.objTeleportTakeOff
import content.entity.sound.sound
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction

class ApeAtollDungeon : Script {

    init {
        objTeleportTakeOff("Enter", "ape_atoll_hole") {
            val weapon = player.equipped(EquipSlot.Weapon).id
            if (!weapon.endsWith("_greegree")) {
                return@objTeleportTakeOff
            }
            if (weapon == "small_ninja_monkey_greegree") {
                player.message("You scamper through the vine choked hole...")
            } else {
                player.message("Only the stealthiest and most agile monkey can use this!")
                cancel()
            }
        }

        objTeleportLand("Enter", "ape_atoll_hole") {
            if (player.equipped(EquipSlot.Weapon).id != "small_ninja_monkey_greegree") {
                player.message("You slip climbing down the hole and land hard on the floor.")
                player.anim("stand")
                player.sound("land_flat", delay = 5)
                player.face(Direction.WEST)
            } else {
                player.message("...and find yourself in front of a magnificent Monkey Nut bush.")
            }
        }

        objectOperate("Pick", "ape_atoll_monkey_nut_bush") {
            if (questCompleted("recipe_for_disaster")) {
                message("You have already made the King's meal, you don't need any more of these.")
            }
        }

        objTeleportTakeOff("Climb-up", "ape_atoll_hole_exit") {
            player.message("You climb back out of the cavern.")
        }
    }
}
