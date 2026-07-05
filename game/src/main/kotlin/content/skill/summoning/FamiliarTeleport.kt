package content.skill.summoning

import content.area.wilderness.wildernessLevel
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.type.Tile

/** Familiar teleports (hunting cats, lava titan) refuse above this wilderness level, like jewellery. */
private const val FAMILIAR_TELEPORT_WILDERNESS_CAP = 20

/**
 * Teleports the owner to a familiar's home hunting ground ([tile]), refusing above level-20
 * wilderness. [name] is the familiar as it reads in the refusal message ("the graahk", ...).
 */
fun Player.familiarTeleport(tile: Tile, name: String): Boolean {
    if (wildernessLevel > FAMILIAR_TELEPORT_WILDERNESS_CAP) {
        message("You cannot teleport with $name above level $FAMILIAR_TELEPORT_WILDERNESS_CAP wilderness.")
        return false
    }
    return Teleport.teleport(this, tile, "jewellery")
}
