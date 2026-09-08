package content.minigame.mage_training_arena

import content.entity.gfx.areaGfx
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.entity.proj.shoot
import content.quest.clearInstance
import content.quest.instanceOffset
import content.quest.setInstanceLogout
import content.quest.smallInstance
import content.skill.magic.spell.removeSpellItems
import org.rsmod.game.pathfinder.StepValidator
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile

/**
 * Ten mazes copied into a private instance of region 13463; the player casts Telekinetic Grab on a
 * statue to slide it towards the side of the maze they stand on until it reaches the exit square.
 */
class TelekineticTheatre(private val stepValidator: StepValidator) : Script {

    init {
        onFloorItemApproach("modern_spellbook:telekinetic_grab", "guardian_statue") {
            approachRange(10)
            steps.clear()
            val statue = it.target
            face(statue.tile)
            if (hasClock("action_delay")) {
                return@onFloorItemApproach
            }
            val maze = currentMaze(this) ?: return@onFloorItemApproach
            val direction = direction(this, maze)
            if (!removeSpellItems("telekinetic_grab")) {
                return@onFloorItemApproach
            }
            start("action_delay", 3)
            anim("tele_grab_cast")
            gfx("tele_grab_cast")
            sound("tele_grab_cast")
            exp(Skill.Magic, Tables.int("spells.telekinetic_grab.xp") / 10.0)
            val clientTicks = shoot("tele_grab_travel", statue.tile)
            areaSound("tele_grab_impact", statue.tile, delay = clientTicks, radius = 10)
            areaGfx("tele_grab_impact", statue.tile, delay = clientTicks)
            delay(3)
            if (direction == null) {
                message("Invalid move!")
                return@onFloorItemApproach
            }
            strongQueue("mta_statue") {
                slide(this, maze, direction)
            }
        }

        // The statue sits inside the maze walls, so these work from a distance rather than walking to it.
        floorItemApproach("Observe") { (target) ->
            if (target.id != "guardian_statue") {
                return@floorItemApproach
            }
            steps.clear()
            observe(this)
        }

        floorItemApproach("Reset") { (target) ->
            if (target.id != "guardian_statue") {
                return@floorItemApproach
            }
            steps.clear()
            val maze = currentMaze(this) ?: return@floorItemApproach
            placeStatue(this, local(this, maze.tile("statue")))
        }

        npcOperate("Talk-to", "maze_guardian") { (target) ->
            player<Neutral>("Hi!")
            npc<Neutral>("Well done on releasing me. Would you like to try another maze?")
            choice {
                option<Neutral>("Yes please!") {
                    npc<Neutral>("Very well, I shall teleport you.")
                    NPCs.remove(target)
                    nextMaze(this)
                }
                option<Neutral>("No thanks.") {
                    npc<Neutral>("Very well. Talk to me if you want to move onto the next maze, or you can return to the entrance hall through the portal.")
                }
            }
        }

        npcOperate("Talk-to", "telekinetic_guardian") {
            player<Neutral>("Hi.")
            guardianMenu()
        }

        entered("mage_training_arena_telekinetic_theatre") {
            refreshSolved(this)
        }

        exited("mage_training_arena_telekinetic_theatre") {
            leave(this)
        }
    }

    private suspend fun Player.guardianMenu() {
        choice {
            option<Neutral>("What do I have to do in this room?") {
                npc<Neutral>("In this room you will see a maze within which one of my fellow Guardians has been turned to stone for the purpose of this exercise. You must move the statue using your telekinetic grab spell to the exit square at")
                npc<Neutral>("the edge of the maze to bring the Guardian back to life. Simply stand on the side that you wish for the statue to travel towards and cast the spell on the statue. Once you have solved the maze, the statue will change back")
                npc<Neutral>("into the Guardian and he will award you with Telekinetic Pizazz Points and teleport you to the next maze. You can switch to a better view of the maze by selecting the 'Observe' option on the statue and return")
                npc<Neutral>("your view to normal by selecting the same option again. There is also a 'Reset' option on the statue just in case things aren't going too well.")
                guardianMenu()
            }
            option<Neutral>("What are the rewards?") {
                npc<Neutral>("As well as the experience in casting magic, you will get Telekinetic Pizazz Points for each maze successfully solved and bonus points for completing five mazes in a row without returning to the entrance.")
                guardianMenu()
            }
            option<Neutral>("Got any tips that may help me?") {
                npc<Neutral>("Have a good look at the maze before you try to solve it because this can save you time and runes required to navigate the maze. Although you will still be getting magic experience for moving the statue incorrectly, you")
                npc<Neutral>("won't be progressing towards collecting Telekinetic Pizazz Points. Lastly, all the mazes can be solved in ten moves or less.")
                player<Neutral>("I see.")
            }
            option<Neutral>("Thanks, bye!")
        }
    }

    /**
     * Slides the statue one tile per tick in [direction] until it hits a wall or reaches the exit.
     */
    private suspend fun slide(player: Player, maze: RowDefinition, direction: Direction) {
        val end = local(player, maze.tile("end"))
        while (true) {
            val statue = statues[player.index] ?: return
            val tile = statue.tile
            val blocked = !stepValidator.canTravel(
                level = tile.level,
                x = tile.x,
                z = tile.y,
                offsetX = direction.delta.x,
                offsetZ = direction.delta.y,
                size = 1,
                extraFlag = 0,
                collision = CollisionStrategies.Normal,
            )
            if (blocked) {
                return
            }
            val next = tile.add(direction)
            player.sound("mta_move_statue")
            placeStatue(player, next)
            if (next == end) {
                win(player, maze)
                return
            }
            player.delay(1)
        }
    }

