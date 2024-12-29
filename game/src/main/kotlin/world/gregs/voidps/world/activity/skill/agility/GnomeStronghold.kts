package world.gregs.voidps.world.activity.skill.agility

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.exactMove
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.suspend.delay
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

var Player.gnomeStage: Int
    set(value) {
        val current = this["gnome_course_stage", 0]
        if (value == current + 1 || value == 0) {
            this["gnome_course_stage"] = value
        }
    }
    get() = this["gnome_course_stage", 0]

objectOperate("Walk-across", "gnome_log_balance") {
    player.walkTo(Tile(2474, 3429), noCollision = true, noRun = true)
    player.message("You walk carefully across the slippery log...", ChatType.Filter)

    player.start("input_delay", 8)
    player.strongQueue("log-balance", 8) {
        player.gnomeStage++
        player.exp(Skill.Agility, 7.5)
        player.message("... and make it safely to the other side.", ChatType.Filter)
    }
}

objectOperate("Climb-over", "gnome_obstacle_net") {
    player.message("You climb the netting.", ChatType.Filter)
    player.setAnimation("climb_up")
    player.start("input_delay", 2)
    player.strongQueue("netting", 2) {
        player.gnomeStage++
        player.tele(player.tile.add(y = -2, level = 1))
        player.exp(Skill.Agility, 7.5)
    }
}

objectOperate("Climb", "gnome_tree_branch_up") {
    player.message("You climb the tree...", ChatType.Filter)
    player.setAnimation("climb_up")
    player.start("input_delay", 2)
    player.strongQueue("netting", 2) {
        player.gnomeStage++
        player.tele(player.tile.add(y = -3, level = 1))
        player.exp(Skill.Agility, 5.0)
    }
}

objectOperate("Walk-on", "gnome_balancing_rope") {
    player.walkTo(Tile(2483, 3420, 2), noCollision = true, noRun = true)
    player.start("input_delay", 7)
    player.renderEmote = "rope_balance"
    player.strongQueue("rope-balance", 7) {
        player.gnomeStage++
        player.clearRenderEmote()
        player.exp(Skill.Agility, 7.5)
        player.message("You passed the obstacle successfully.", ChatType.Filter)
    }
}

objectOperate("Walk-on", "gnome_balancing_rope_end") {
    player.walkTo(Tile(2477, 3420, 2), noCollision = true, noRun = true)
    player.start("input_delay", 7)
    player.renderEmote = "rope_balance"
    player.strongQueue("rope-balance", 7) {
        player.gnomeStage++
        player.clearRenderEmote()
        player.exp(Skill.Agility, 7.5)
        player.message("You passed the obstacle successfully.", ChatType.Filter)
    }
}

objectOperate("Climb-down", "gnome_tree_branch_down") {
    player.message("You climb the tree...", ChatType.Filter)
    player.setAnimation("climb_down")
    player.start("input_delay", 2)
    player.strongQueue("netting", 2) {
        player.gnomeStage++
        player.tele(player.tile.add(x = 1, y = 1, level = -2))
        player.exp(Skill.Agility, 5.0)
    }
}

objectOperate("Climb-over", "gnome_obstacle_net_free_standing") {
    player.message("You climb the netting.", ChatType.Filter)
    player.setAnimation("climb_up")
    player.start("input_delay", 2)
    player.strongQueue("netting", 2) {
        player.gnomeStage++
        player.tele(player.tile.add(y = 2))
        player.exp(Skill.Agility, 7.5)
    }
}

objectOperate("Squeeze-through", "gnome_obstacle_pipe_*") {
    if (player.tile.y >= target.y) {
        player.walkTo(target.tile.addY(-1))
        pipe(player, target, 2)
    } else {
        pipe(player, target, 1)
    }
}

fun pipe(player: Player, target: GameObject, initialDelay: Int) {
    player.strongQueue("obstacle_pipe", initialDelay) {
        player.face(Direction.NORTH)
        player.message("You pull yourself through the pipes..", ChatType.Filter)
        player.start("input_delay", 8)
        player.strongQueue("obstacle_pipe", 1) {
            player.setAnimation("10580")
            player.exactMove(target.tile.addY(2))
            player.strongQueue("obstacle_pipe", 4) {
                player.face(Direction.NORTH)
                player.tele(target.tile.addY(3))
                player.setAnimation("10580", delay = 1)
                player.exactMove(target.tile.addY(6))
                player.softQueue("reward", 4) {
                    if (player.gnomeStage == 5) {
                        player.gnomeStage = 0
                        player.exp(Skill.Agility, 39.5)
                    }
                    player.exp(Skill.Agility, 7.5)
                }
            }
        }
    }
}