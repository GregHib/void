package content.area.karamja.brimhaven.agility_arena

import content.entity.combat.hit.damage
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.clearHint
import world.gregs.voidps.engine.client.markHint
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.network.login.protocol.encode.HintType
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import world.gregs.voidps.type.random

/**
 * Brimhaven Agility Arena: tag the ticket dispenser marked by the arrow, which moves to another pillar every minute.
 * Tagging it makes the player active, every pillar tagged while active earns a ticket; missing one makes them inactive.
 *
 * Cap'n Izzy runs the arena: he keeps the saw blades swinging and picks a new pillar and safe plank lane each minute.
 */
class BrimhavenAgilityArena : Script {

    val dispensers = mutableListOf<Tile>()

    // Each saw blade trap is a pair of single tile areas, the swinging blade covers both from the first
    val blades = mutableListOf<Pair<Tile, Tile>>()
    val arenaZones = mutableListOf<Zone>()

    // The pillar the arrow points at for everyone
    var active: Tile = Tile.EMPTY

    init {
        worldSpawn {
            dispensers.clear()
            for (row in Tables.get("brimhaven_arena_dispensers").rows()) {
                dispensers.add(row.tile("tile"))
            }
            blades.clear()
            for (i in 1..6 step 2) {
                blades.add(Areas["brimhaven_arena_saw_blade_$i"].first() to Areas["brimhaven_arena_saw_blade_${i + 1}"].first())
            }
            arenaZones.clear()
            arenaZones.addAll(Areas["brimhaven_agility_arena"].toZones(3))
        }

        objectOperate("Climb-Down", "brimhaven_arena_entrance_ladder") {
            // Free after Rocking Out or with Karamja gloves 3
            if (!questCompleted("rocking_out") && karamjaGloves() < 3) {
                if (!get("brimhaven_arena_paid", false)) {
                    npc<Neutral>("parrot_brimhaven", "Clap him in irons!")
                    npc<Neutral>("capn_izzy_no_beard_brimhaven", "Ahoy there! Pay up first!")
                    return@objectOperate
                }
                set("brimhaven_arena_paid", false)
            }
            anim("climb_down")
            delay(2)
            tele(2805, 9589, 3)
        }

        objectOperate("Climb-up", "brimhaven_arena_exit_ladder") {
            anim("climb_up")
            delay(2)
            tele(2809, 3193, 0)
        }

        objectOperate("Climb", "brimhaven_arena_climbing_rope") { (target) ->
            anim("climb_up")
            delay(2)
            tele(target.tile.x, target.tile.y, 3)
        }

        objectOperate("Tag", "brimhaven_arena_ticket_dispenser*") { (target) ->
            tag(target)
        }

        entered("brimhaven_agility_arena") {
            open("brimhaven_arena_overlay")
            if (active != Tile.EMPTY) {
                markHint(active, height = 65)
            }
        }

        exited("brimhaven_agility_arena") {
            close("brimhaven_arena_overlay")
            clearHint(HintType.TILE)
            clear("brimhaven_arena_tagged")
            set("brimhaven_arena_active", false)
            // In case dying or teleporting interrupted a dart trap's camera
            clearCamera()
        }

        npcSpawn("capn_izzy_no_beard_brimhaven") {
            softTimers.start("brimhaven_arena")
        }

        npcTimerStart("brimhaven_arena") {
            movePillar()
            BLADE_TICKS
        }

        npcTimerTick("brimhaven_arena") {
            val cycle = inc("brimhaven_arena_cycle")
            if (cycle % PILLAR_CYCLES == 0) {
                movePillar()
            } else if (cycle % 2 == 0 && occupied()) {
                swingBlades()
            }
            Timer.CONTINUE
        }
    }

    fun occupied() = arenaZones.any { Players.at(it).isNotEmpty() }

    /**
     * Point the arrow at a new pillar and swap the safe plank lane; players who didn't
     * tag the last pillar in time become inactive
     */
    fun movePillar() {
        val previous = active
        val options = dispensers.filter { it != previous }
        if (options.isEmpty()) {
            return
        }
        active = options.getOrElse(random.nextInt(options.size)) { options.last() }
        if (!occupied()) {
            return
        }
        swapPlanks()
        for (zone in arenaZones) {
            for (player in Players.at(zone)) {
                if (!player.get("brimhaven_arena_tagged", false)) {
                    player["brimhaven_arena_active"] = false
                }
                player.clear("brimhaven_arena_tagged")
                player.markHint(active, height = 65)
            }
        }
    }

