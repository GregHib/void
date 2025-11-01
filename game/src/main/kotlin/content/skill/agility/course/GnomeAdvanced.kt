package content.skill.agility.course

import content.entity.combat.hit.damage
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

class GnomeAdvanced : Script {

    val npcs: NPCs by inject()

    init {
        objectOperate("Climb-up", "gnome_tree_branch_advanced") {
            if (!has(Skill.Agility, 85, message = true)) {
                npc<Happy>("gnome_trainer", "Sorry mate, you're not experienced enough to try that route. I suggest you carry on over the balancing rope instead.")
                return@objectOperate
            }
            npcs.gnomeTrainer("Terrorbirds could climb faster than that!", Zone(9263413))
            message("You climb the tree...", ChatType.Filter)
            anim("climb_up")
            delay(2)
            message("... to an even higher platform.", ChatType.Filter)
            agilityStage(4)
            tele(tile.add(y = -1, level = 1))
            exp(Skill.Agility, 25.0)
        }

        objectApproach("Run-across", "gnome_sign_post_advanced") {
            npcs.gnomeTrainer("Come on! I'd be over there by now.", Zone(13457717))
            approachRange(1)
            arriveDelay()
            val disable = Settings["agility.disableCourseFailure", false]
            val success = disable || Level.success(levels.get(Skill.Agility), -8..286) // failure rate 4.68-1.17% from 85-88
            face(Direction.EAST)
            anim("gnome_wall_${if (success) "run" else "fail"}")
            delay(1)
            if (!success) {
                exactMoveDelay(Tile(2480, 3418, 3), 30, Direction.EAST)
                delay(5)
            }
            exactMoveDelay(Tile(2484, 3418, 3), if (success) 60 else 210, Direction.EAST)
            if (success) {
                exp(Skill.Agility, 25.0)
            } else {
                delay(3)
                anim("gnome_wall_stand")
                delay()
                damage((levels.get(Skill.Constitution) - 10).coerceAtMost(65))
            }
            // Skip stage so lap doesn't count at end
            if (success || Settings["agility.disableFailLapSkip", false]) {
                agilityStage(5)
            }
            clearAnim()
        }

        objectApproach("Swing-to", "gnome_pole_advanced") {
            var tile = tile
            if (tile.x == 2484) {
                tile = Tile(2485, 3418, 3)
            }
            steps.clear()
            face(Direction.NORTH)
            delay()
            anim("gnome_run_up")
            exactMoveDelay(tile.copy(y = 3421), 60, Direction.NORTH)
            anim("gnome_jump")
            exactMoveDelay(tile.copy(y = 3425), 30, Direction.NORTH)
            anim("gnome_swing")
            delay(4)
            exactMoveDelay(tile.copy(y = 3429), 30, Direction.NORTH)
            delay(4)
            exactMoveDelay(tile.copy(y = 3432), 30, Direction.NORTH)
            delay(1)
            agilityStage(6)
            exp(Skill.Agility, 25.0)
        }

        objectOperate("Jump-over", "gnome_barrier_advanced") {
            anim("gnome_jump_barrier")
            delay()
            exactMoveDelay(Tile(2485, 3434, 3), 30, Direction.NORTH)
            delay(1)
            tele(2485, 3436, 0)
            anim("gnome_pipe_land")
            if (agilityStage == 6) {
                agilityStage = 0
                inc("gnome_course_advanced_laps")
                exp(Skill.Agility, 605.0)
            }
            exp(Skill.Agility, 25.0)
        }
    }
}
