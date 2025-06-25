package content.area.troll_country.god_wars_dungeon

import content.entity.sound.sound
import content.skill.melee.weapon.Weapon
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.obj.objectApproach
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

objectApproach("Grapple", "armadyl_pillar") {
    if (!Weapon.hasGrapple(player)) {
        return@objectApproach
    }

    if (player.tile.y > target.tile.y) {
        player.walkTo(Tile(2872, 5279, 2))
        player.message("You fire your grapple at the pillar...", type = ChatType.Filter)
        delay()
        player.sound("crossbow_grappling")
        player.anim("godwars_crossbow_swing")
        player.gfx("godwars_grapple_shoot")
        target.anim("godwars_grapple_swing")
        delay(2)
        player.exactMoveDelay(Tile(2872, 5274, 2), startDelay = 15, delay = 29, direction = Direction.SOUTH)
        player.exactMoveDelay(Tile(2872, 5269, 2), delay = 21, direction = Direction.SOUTH)
        player.message("...and swing safely to the other side.", type = ChatType.Filter)
        player.sound("land_flatter")
    }
}
