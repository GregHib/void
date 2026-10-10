package content.skill.construction

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.type.Direction

/**
 * Games room elemental balances: activate to summon a sphere of elemental energy, banish it to put the balance back.
 * https://oldschool.runescape.wiki/w/Games_room
 */
class HouseElementalBalance : Script {
    init {
        objectOperate("Activate", "elemental_balance_*") { (target) ->
            if (get("house_build_mode", false)) {
                message("You cannot set up an elemental balance while in building mode.")
                return@objectOperate
            }
            areaSound("poh_sphere_powerup", target.tile, radius = 5)
            val sphere = NPCs.add(SPHERE, target.tile, Direction.SOUTH)
            sphere["balance_id"] = target.id
            // Npcs spawn at the start of the next tick, so the balance stays until then rather than leaving a gap
            delay(1)
            target.replace("invisible_seat", collision = false)
        }

        npcOperate("Banish", SPHERE) { (target) ->
            banish(target)
        }
    }

    private suspend fun Player.banish(sphere: NPC) {
        choice("Banish the sphere?") {
            option("Yes") {
                sphere.anim("elemental_orb_closes")
                areaSound("poh_sphere_powerdown", sphere.tile, radius = 5)
                delay(3)
                val id: String = sphere["balance_id", ""]
                NPCs.remove(sphere)
                GameObjects.findOrNull(sphere.tile, "invisible_seat")?.replace(id)
            }
            option("No")
        }
    }

    companion object {
        private const val SPHERE = "4021"
    }
}