    suspend fun Player.tag(target: GameObject) {
        if (target.tile != active) {
            message("Tag the pillar indicated by the yellow arrow to get an Agility ticket.")
            return
        }
        if (get("brimhaven_arena_tagged", false)) {
            message("You have already tagged this pillar.")
            return
        }
        set("brimhaven_arena_tagged", true)
        if (get("brimhaven_arena_active", false)) {
            addOrDrop("agility_arena_ticket")
            jingle("brimhaven_agility_ticket")
            set("just_the_ticket_task", true)
            message("You have received an Agility Arena Ticket!")
            item("agility_arena_ticket", "You have received an Agility Arena Ticket!")
            return
        }
        set("brimhaven_arena_active", true)
        message("You get tickets by tagging more than one pillar in a row, tag the next pillar!")
        statement("You get tickets by tagging more than one pillar in a row. <col=C04000>Tag the next pillar for a ticket!</col>")
    }

    fun swingBlades() {
        for ((left, right) in blades) {
            val start = GameObjects.findOrNull(left, "brimhaven_arena_blade_left") ?: continue
            GameObjects.replace(start, "brimhaven_arena_blade", shape = 10, ticks = BLADE_TICKS, collision = false)
            val end = GameObjects.findOrNull(right, "brimhaven_arena_blade_right") ?: continue
            GameObjects.remove(end, ticks = BLADE_TICKS, collision = false)
            // Anyone stood under the blade is knocked off the end of it, along the walkway
            for ((tile, other) in listOf(left to right, right to left)) {
                val away = Direction.of(tile.x - other.x, tile.y - other.y)
                for (player in Players.at(tile)) {
                    player.hitBySawBlade(tile.add(away), away.inverse())
                }
            }
        }
    }

    /**
     * One of the three lanes on each plank crossing is safe, the others have cracked planks
     */
    fun swapPlanks() {
        val safe = random.nextInt(3)
        for (row in Tables.get("brimhaven_arena_plank_lanes").rows()) {
            val id = if (row.int("lane") == safe) "brimhaven_arena_plank_middle" else "brimhaven_arena_plank_broken"
            val from = row.tile("from")
            for (x in from.x..row.tile("to").x) {
                val plank = GameObjects.getLayer(from.copy(x = x), ObjectLayer.GROUND_DECORATION) ?: continue
                if (plank.id != id) {
                    GameObjects.replace(plank, id)
                }
            }
        }
    }

    companion object {
        private const val BLADE_TICKS = 3

        // A minute between pillars
        private const val PILLAR_CYCLES = 33

        /**
         * Whether an arena obstacle or trap with an Agility [level] requirement is failed,
         * always failing below the requirement and never 70 or more levels above it.
         */
        fun Player.arenaFailed(level: Int, chance: Double): Boolean {
            val difference = levels.get(Skill.Agility) - level
            if (difference < 0) {
                return true
            }
            if (difference > 69 || Settings["agility.disableCourseFailure", false]) {
                return false
            }
            return (1 + difference) * 0.01 * random.nextDouble() <= random.nextDouble() * chance
        }

        /**
         * Failing an obstacle hits 20 to 60 depending on current life points
         */
        fun Player.arenaDamage() = (levels.get(Skill.Constitution) / 12).coerceIn(20, 60)

        /**
         * Tier of the Karamja gloves worn, 0 for none
         */
        fun Player.karamjaGloves() = equipped(EquipSlot.Hands).id.removePrefix("karamja_gloves_").toIntOrNull() ?: 0

        /**
         * Agility [experience] with 10% extra wearing Karamja gloves 2 or better, from obstacles and tickets alike
         */
        fun Player.arenaExp(experience: Double) {
            exp(Skill.Agility, if (karamjaGloves() >= 2) experience * 1.1 else experience)
        }

        /**
         * Knocked onto [tile] by a saw blade, facing [direction]
         */
        fun Player.hitBySawBlade(tile: Tile, direction: Direction) {
            if (queue.contains("brimhaven_arena_trap")) {
                return
            }
            steps.clear()
            strongQueue("brimhaven_arena_trap") {
                anim("saw_blade_hit")
                exactMoveDelay(tile, 30, direction)
                damage(arenaDamage())
                message("You were hit by the saw blade!")
            }
        }

        /**
         * Drop from the arena platforms into the pit below [tile]
         */
        fun Player.arenaFall(tile: Tile, message: String?) {
            tele(tile.x, tile.y, 0)
            damage(arenaDamage())
            if (message != null) {
                message(message)
            }
        }
    }
}
