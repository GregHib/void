package content.skill.agility.course

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

class GnomeStronghold(val npcs: NPCs) : Script {

    init {
        objectOperate("Walk-across", "gnome_log_balance") {
            agilityCourse("gnome")
            npcs.gnomeTrainer("Okay get over that log, quick quick!", listOf(Zone(878901), Zone(878900), Zone(876852)))
            renderEmote("rope_balance")
            message("You walk carefully across the slippery log...", ChatType.Filter)
            walkOverDelay(Tile(2474, 3429))
            clearRenderEmote()
            agilityStage(1)
            exp(Skill.Agility, 7.5)
            message("... and make it safely to the other side.", ChatType.Filter)
        }

        objectOperate("Climb-over", "gnome_obstacle_net") {
            arriveDelay()
            agilityCourse("gnome")
            npcs.gnomeTrainer("Move it, move it, move it!", listOf(Zone(8768252), Zone(876853)))
            message("You climb the netting...", ChatType.Filter)
            anim("climb_up")
            delay(2)
            agilityStage(2)
            tele(tile.x.coerceIn(2471, 2476), 3424, 1)
            exp(Skill.Agility, 7.5)
        }

        objectOperate("Climb", "gnome_tree_branch_up") {
            npcs.gnomeTrainer("That's it - straight up", listOf(Zone(5069109), Zone(5071157)))
            message("You climb the tree...", ChatType.Filter)
            anim("climb_up")
            delay(2)
            message("... to the platform above.", ChatType.Filter)
            agilityStage(3)
            tele(2473, 3420, 2)
            exp(Skill.Agility, 5.0)
        }

        objectOperate("Walk-on", "gnome_balancing_rope") {
            npcs.gnomeTrainer("Come on scaredy cat, get across that rope!", Zone(9263413))
            renderEmote("rope_balance")
            walkOverDelay(Tile(2483, 3420, 2))
            agilityStage(4)
            clearRenderEmote()
            exp(Skill.Agility, 7.5)
            message("You passed the obstacle successfully.", ChatType.Filter)
        }

        objectOperate("Walk-on", "gnome_balancing_rope_end") {
            renderEmote("rope_balance")
            walkOverDelay(Tile(2477, 3420, 2))
            clearRenderEmote()
            exp(Skill.Agility, 7.5)
            message("You passed the obstacle successfully.", ChatType.Filter)
        }

        objectOperate("Climb-down", "gnome_tree_branch_down") {
            message("You climb the tree...", ChatType.Filter)
            anim("climb_down")
            delay(2)
            agilityStage(5)
            tele(2486, 3420, 0)
            exp(Skill.Agility, 5.0)
        }

        objectOperate("Climb-over", "gnome_obstacle_net_free_standing") { (target) ->
            agilityCourse("gnome")
            npcs.gnomeTrainer("My Granny can move faster than you.", Zone(876854))
            message("You climb the netting.", ChatType.Filter)
            anim("climb_up")
            delay(2)
            agilityStage(6)
            val direction = target.tile.delta(tile).toDirection().vertical()
            tele(tile.add(direction.delta).add(direction.delta))
            exp(Skill.Agility, 7.5)
        }

        objectOperate("Squeeze-through", "gnome_obstacle_pipe_*") { (target) ->
            agilityCourse("gnome")
            delay()
            face(Direction.NORTH)
            message("You pull yourself through the pipes..", ChatType.Filter)
            delay()
            anim("climb_through_pipe")
            exactMoveDelay(target.tile.addY(2))
            face(Direction.NORTH)
            tele(target.tile.addY(3))
            anim("climb_through_pipe", delay = 1)
            exactMoveDelay(target.tile.addY(6))
            if (agilityStage == 6) {
                agilityStage = 0
                inc("gnome_course_laps")
                exp(Skill.Agility, 39.0)
            }
            exp(Skill.Agility, 7.5)
        }
    }
}
