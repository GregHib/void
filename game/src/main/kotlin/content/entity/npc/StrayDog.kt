package content.entity.npc

import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Leash
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.canFit
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

private const val BEHAVIOUR_TIMER = "stray_dog_behaviour"
private const val FOLLOW_TIMER = "stray_dog_follow"
private const val SHOOED_TIMER = "stray_dog_shooed"
private const val REACQUIRE_TIMER = "stray_dog_reacquire"

private const val TARGET_INDEX_KEY = "stray_dog_target_index"

private const val FIND_PLAYER_RADIUS = 4
private const val FOLLOW_RADIUS = 24
private const val RETREAT_DISTANCE = 6
private const val RETREAT_TICKS = 5
private const val BEHAVIOUR_TICKS = 3

private val ambientFollowTicks = TimeUnit.MINUTES.toTicks(5)
private val fedFollowTicks = TimeUnit.MINUTES.toTicks(5)
private val shooedTicks = TimeUnit.SECONDS.toTicks(12)
private val reacquireTicks = TimeUnit.SECONDS.toTicks(8)

private const val STRAY_DOG_IDS = "stray_dog_defender_of_varrock,stray_dog_defender_of_varrock_2,stray_dog_meiyerditch,stray_dog_meiyerditch_2"
private val acceptedFood = setOf("bones", "raw_meat", "raw_chicken", "cooked_meat", "cooked_chicken")

class StrayDog(private val steps: StepValidator) : Script {

    init {
        npcSpawn(STRAY_DOG_IDS) {
            softTimers.start(BEHAVIOUR_TIMER)
            set(TARGET_INDEX_KEY, -1)
        }

        npcTimerStart(BEHAVIOUR_TIMER) {
            BEHAVIOUR_TICKS
        }

        npcTimerTick(BEHAVIOUR_TIMER) {
            tickStrayDog(this)
            Timer.CONTINUE
        }

        npcOperate("Shoo-away", STRAY_DOG_IDS) { (target) ->
            shoo(target)
        }

        itemOnNPCOperate(npc = STRAY_DOG_IDS) { interact ->
            feed(interact.target, interact.item.id, interact.item.def.lendTemplateId != -1)
        }
    }

    private fun tickStrayDog(dog: NPC) {
        if (dog.dead) {
            dog.clearTarget()
            return
        }

        // Not implemented yet: stray dogs do not currently react to the player's pet cat.

        val target = dog.targetPlayer()
        if (target != null && dog.hasClock(FOLLOW_TIMER) && dog.canFollow(target)) {
            if (dog.mode !is Leash || (dog.mode as Leash).target != target) {
                dog.mode = Leash(dog, target, FOLLOW_RADIUS)
            }
            return
        }
        if (target != null) {
            val timedOut = !dog.hasClock(FOLLOW_TIMER)
            dog.clearTarget()
            if (timedOut) {
                dog.start(REACQUIRE_TIMER, reacquireTicks)
                return
            }
        }

        if (dog.hasClock(SHOOED_TIMER) || dog.hasClock(REACQUIRE_TIMER)) {
            return
        }

        dog.clearWatch()
        val next = dog.findNearbyPlayer() ?: return
        dog.follow(next, ambientFollowTicks)
    }

    private fun Player.shoo(dog: NPC) {
        val player = this
        queue("stray_dog_shoo_${player.index}_${dog.index}") {
            player.anim("emote_raspberry")
            player.say("Thbbbbt!")
            player.delay(2)
            if (dog.dead) {
                return@queue
            }
            dog.start(SHOOED_TIMER, shooedTicks)
            dog.whine()
            dog.runAwayFrom(player)
        }
    }

    private fun Player.feed(dog: NPC, itemId: String, borrowed: Boolean) {
        anim("climb_down")
        if (borrowed) {
            dog.say("Grrrr!")
            message("A borrowed object cannot be eaten by a stray dog.")
            return
        }
        if (itemId !in acceptedFood) {
            dog.say("Grrrr!")
            message("The dog doesn't seem interested in that.")
            return
        }
        if (!inventory.remove(itemId)) {
            return
        }
        dog.stop(SHOOED_TIMER)
        dog.follow(this, fedFollowTicks)
        dog.say("Woof woof!")
        message("The stray dog gobbles it up.")
    }

    private fun NPC.follow(player: Player, ticks: Int) {
        set(TARGET_INDEX_KEY, player.index)
        stop(REACQUIRE_TIMER)
        start(FOLLOW_TIMER, ticks)
        mode = Leash(this, player, FOLLOW_RADIUS)
    }

    private fun NPC.clearTarget() {
        set(TARGET_INDEX_KEY, -1)
        stop(FOLLOW_TIMER)
        if (mode is Leash) {
            mode = EmptyMode
        }
    }

    private fun NPC.targetPlayer(): Player? = Players.indexed(get(TARGET_INDEX_KEY, -1))

    private fun NPC.canFollow(player: Player): Boolean {
        if (player.dead || player["logged_out", false]) {
            return false
        }
        if (tile.level != player.tile.level) {
            return false
        }
        val spawn = get<Tile>("spawn_tile") ?: return false
        return tile.within(spawn, FOLLOW_RADIUS) &&
            player.tile.within(spawn, FOLLOW_RADIUS) &&
            this@StrayDog.steps.canFit(player.tile, collision, size, 0)
    }

    private fun NPC.findNearbyPlayer(): Player? {
        var preferred: Player? = null
        var preferredDistance = Int.MAX_VALUE
        Players.forEachInRadius(tile, FIND_PLAYER_RADIUS) { player ->
            if (!canFollow(player)) {
                return@forEachInRadius
            }
            val distance = tile.distanceTo(player.tile)
            if (distance < preferredDistance) {
                preferred = player
                preferredDistance = distance
            }
        }
        return preferred
    }

    private fun NPC.whine() {
        anim("stray_dog_whine")
        areaSound("fenris_wolf_death", tile)
        say("Whine!")
    }

    private fun NPC.runAwayFrom(target: Player) {
        clearTarget()
        steps.clear()
        val destination = retreatTileFrom(target) ?: return
        watch(target)
        walkTo(destination, forceWalk = true)
        queue.clear("stray_dog_retreat_end")
        queue("stray_dog_retreat_end", RETREAT_TICKS) {
            clearWatch()
            steps.clear()
            mode = EmptyMode
        }
    }

    private fun NPC.retreatTileFrom(target: Player) = retreatTilesFrom(target)
        .firstOrNull { it != tile && this@StrayDog.steps.canFit(it, collision, size, 0) }

    private fun NPC.retreatTilesFrom(target: Player): Sequence<Tile> {
        val x = deltaAway(tile.x, target.tile.x)
        val y = deltaAway(tile.y, target.tile.y)
        if (x == 0 && y == 0) {
            return (RETREAT_DISTANCE downTo 1).asSequence().flatMap { distance ->
                sequenceOf(
                    tile.add(distance, 0),
                    tile.add(-distance, 0),
                    tile.add(0, distance),
                    tile.add(0, -distance),
                    tile.add(distance, distance),
                    tile.add(distance, -distance),
                    tile.add(-distance, distance),
                    tile.add(-distance, -distance),
                )
            }
        }
        return (RETREAT_DISTANCE downTo 1).asSequence().flatMap { distance ->
            sequenceOf(
                tile.add(x * distance, y * distance),
                tile.add(x * distance, 0),
                tile.add(0, y * distance),
            ).distinct()
        }
    }

    private fun deltaAway(self: Int, other: Int): Int = when {
        self > other -> 1
        self < other -> -1
        else -> 0
    }
}
