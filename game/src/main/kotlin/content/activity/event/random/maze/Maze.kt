package content.activity.event.random.maze

import content.activity.event.random.RandomEvents
import content.activity.event.random.kidnap
import content.activity.event.random.mysteriousOldMan
import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.closeTabs
import content.quest.instance
import content.quest.instanceOffset
import content.quest.openTabs
import content.quest.setInstanceLogout
import content.quest.smallInstance
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.Minimap
import world.gregs.voidps.engine.client.clearMinimap
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.minimap
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Maze random event; reach the shrine at the centre before the time runs out for a random event gift.
 * https://runescape.wiki/w/Maze
 */
class Maze : Script {

    private val duration = 300

    // Wall tile to the player tiles it can't be opened from
    private val oneWayWalls = mapOf(
        Tile(2904, 4573) to setOf(Tile(2904, 4573), Tile(2904, 4572), Tile(2904, 4574)),
        Tile(2906, 4586) to setOf(Tile(2906, 4586), Tile(2907, 4586)),
        Tile(2902, 4575) to setOf(Tile(2903, 4575)),
        Tile(2924, 4583) to setOf(Tile(2923, 4583)),
    )

    init {
        RandomEvents.register("maze") {
            startMaze()
        }

        timerStart("maze") { 1 }

        timerTick("maze") {
            val ticks = dec("maze_ticks")
            set("maze_timer_bar", ticks / 3)
            when {
                instance() == null -> {
                    RandomEvents.fail(this)
                    Timer.CANCEL
                }
                ticks <= 0 -> {
                    message("You failed to reach the centre of the maze in time.")
                    RandomEvents.fail(this)
                    Timer.CANCEL
                }
                else -> Timer.CONTINUE
            }
        }

        timerStop("maze") {
            close("maze_timer")
            openTabs()
            clearMinimap()
            clear("maze_ticks")
        }

        objectOperate("Touch", "strange_shrine") { (target) ->
            softTimers.stop("maze")
            // The player finishes on the shrine's footprint, so the engine's face_entity
            // auto-face resolves to the wrong direction. Clear it and face the centre tile.
            clear("face_entity")
            face(target.tile.add(target.width / 2, target.height / 2))
            delay(2)
            anim("emote_cheer")
            delay(2)
            addOrDrop("random_event_gift")
            RandomEvents.complete(this)
            jingle("maze_complete")
        }

        objectOperate("Open", "maze_wall") {
            message("That bit doesn't open.")
        }

        objectOperate("Open", "maze_door_*_closed") { (target) ->
            val blocked = oneWayWalls[target.tile.minus(instanceOffset())]
            if (blocked != null && tile.minus(instanceOffset()) in blocked) {
                statement("I don't think that's the right way.")
            } else {
                enterDoor(target)
            }
        }

        objectOperate("Open", "maze_chest") { (target) ->
            if (get("maze_ticks", 0) <= 0 || this["maze_chests_opened", 0] >= MAX_CHESTS) {
                message("You find nothing of interest.")
                return@objectOperate
            }
            inc("maze_chests_opened")
            anim("open_chest")
            target.replace("maze_chest_opened", ticks = 50)
            val row = Tables.get("maze_chest_rewards").rows().random(random)
            val reward = row.item("item")
            val amount = row.int("amount")
            addOrDrop(reward, amount)
            item(reward, row.string("text"))
        }

        objectOperate("Search", "maze_chest_opened") {
            message("You find nothing of interest.")
        }
    }

    private suspend fun Player.startMaze() {
        // Allocate the instance before the old-man intro so a resumed maze timer (on relog) sees a
        // live instance during the intro delay instead of failing the event.
        smallInstance(Region(MAZE_REGION), levels = 1)
        setInstanceLogout(Tile(this["random_event_origin", tile.id]))
        mysteriousOldMan()
        val start = Tables.get("maze_start_points").rows().random(random).tile("tile")
        kidnap(start.add(instanceOffset()))
        closeTabs()
        minimap(Minimap.HideMap)
        set("maze_ticks", duration)
        set("maze_timer_bar", duration / 3)
        open("maze_timer")
        softTimers.start("maze")
        message("You need to reach the maze centre, then you'll be returned to where you were.")
    }

    companion object {
        private const val MAZE_REGION = 11591
        private const val MAX_CHESTS = 10
    }
}
