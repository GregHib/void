package content.quest.member.ghosts_ahoy

import content.entity.obj.objTeleportLand
import content.entity.obj.objTeleportTakeOff
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.type.Direction

objTeleportTakeOff("Jump-down", "ectopool_shortcut_rail") {
    if (!player.has(Skill.Agility, 58)) {
        player.message("You need an agility level of at least 58 to climb down this wall.")
        cancel()
        return@objTeleportTakeOff
    }
    delay = 1
    player.anim("jump_down")
}

objTeleportLand("Jump-down", "ectopool_shortcut_rail") {
    player.anim("jump_land")
}

objTeleportTakeOff("Jump-up", "ectopool_shortcut_wall") {
    if (!player.has(Skill.Agility, 58)) {
        player.message("You need an agility level of at least 58 to climb up this wall.")
        cancel()
        return@objTeleportTakeOff
    }
    move = { tile ->
        player.tele(tile.addX(1))
        player.exactMoveDelay(tile, startDelay = 49, delay = 68, direction = Direction.WEST)
    }
    player.anim("jump_up")
}
