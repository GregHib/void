package content.area.misthalin.wizards_tower.runecrafting_guild

import content.entity.obj.ObjectTeleports
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import content.minigame.great_orb_project.GreatOrbProject
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.equals

class RunecraftingGuild(val teleports: ObjectTeleports) : Script {
    init {
        objTeleportTakeOff("Enter", "portal_runecrafting_guild") { target, _ ->
            anim("teleport_rc_guild")
            gfx("teleport_rc_guild")
            if (get("gop_team", "") != "") {
                statement("If you return to the Wizards' Tower you will be removed from your Great Orb Project team.")
                choice("Leave your team?") {
                    option("Leave") {
                        GreatOrbProject.leave(this)
                        val definition = teleports.get("Enter")[target.tile.id]!!
                        teleports.teleportContinue(this, definition, target, delay = Teleport.CONTINUE)
                    }
                    option("Stay")
                }
                return@objTeleportTakeOff Teleport.CANCEL
            }
            if (hasMax(Skill.Runecrafting, 50)) {
                Teleport.CONTINUE
            } else {
                statement("You must have level 50 Runecrafting to go through this portal.")
                Teleport.CANCEL
            }
        }

        objTeleportLand("Enter", "portal_runecrafting_guild*") { target, _ ->
            anim("teleport_land_rc_guild")
            gfx("teleport_land_rc_guild")
            if (target.tile.equals(1696, 5460, 2)) {
                face(Direction.WEST)
            } else {
                face(Direction.NORTH)
            }
        }

        objectOperate("Search", "rcguild_guild_bookcase*") {
            message("You search the books...")
            delay(2)
            message("None of them look very interesting.")
        }
    }
}
