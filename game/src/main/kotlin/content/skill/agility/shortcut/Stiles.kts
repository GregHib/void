package content.skill.agility.shortcut

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.chat.obstacle
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Direction

objectOperate("Climb-over", "freds_farm_stile") {
    climbStile(Direction.NORTH)
}

objectOperate("Climb-over", "catherby_stile") {
    climbStile(Direction.NORTH)
}

objectOperate("Climb-over", "death_plateau_stile") {
    climbStile(Direction.NORTH)
}

objectOperate("Climb-over", "falconry_area_stile") {
    climbStile(Direction.NORTH)
}

objectOperate("Climb-over", "ardougne_farm_stile") {
    climbStile(Direction.EAST)
}

objectOperate("Climb-over", "falador_farm_stile") {
    val rotation = when (target.rotation) {
        2 -> Direction.NORTH
        3 -> Direction.EAST
        else -> return@objectOperate player.noInterest()
    }
    climbStile(rotation)
}

objectOperate("Climb-over", "vinesweeper_stile") {
    val rotation = when (target.rotation) {
        0, 2 -> Direction.NORTH
        1, 3 -> Direction.EAST
        else -> return@objectOperate player.noInterest()
    }
    climbStile(rotation)
}

objectOperate("Climb-over", "falador_crumbling_wall") {
    if (!player.has(Skill.Agility, 5)) {
        player.obstacle(5)
        return@objectOperate
    }
    climbStile(Direction.EAST)
    player.exp(Skill.Agility, 0.5)
}

suspend fun ObjectOption<Player>.climbStile(rotation: Direction) {
    val direction = when (rotation) {
        Direction.NORTH -> if (player.tile.y > target.tile.y) Direction.SOUTH else Direction.NORTH
        Direction.SOUTH -> if (player.tile.y < target.tile.y) Direction.NORTH else Direction.SOUTH
        Direction.EAST -> if (player.tile.x > target.tile.x) Direction.WEST else Direction.EAST
        Direction.WEST -> if (player.tile.x < target.tile.x) Direction.EAST else Direction.WEST
        else -> return player.noInterest()
    }
    val start = if (direction == rotation) target.tile else target.tile.minus(direction)
    player.walkOverDelay(start)
    player.face(direction)
    delay()
    player.anim("rocks_pile_climb")
    val target = if (direction == rotation) target.tile.add(direction) else target.tile
    player.exactMoveDelay(target, 30, direction = direction)
}
