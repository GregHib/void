package content.area.asgarnia.falador

import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.Use
import world.gregs.voidps.type.Tile

private val acceptedTiles = listOf(
    Tile(3005, 3376, 0),
    Tile(2999, 3375, 0),
    Tile(2996, 3377, 0),
    Tile(2989, 3378, 0),
)

@Use(option = "Climb", ids = ["giant_mole_lair_escape_rope"])
fun ObjectOption<Player>.exitMoleLair() {
    player.anim("climb_up")
    player.tele(acceptedTiles.random())
}