package world.gregs.voidps.world.activity.skill.agility.course

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.exactMove
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.objectSpawn
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.suspend.delay
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

objectOperate("Climb", "pyramid_climbing_rocks_top") {
    player.face(Direction.WEST)
    player.strongQueue("agility_rocks", 1) {
        player.setAnimation("rocks_climb_up", 15)
        player.exactMove(Tile(3338, 2829), 150, Direction.WEST)
        delay(5)
        player.clearAnimation()
    }
}

objectOperate("Climb", "pyramid_climbing_rocks_top") {
    if(!player.has(Skill.Agility, 30)) {
        player.message("You must be level 30 agility or higher to climb down the rocks.")
        return@objectOperate
    }
    player.face(Direction.WEST)
    player.strongQueue("agility_rocks", 1) {
        player.setAnimation("rocks_climb_up", 15)
        player.exactMove(Tile(3352, 2827), 150, Direction.WEST)
        delay(5)
        player.clearAnimation()
    }
}

objectOperate("Climb-up", "agility_pyramid_stairs_start") {
    if(!player.has(Skill.Agility, 30)) {
        player.message("You must be level 30 agility or higher to climb down the rocks.")
        return@objectOperate
    }
    player.face(Direction.WEST)
    player.tele(3355, 2833, 1)
    player.agilityCourse("pyramid")
}

objectSpawn() {

}

enterArea("pyramid_obstacle_1") {

}

enterArea("pyramid_obstacle_1") {

}

objectOperate("Climb-up", "agility_pyramid_stairs_start") {
    if(!player.has(Skill.Agility, 30)) {
        player.message("You must be level 30 agility or higher to climb down the rocks.")
        return@objectOperate
    }
    player.face(Direction.WEST)
    player.tele(3355, 2833, 1)
    player.agilityCourse("pyramid")
}
