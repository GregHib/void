package world.gregs.voidps.world.activity.skill.agility.course

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

val npcs: NPCs by inject()

objectOperate("Walk-across", "gnome_log_balance") {
    player.agilityCourse("gnome")
    npcs.gnomeTrainer("Okay get over that log, quick quick!", listOf(Zone(878901), Zone(878900), Zone(876852)))
    player.renderEmote("rope_balance")
    player.message("You walk carefully across the slippery log...", ChatType.Filter)
    player.walkOverDelay(Tile(2474, 3429))
    player.clearRenderEmote()
    player.agilityStage(1)
    player.exp(Skill.Agility, 7.5)
    player.message("... and make it safely to the other side.", ChatType.Filter)
}

objectOperate("Climb-over", "gnome_obstacle_net") {
    arriveDelay()
    player.agilityCourse("gnome")
    npcs.gnomeTrainer("Move it, move it, move it!", listOf(Zone(8768252), Zone(876853)))
    player.message("You climb the netting...", ChatType.Filter)
    player.anim("climb_up")
    delay(2)
    player.agilityStage(2)
    player.tele(player.tile.x.coerceIn(2471, 2476), 3424, 1)
    player.exp(Skill.Agility, 7.5)
}

objectOperate("Climb", "gnome_tree_branch_up") {
    npcs.gnomeTrainer("That's it - straight up", listOf(Zone(5069109), Zone(5071157)))
    player.message("You climb the tree...", ChatType.Filter)
    player.anim("climb_up")
    delay(2)
    player.message("... to the platform above.", ChatType.Filter)
    player.agilityStage(3)
    player.tele(2473, 3420, 2)
    player.exp(Skill.Agility, 5.0)
}

objectOperate("Walk-on", "gnome_balancing_rope") {
    npcs.gnomeTrainer("Come on scaredy cat, get across that rope!", Zone(9263413))
    player.renderEmote("rope_balance")
    player.walkOverDelay(Tile(2483, 3420, 2))
    player.agilityStage(4)
    player.clearRenderEmote()
    player.exp(Skill.Agility, 7.5)
    player.message("You passed the obstacle successfully.", ChatType.Filter)
}

objectOperate("Walk-on", "gnome_balancing_rope_end") {
    player.renderEmote("rope_balance")
    player.walkOverDelay(Tile(2477, 3420, 2))
    player.clearRenderEmote()
    player.exp(Skill.Agility, 7.5)
    player.message("You passed the obstacle successfully.", ChatType.Filter)
}

objectOperate("Climb-down", "gnome_tree_branch_down") {
    player.message("You climb the tree...", ChatType.Filter)
    player.anim("climb_down")
    delay(2)
    player.agilityStage(5)
    player.tele(2486, 3420, 0)
    player.exp(Skill.Agility, 5.0)
}

objectOperate("Climb-over", "gnome_obstacle_net_free_standing") {
    player.agilityCourse("gnome")
    npcs.gnomeTrainer("My Granny can move faster than you.", Zone(876854))
    player.message("You climb the netting.", ChatType.Filter)
    player.anim("climb_up")
    delay(2)
    player.agilityStage(6)
    val direction = target.tile.delta(player.tile).toDirection().vertical()
    player.tele(player.tile.add(direction.delta).add(direction.delta))
    player.exp(Skill.Agility, 7.5)
}

objectOperate("Squeeze-through", "gnome_obstacle_pipe_*") {
    player.agilityCourse("gnome")
    delay()
    player.face(Direction.NORTH)
    player.message("You pull yourself through the pipes..", ChatType.Filter)
    delay()
    player.anim("climb_through_pipe")
    player.exactMoveDelay(target.tile.addY(2))
    player.face(Direction.NORTH)
    player.tele(target.tile.addY(3))
    player.anim("climb_through_pipe", delay = 1)
    player.exactMoveDelay(target.tile.addY(6))
    if (player.agilityStage == 6) {
        player.agilityStage = 0
        player.inc("gnome_course_laps")
        player.exp(Skill.Agility, 39.0)
    }
    player.exp(Skill.Agility, 7.5)
}