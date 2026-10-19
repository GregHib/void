package content.area.kandarin.yanille

import content.entity.combat.hit.damage
import content.entity.obj.door.enterDoor
import content.entity.obj.door.openDoor
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class YanilleAgilityDungeon : Script {

    private var balancingLedgeBusy = false
    private var obstaclePipeBusy = false

    init {
        objectOperate("Open", "gate_42_closed,gate_43_closed") { (target) ->
            if (target.tile.level == 0 &&
                (
                    (target.tile.x == 2593 && target.tile.y == 9490) ||
                        (target.tile.x == 2592 && target.tile.y == 9490)
                    )
            ) {
                val frumscone = NPCs.findBySpawn(Tile(2588, 9489), "wizard_frumscone")
                talkWith(frumscone)
                npc<Neutral>("You can't attack the Zombies in the room, my Zombies are for magic target practice only and should be attacked from the other side of the fence.")
                return@objectOperate
            }
        }

        objectOperate("Swing across", "yanille_dungeon_monkey_bars*") { (target) ->
            if (!has(Skill.Agility, 57)) {
                message("You need an Agility level of 57 to swing across the monkey bars.")
                return@objectOperate
            }
            swingAcrossMonkeyBars(target.tile)
        }

        objectOperate("Squeeze-through", "yanille_dungeon_obstacle_pipe") {
            if (!has(Skill.Agility, 49)) {
                message("You need an Agility level of 49 to squeeze through the pipe.")
                return@objectOperate
            }
            squeezeThroughPipe()
        }

        objectOperate("Walk-across", "yanille_dungeon_balancing_ledge*") { (target) ->
            if (!has(Skill.Agility, 40)) {
                message("You need an Agility level of 40 to walk across the balancing ledge.")
                return@objectOperate
            }
            walkAcrossBalancingLedge(target.tile)
        }

        objectOperate("Climb-up", "yanille_dungeon_rubble") {
            if (!has(Skill.Agility, 67)) {
                message("You need an Agility level of 67 to climb up the pile of rubble.")
                return@objectOperate
            }
            anim("climb_up")
            message("You climb up the pile of rubble...")
            delay(2)
            tele(Tile(2614, 9505))
            exp(Skill.Agility, 5.5)
        }

        objectOperate("Climb-down", "yanille_dungeon_rubble_2") {
            anim("climb_down")
            message("You climb down the pile of rubble...")
            delay(2)
            tele(Tile(2616, 9571))
        }

        objectOperate("Climb-up", "tithe_roof_g") { (target) ->
            if (target.tile.x == 2594 && target.tile.y == 9485 && target.tile.level == 0) {
                face(Direction.SOUTH)
                anim("climb_up")
                delay(2)
                tele(Tile(2594, 3086))
                face(Direction.SOUTH)
            }
        }

        objectOperate("Pray", "prayer_altar_chaos_yanille_agility_dungeon") {
            prayAtAltar()
            springTrapdoor()
        }

        objectOperate("Pray-at", "prayer_altar_chaos_yanille_agility_dungeon") {
            prayAtAltar()
            springTrapdoor()
        }

        objectOperate("Open", "door_70_closed") { (target) ->
            if (target.tile.x == 2601 && target.tile.y == 9482) {
                if (this.tile.y > 9481) {
                    enterDoor(target)
                } else {
                    message("The door is locked.")
                    sound("locked")
                }
                return@objectOperate
            }
            openDoor(target)
        }

        objectOperate("Pick-lock", "door_70_closed") { (target) ->
            if (target.tile.x != 2601 || target.tile.y != 9482) {
                openDoor(target)
                return@objectOperate
            }
            if (this.tile.y > 9481) {
                message("The door is already unlocked.")
                enterDoor(target)
                return@objectOperate
            }
            if (!inventory.contains("lockpick")) {
                message("You need a lockpick to pick this lock.")
                sound("locked")
                return@objectOperate
            }
            if (!has(Skill.Thieving, 82)) {
                message("You need a Thieving level of 82 to pick this lock.")
                sound("locked")
                return@objectOperate
            }
            message("You attempt to pick the lock.")
            sound("locked")
            if (success(levels.get(Skill.Thieving))) {
                message("You manage to pick the lock.")
                exp(Skill.Thieving, 50.0)
                enterDoor(target)
            } else {
                message("You fail to pick the lock.")
            }
        }
    }

    private fun Player.prayAtAltar() {
        if (levels.getOffset(Skill.Prayer) >= 0) {
            message("You already have full Prayer points.")
        } else {
            levels.set(Skill.Prayer, levels.getMax(Skill.Prayer))
            anim("altar_pray")
            message("You recharge your Prayer points.")
            set("prayer_point_power_task", true)
        }
    }

    private suspend fun Player.springTrapdoor() {
        delay(3)
        message("Suddenly a trap door opens beneath you...")
        delay()
        tele(Tile(2588, 9573))
        say("Oof!")
        sound("door_open")
        damage(150)
    }

    private fun success(level: Int): Boolean {
        val chance = when (level) {
            in 82..84 -> 35
            in 85..86 -> 36
            in 87..89 -> 37
            in 90..92 -> 38
            in 93..94 -> 39
            in 95..97 -> 40
            else -> 41
        }
        return random.nextInt(256) < chance
    }

    private suspend fun Player.swingAcrossMonkeyBars(tile: Tile) {
        val northToSouth = this.tile.y >= 9494
        val start = Tile(if (northToSouth) 2599 else 2598, if (northToSouth) 9494 else 9489, tile.level)
        val end = Tile(start.x, if (northToSouth) 9489 else 9494, tile.level)
        val failTile = if (northToSouth) {
            Tile(2599, 9555, tile.level)
        } else {
            Tile(2598, 9556, tile.level)
        }
        walkToDelay(start)
        clear("face_entity")
        face(end)
        anim("jump_onto_monkey_bars")
        sound("monkeybars_on")
        delay(1)
        renderEmote("monkey_bars")
        val disable = Settings["agility.disableCourseFailure", false]
        val success = disable || Level.success(levels.get(Skill.Agility), 74, 36)
        if (!success) {
            anim("rope_walk_fall_down")
            delay()
            message("You miss the hand hold and fall to the level below.")
            tele(failTile)
            clearRenderEmote()
            sound("monkeybars_off")
            damage((levels.get(Skill.Constitution) * 15) / 100 + 1)
            return
        }
        sound("monkeybars_loop", repeat = 8, delay = 40)
        walkOverDelay(end, forceWalk = true)
        anim("jump_from_monkey_bars")
        clearRenderEmote()
        sound("monkeybars_off")
        delay(2)
        this["monkey_business_task"] = true
        exp(Skill.Agility, 20.0)
    }

    private suspend fun Player.squeezeThroughPipe() {
        if (obstaclePipeBusy) {
            message("The pipe is being used.")
            return
        }
        obstaclePipeBusy = true
        val east = tile.x >= 2575
        val direction = if (east) Direction.WEST else Direction.EAST
        val start = Tile(if (east) 2578 else 2572, 9506)
        val middle = Tile(2575, 9506)
        val end = Tile(if (east) 2572 else 2578, 9506)
        try {
            walkToDelay(start)
            message("You squeeze into the pipe...")
            delay()
            clear("face_entity")
            face(direction)
            anim("climb_through_pipe")
            exactMoveDelay(middle, delay = 95, direction = direction, startDelay = 5)
            delay()
            anim("climb_through_pipe", delay = 20)
            exactMoveDelay(end, delay = 95, direction = direction, startDelay = 5)
            exp(Skill.Agility, 7.5)
        } finally {
            obstaclePipeBusy = false
        }
    }

    private suspend fun Player.walkAcrossBalancingLedge(tile: Tile) {
        if (balancingLedgeBusy) {
            message("The ledge is being used.")
            return
        }
        balancingLedgeBusy = true
        val northToSouth = this.tile.y > tile.y
        try {
            val direction = if (northToSouth) Direction.SOUTH else Direction.NORTH
            val start = tile.add(direction.inverse())
            val end = tile.addY(direction.delta.y * 7)
            val emote = if (northToSouth) "ledge_balance_3" else "ledge_balance_4"
            walkToDelay(start)
            message("You put your foot on the ledge and try to edge across...")
            clear("face_entity")
            face(direction)
            delay()
            renderEmote(emote)
            val disable = Settings["agility.disableCourseFailure", false]
            val success = disable || Level.success(levels.get(Skill.Agility), 64, 62)
            if (!success) {
                anim(if (northToSouth) "fall_off_log_right" else "fall_off_log_left")
                delay()
                clearRenderEmote()
                tele(Tile(2572, 9570))
                message("You lost your balance!")
                damage(((levels.get(Skill.Constitution) * 20) / 100).coerceAtMost(150))
                return
            }
            walkOverDelay(end, forceWalk = true)
            clearRenderEmote()
            message("You skillfully edge across the gap.")
            exp(Skill.Agility, 22.5)
        } finally {
            balancingLedgeBusy = false
        }
    }
}