    private suspend fun win(player: Player, maze: RowDefinition) {
        statues.remove(player.index)?.let { FloorItems.remove(it) }
        NPCs.remove(guardians.remove(player.index))
        player["mage_training_arena_mazes_solved"] = player["mage_training_arena_mazes_solved", 0] or (1 shl maze.int("index"))
        val streak = player.inc("mage_training_arena_maze_streak")
        val bonus = streak >= 5
        if (bonus) {
            player["mage_training_arena_maze_streak"] = 0
            player.exp(Skill.Magic, 1000.0)
            player.addOrDrop("law_rune", 10)
        }
        PizazzPoints.add(player, "telekinetic", if (bonus) 10 else 2)
        if (player["mage_training_arena_camera", false]) {
            player["mage_training_arena_camera"] = false
            player.clearCamera()
        }
        refreshSolved(player)
        NPCs.add("maze_guardian", local(player, maze.tile("end")))
        AuditLog.event(player, "mta_maze_solved", maze.rowId, streak)
        player.statement("Congratulations! You have received two Telekinetic Pizazz Points!")
        if (bonus) {
            player.statement("Congratulations on solving five mazes in a row, have 8 bonus points, 10 law runes and extra magic XP!")
        }
    }

    private fun observe(player: Player) {
        if (player["mage_training_arena_camera", false]) {
            player["mage_training_arena_camera"] = false
            player.clearCamera()
            return
        }
        val maze = currentMaze(player) ?: return
        player.message("Click observe on the guardian to reset your camera.")
        player["mage_training_arena_camera"] = true
        val camera = local(player, maze.tile("camera"))
        if (maze.rowId == "maze_4") {
            player.moveCamera(camera.add(11, 5), 799, 95, 95)
            player.turnCamera(camera.add(-44, -10), 799, 95, 95)
            return
        }
        player.moveCamera(camera.addY(-10), 798, 95, 95)
        player.turnCamera(camera.addY(-10), 798, 95, 95)
    }

    private fun leave(player: Player) {
        statues.remove(player.index)
        guardians.remove(player.index)
        if (player["mage_training_arena_camera", false]) {
            player["mage_training_arena_camera"] = false
            player.clearCamera()
        }
        player.clearInstance()
        if (player["logged_out", false]) {
            player.tele(MageTrainingArena.lobby)
        }
    }

    companion object {
        val region = Region(13463)

        private val statues = HashMap<Int, FloorItem>()
        private val guardians = HashMap<Int, NPC>()

        private val mazes: List<RowDefinition>
            get() = Tables.get("mta_mazes").rows()

        suspend fun start(player: Player) {
            player.smallInstance(region, levels = 3)
            player.setInstanceLogout(MageTrainingArena.lobby)
            player.delay(1)
            nextMaze(player)
        }

        /**
         * Picks an unsolved maze other than the current one, refilling the set once all ten are solved.
         */
        fun nextMaze(player: Player) {
            val all = mazes
            var solved = player["mage_training_arena_mazes_solved", 0]
            if (all.indices.all { solved and (1 shl it) != 0 }) {
                solved = 0
                player["mage_training_arena_mazes_solved"] = 0
            }
            val current = player["mage_training_arena_telekinetic_maze", 0]
            val remaining = all.filter { solved and (1 shl it.int("index")) == 0 }
            val choices = remaining.filter { it.int("index") + 1 != current }.ifEmpty { remaining }
            val maze = choices.random()
            player["mage_training_arena_telekinetic_maze"] = maze.int("index") + 1
            NPCs.remove(guardians.remove(player.index))
            placeStatue(player, local(player, maze.tile("statue")))
            guardians[player.index] = NPCs.add("telekinetic_guardian", local(player, maze.tile("guardian")))
            player.tele(local(player, maze.tile("base")))
        }

        fun currentMaze(player: Player): RowDefinition? {
            val index = player["mage_training_arena_telekinetic_maze", 0] - 1
            return mazes.firstOrNull { it.int("index") == index }
        }

        fun statue(player: Player): FloorItem? = statues[player.index]

        fun placeStatue(player: Player, tile: Tile) {
            statues.remove(player.index)?.let { FloorItems.remove(it) }
            statues[player.index] = FloorItems.add(tile, "guardian_statue", owner = player)
        }

        /**
         * Converts a maze offset into a tile inside the player's instance.
         */
        fun local(player: Player, offset: Tile): Tile = region.tile.add(offset).add(player.instanceOffset())

        /**
         * The side of the maze the player stands on decides where the statue travels.
         */
        fun direction(player: Player, maze: RowDefinition): Direction? {
            val origin = region.tile.add(player.instanceOffset())
            val x = player.tile.x - origin.x
            val y = player.tile.y - origin.y
            return when {
                y >= maze.int("north") -> Direction.NORTH
                y <= maze.int("south") -> Direction.SOUTH
                x <= maze.int("west") -> Direction.WEST
                x >= maze.int("east") -> Direction.EAST
                else -> null
            }
        }

        fun refreshSolved(player: Player) {
            if (!player.hasOpen("mage_training_arena_telekinetic")) {
                return
            }
            player.interfaces.sendText("mage_training_arena_telekinetic", "solved", player["mage_training_arena_maze_streak", 0].toString())
        }
    }
}
