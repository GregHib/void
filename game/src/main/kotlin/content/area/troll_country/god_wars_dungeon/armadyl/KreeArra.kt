package content.area.troll_country.god_wars_dungeon.armadyl

import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.canTravel
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class KreeArra(val stepValidator: StepValidator) : Script {

    var kilisa: NPC? = null
    var skree: NPC? = null
    var geerin: NPC? = null

    init {
        npcSpawn("kree_arra") {
            if (kilisa == null) {
                kilisa = NPCs.add("flight_kilisa", Tile(2833, 5297, 2))
            }
            if (skree == null) {
                skree = NPCs.add("wingman_skree", Tile(2840, 5303, 2))
            }
            if (geerin == null) {
                geerin = NPCs.add("flockleader_geerin", Tile(2828, 5299, 2))
            }
        }

        npcAttack("kree_arra", "ranged_teleport", ::knockBack)
        npcAttack("kree_arra", "magic_teleport", ::knockBack)

        npcDespawn("flight_kilisa") {
            kilisa = null
        }

        npcDespawn("wingman_skree") {
            skree = null
        }

        npcDespawn("flockleader_geerin") {
            geerin = null
        }
    }

    fun knockBack(npc: NPC, target: Character) {
        val direction = target.tile.delta(npc.tile).toDirection()
        if (knockBack(target, direction)) {
            return
        }
        if (direction.isDiagonal()) {
            if (knockBack(target, direction.horizontal())) {
                return
            }
            if (knockBack(target, direction.vertical())) {
                return
            }
        }
    }

    fun knockBack(target: Character, direction: Direction): Boolean {
        if (stepValidator.canTravel(target, direction.delta.x, direction.delta.y)) {
            target.gfx("kree_arra_stun", delay = 100)
            target.anim("kree_arra_stun", delay = 100)
            target.sound("kree_arra_stun")
            return true
        }
        return false
    }
}
