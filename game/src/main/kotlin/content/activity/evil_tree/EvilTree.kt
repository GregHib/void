package content.activity.evil_tree

import com.github.michaelbull.logging.InlineLogger
import content.entity.effect.transform
import content.entity.player.dialogue.type.statement
import content.skill.woodcutting.Hatchet
import content.skill.woodcutting.Woodcutting
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.engine.suspend.awaitDialogues
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class EvilTree : Script {

    private val logger = InlineLogger()

    internal var tree: GameObject = GameObject(0)
    internal var leprechaun: NPC = NPC()
    internal var health: Int = 0
    internal var maxHealth: Int = 0
    internal val roots = mutableMapOf<String, Root>()
    internal val fires = mutableMapOf<String, GameObject>()
    private var growth: Int = 0
    private var strikes: Int = 0
    private var growthDelay: Int = 0
    private var rootDelay: Int = 0
    private var deathDelay: Int = 0
    private var respawnTicks: Int = 0

    init {
        worldSpawn {
            if (Settings["events.evilTree.enabled", false]) {
                schedule(STARTUP_MINUTES)
            }
        }

        settingsReload {
            if (!Settings["events.evilTree.enabled", false]) {
                World.timers.clear("evil_tree_spawn")
                clear()
                return@settingsReload
            }
            if (!active && !World.timers.contains("evil_tree_spawn")) {
                schedule(STARTUP_MINUTES)
            }
        }

        worldTimerStart("evil_tree_spawn") { respawnTicks }

        worldTimerTick("evil_tree_spawn") {
            spawn()
            Timer.CANCEL
        }

        worldTimerStart("evil_tree") { TICK_INTERVAL }

        worldTimerTick("evil_tree") { tick() }

        objectOperate("Nurture", "evil_tree_seedling,evil_tree_sapling*,evil_tree_young*") { (target) ->
            startNurture(target)
        }

        objectOperate("Inspect", "evil_tree_*") {
            inspect()
        }

        objectOperate("Chop", "evil_tree_*") { (target) ->
            chop(target)
        }

        objectOperate("Chop", "evil_branches_*") { (target) ->
            chopRoot(target)
        }

        objectOperate("Light fire", "evil_tree_*") { (target) ->
            light(target)
        }

        objectDespawn("evil_branches_*_spawn") {
            settle(this)
        }

        objectDespawn("evil_tree_fire") {
            fires.values.remove(this)
        }

        playerSpawn {
            if (this["evil_tree_buff", 0] > 0) {
                timers.restart("evil_tree_buff")
            }
        }

        timerStart("evil_tree_buff") { 1 }

        timerTick("evil_tree_buff") {
            if (dec("evil_tree_buff") <= 0) Timer.CANCEL else Timer.CONTINUE
        }

        timerStop("evil_tree_buff") {
            message("${Colours.DARK_RED.toTag()}Your evil tree magic has worn off.")
        }

        adminCommand("eviltree", stringArg("minutes"), desc = "Start a new evil tree event in [minutes]", handler = ::command)

        adminCommand(
            "evil_tree_magic",
            intArg("minutes", "how long it should last, 0 to remove it (default $MAX_BUFF_MINUTES)", optional = true),
            desc = "Give yourself the evil tree woodcutting reward buff",
            handler = ::magicCommand,
        )
    }

    /**
     * Grants the reward buff without having to fell a tree for it, for testing the woodcutting
     * effects it has - banked logs, fewer felled trees and more birds nests.
     */
    private fun magicCommand(player: Player, args: List<String>) {
        val minutes = args.getOrNull(0)?.toIntOrNull() ?: MAX_BUFF_MINUTES
        if (minutes <= 0) {
            player["evil_tree_buff"] = 0
            player.timers.stop("evil_tree_buff")
            return
        }
        player["evil_tree_buff"] = TimeUnit.MINUTES.toTicks(minutes)
        player.timers.restart("evil_tree_buff")
        player.message("Evil tree magic granted for $minutes ${"minute".plural(minutes)}.")
    }

    private fun command(player: Player, args: List<String>) {
        clear()
        schedule(args.getOrNull(0)?.toIntOrNull() ?: 0)
        player.message("Evil tree event scheduled.")
    }

    private fun schedule(minutes: Int) {
        respawnTicks = TimeUnit.MINUTES.toTicks(minutes)
        World.timers.clear("evil_tree_spawn")
        World.timers.start("evil_tree_spawn")
    }

    private fun spawn() {
        clear()
        val row = Tables.get("evil_tree_place").rows().random(random)
        place = row.rowId
        spawnTile = row.tileList("tiles").random(random)
        type = Tables.get("evil_tree_type").rows().random(random).rowId
        spawnId++
        tree = GameObjects.add("evil_tree_seedling", centre)
        displace()
        leprechaun = NPCs.add("leprechaun_evil_tree", spawnTile.add(-1, -1))
        World.timers.clear("evil_tree")
        World.timers.start("evil_tree")
        announce(row)
        logger.info { "Evil tree event has started at: $place (${spawnTile.x}, ${spawnTile.y}) type $type." }
    }

    private fun announce(place: RowDefinition) {
        if (!Settings["world.messages", false]) {
            return
        }
        val hint = place.string("hint").replaceFirstChar { it.lowercase() }.replace("<br>", " ")
        for (player in Players) {
            player.message("${Colours.DARK_RED.toTag()}An evil tree has begun to sprout $hint.")
        }
    }

    fun clear() {
        if (!active) {
            return
        }
        World.timers.clear("evil_tree")
        clearRoots()
        clearFires()
        GameObjects.remove(tree)
        NPCs.remove(leprechaun)
        reset()
    }

    internal fun reset() {
        place = ""
        type = "normal"
        spawnTile = Tile.EMPTY
        tree = GameObject(0)
        leprechaun = NPC()
        health = 0
        maxHealth = 0
        growth = 0
        grownTick = NOT_GROWN
        strikes = 0
        dead = false
        growthDelay = 0
        rootDelay = 0
        deathDelay = 0
        roots.clear()
        fires.clear()
    }

    private fun clearRoots() {
        for (root in roots.values) {
            GameObjects.remove(root.obj)
        }
        roots.clear()
    }

    private fun clearFires() {
        for (fire in fires.values) {
            GameObjects.remove(fire)
        }
        fires.clear()
    }

    /*
     * World tick
     */

    private fun tick(): Int {
        if (!active) {
            return Timer.CANCEL
        }
        if (deathDelay > 0) {
            deathDelay -= TICK_INTERVAL
            if (deathDelay <= 0) {
                tree = tree.replace("evil_tree_${type}_stump", spawnTile)
                return Timer.CANCEL
            }
            return Timer.CONTINUE
        }
        if (!grown) {
            growthTick()
            return Timer.CONTINUE
        }
        if (fires.isNotEmpty()) {
            damage(fires.size)
            if (deathDelay > 0) {
                return Timer.CONTINUE
            }
        }
        rootTick()
        lightning()
        return Timer.CONTINUE
    }

    private fun growthTick() {
        notice()
        growthDelay += TICK_INTERVAL
        if (growthDelay < Settings["events.evilTree.growthTicks", 100]) {
            return
        }
        growthDelay = 0
        addGrowth()
    }

    /**
     * Roots briefly shoot out of the ground around a sprouting sapling, once per player per tree.
     */
    private fun notice() {
        Players.forEachInRadius(centre, NOTICE_RADIUS) { player ->
            if (player["evil_tree_noticed", 0] == spawnId) {
                return@forEachInRadius
            }
            player["evil_tree_noticed"] = spawnId
            player.gfx("evil_root")
            player.say("What was that?")
        }
    }

    private fun rootTick() {
        rootDelay += TICK_INTERVAL
        if (rootDelay < ROOT_RESPAWN_TICKS) {
            return
        }
        rootDelay = 0
        val row = Tables.get("evil_branches").rows().filterNot { roots.containsKey(it.rowId) }.randomOrNull(random) ?: return
        spawnRoot(row)
    }

    private fun lightning() {
        if (strikes >= LIGHTNING_STRIKES) {
            return
        }
        val minutes = (GameLoop.tick - grownTick) / TimeUnit.MINUTES.toTicks(1)
        if (minutes < (strikes + 1) * LIGHTNING_INTERVAL_MINUTES) {
            return
        }
        strikes++
        say("A bolt of lightning strikes the evil tree!")
        if (strikes >= LIGHTNING_STRIKES) {
            damage(health)
            return
        }
        val cap = maxHealth shr strikes
        if (health > cap) {
            damage(health - cap)
        }
    }

    private fun say(message: String) {
        leprechaun.say(message)
        Players.forEachInRadius(centre, 15) { player ->
            player.message("${Colours.DARK_RED.toTag()}$message")
        }
    }

    /*
     * Growth
     */

    private fun Player.startNurture(target: GameObject) {
        if (!isTree(target) || !sapling) {
            return
        }
        val level = Tables.intOrNull("evil_tree_type.$type.farming") ?: return
        if (!has(Skill.Farming, level, " to help this sapling grow")) {
            return
        }
        if (!evilTreeInteract()) {
            return
        }
        message("You begin tending to the sapling.", ChatType.Filter)
        nurture(target)
    }

    private fun Player.nurture(target: GameObject) {
        if (!isTree(target) || !sapling) {
            return
        }
        anim("nurture_sapling")
        weakQueue("nurture_evil_sapling", 3) {
            if (!isTree(target) || !sapling) {
                return@weakQueue
            }
            exp(Skill.Farming, nurtureExperience())
            addGrowth()
            nurture(tree)
        }
    }

    private fun nurtureExperience(): Double {
        val row = Rows.get("evil_tree_type.$type")
        return row.int("nurture_xp") / 10.0 / row.int("seed_health")
    }

    private fun addGrowth() {
        growth++
        if (growth < Tables.int("evil_tree_type.$type.seed_health")) {
            return
        }
        growth = 0
        grow()
    }

    private fun grow() {
        tree = when (tree.id) {
            "evil_tree_seedling" -> tree.replace("evil_tree_sapling", centre)
            "evil_tree_sapling" -> tree.replace("evil_tree_sapling_large", centre)
            "evil_tree_sapling_large" -> {
                displace()
                tree.replace("evil_tree_young", spawnTile)
            }
            "evil_tree_young" -> tree.replace("evil_tree_young_large", spawnTile)
            "evil_tree_young_large" -> return mature()
            else -> return
        }
        leprechaun.say("Whoa!")
    }

    private fun mature() {
        maxHealth = Tables.int("evil_tree_type.$type.health")
        health = maxHealth
        grownTick = GameLoop.tick.toLong()
        tree = tree.replace("evil_tree_${type}_full", spawnTile)
        leprechaun.transform("leprechaun_panic")
        leprechaun.say("It's alive!")
        for (row in Tables.get("evil_branches").rows()) {
            spawnRoot(row)
        }
    }

    /**
     * Moves anyone standing where the tree is about to grow out of the way.
     */
    private fun displace() {
        for (tile in spawnTile.toCuboid(3, 3)) {
            for (player in Players.at(tile)) {
                push(player, centre)
            }
        }
    }

    private fun push(player: Player, from: Tile) {
        val delta = player.tile.delta(from)
        val direction = if (delta.x == 0 && delta.y == 0) Direction.cardinal.random(random) else delta.toDirection()
        if (direction == Direction.NONE) {
            return
        }
        player.walkTo(player.tile.add(direction.delta))
    }

    /*
     * Roots
     */

    private fun spawnRoot(row: RowDefinition) {
        if (roots.containsKey(row.rowId)) {
            return
        }
        val tile = spawnTile.add(row.int("deltaX"), row.int("deltaY"))
        GameObjects.add(row.obj("spawn"), tile, rotation = row.int("dir"), ticks = ROOT_BURST_TICKS)
        burst(tile)
    }

    /**
     * The burst animation object has expired, replace it with the root players can chop.
     */
    private fun settle(spawn: GameObject) {
        if (!alive) {
            return
        }
        val row = Tables.get("evil_branches").rows().firstOrNull { it.obj("spawn") == spawn.id } ?: return
        if (roots.containsKey(row.rowId)) {
            return
        }
        val root = GameObjects.add(spawn.id.removeSuffix("_spawn"), spawn.tile, rotation = spawn.rotation)
        roots[row.rowId] = Root(root, random.nextInt(ROOT_LIFE.first, ROOT_LIFE.last + 1))
    }

    private fun burst(tile: Tile) {
        Players.forEachInRadius(tile, 1) { player ->
            player.message("You dive out of the way as a new root bursts from the ground.")
            push(player, tile)
            player.anim("step_back_startled")
            player["delay"] = STUN_TICKS
            player.start("stunned", STUN_TICKS)
            player.start("movement_delay", STUN_TICKS)
        }
    }

    private fun killRoot(row: String) {
        val root = roots.remove(row) ?: return
        GameObjects.remove(root.obj)
    }

    /*
     * Chopping
     */

    private suspend fun Player.chop(target: GameObject) {
        if (!isTree(target) || !alive) {
            return
        }
        val hatchet = hatchet() ?: return
        val row = Rows.get("evil_tree_type.$type")
        if (!has(Skill.Woodcutting, row.int("woodcutting"), message = true) || !evilTreeInteract()) {
            return
        }
        val rates = Rows.get("logs.${row.itemList("reward_logs").first()}")
        while (awaitDialogues()) {
            if (!alive || deathDelay > 0) {
                break
            }
            if (!Hatchet.hasRequirements(this, hatchet, message = true)) {
                break
            }
            if (!swing(hatchet.id)) {
                continue
            }
            if (!Woodcutting.success(levels.get(Skill.Woodcutting), hatchet, rates)) {
                continue
            }
            exp(Skill.Woodcutting, row.int("tree_xp") / 10.0)
            damage(1)
        }
        clearAnim()
    }

    private suspend fun Player.chopRoot(target: GameObject) {
        val side = roots.entries.firstOrNull { it.value.obj == target }?.key ?: return
        val hatchet = hatchet() ?: return
        val row = Rows.get("evil_tree_type.$type")
        if (!has(Skill.Woodcutting, row.int("woodcutting"), message = true) || !evilTreeInteract()) {
            return
        }
        val rates = Rows.get("logs.${row.itemList("reward_logs").first()}")
        while (awaitDialogues()) {
            val root = roots[side]
            if (root == null || root.obj != target) {
                break
            }
            if (!Hatchet.hasRequirements(this, hatchet, message = true)) {
                break
            }
            if (inventory.isFull()) {
                message("Your inventory is too full to hold any more kindling.")
                break
            }
            if (!swing(hatchet.id)) {
                continue
            }
            if (!Woodcutting.success(levels.get(Skill.Woodcutting), hatchet, rates)) {
                continue
            }
            exp(Skill.Woodcutting, row.int("root_xp") / 10.0)
            inventory.add("evil_tree_kindling")
            if (--root.life <= 0) {
                killRoot(side)
                break
            }
        }
        clearAnim()
    }

    private fun Player.hatchet() = Hatchet.best(this) ?: run {
        message("You need a hatchet to chop down this tree.")
        message("You do not have a hatchet which you have the woodcutting level to use.")
        null
    }

    /**
     * Waits out the shared action delay, returns whether a chop actually landed this cycle.
     */
    private suspend fun Player.swing(hatchet: String): Boolean {
        val remaining = remaining("action_delay")
        if (remaining > 0) {
            pause(remaining)
            return false
        }
        anim("${hatchet}_chop")
        start("action_delay", CHOP_TICKS)
        pause(CHOP_TICKS)
        return true
    }

    /*
     * Fires
     */

    private suspend fun Player.light(target: GameObject) {
        if (!isTree(target) || !alive) {
            return
        }
        val row = Rows.get("evil_tree_type.$type")
        if (!has(Skill.Firemaking, row.int("firemaking"), message = " to set fire to this evil tree") || !evilTreeInteract()) {
            return
        }
        var first = true
        while (awaitDialogues()) {
            if (!alive || deathDelay > 0) {
                break
            }
            if (!inventory.contains("tinderbox")) {
                message("You need a tinderbox in order to light a fire.")
                break
            }
            if (!inventory.contains("evil_tree_kindling")) {
                message("You don't have any kindling to burn.")
                break
            }
            val spot = freeFire() ?: run {
                message("There's nowhere left to light a fire.")
                null
            } ?: break
            val remaining = remaining("action_delay")
            if (remaining > 0) {
                pause(remaining)
                continue
            }
            if (first) {
                message("You crouch to light the kindling.")
                first = false
            }
            anim("light_fire")
            start("action_delay", LIGHT_TICKS)
            pause(LIGHT_TICKS)
            if (!alive || deathDelay > 0 || fires.containsKey(spot.rowId)) {
                continue
            }
            if (!inventory.remove("evil_tree_kindling")) {
                break
            }
            exp(Skill.Firemaking, row.int("burn_xp") / 10.0)
            val tile = spawnTile.add(spot.int("deltaX"), spot.int("deltaY"))
            fires[spot.rowId] = GameObjects.add("evil_tree_fire", tile, rotation = spot.int("dir"), ticks = row.int("fire_life"))
        }
        clearAnim()
    }

    private fun freeFire(): RowDefinition? = Tables.get("evil_fires").rows().filterNot { fires.containsKey(it.rowId) }.randomOrNull(random)

    /*
     * Damage and death
     */

    private fun damage(amount: Int) {
        if (!alive || deathDelay > 0) {
            return
        }
        health = (health - amount).coerceAtLeast(0)
        if (health <= 0) {
            kill()
            return
        }
        val id = when {
            health * 3 > maxHealth * 2 -> "evil_tree_${type}_full"
            health * 3 > maxHealth -> "evil_tree_${type}_half"
            else -> "evil_tree_${type}_weak"
        }
        if (tree.id == id) {
            return
        }
        tree = tree.replace(id, spawnTile)
    }

    private fun kill() {
        dead = true
        clearRoots()
        clearFires()
        leprechaun.transform("leprechaun_evil_tree")
        tree = tree.replace("evil_tree_${type}_death", spawnTile)
        deathDelay = DEATH_TICKS
        val minutes = Settings["events.evilTree.minRespawnTimeMinutes", 120]..Settings["events.evilTree.maxRespawnTimeMinutes", 120]
        schedule(minutes.random(random))
    }

    /*
     * Inspect
     */

    private suspend fun Player.inspect() {
        val level = Tables.intOrNull("evil_tree_type.$type.woodcutting") ?: return
        val name = if (type == "normal") "Evil tree" else "Evil ${type.toLowerSpaceCase()} tree"
        if (!alive) {
            statement("This is going to be an $name. A Woodcutting and Firemaking level of at least $level is required to interact with it and the surrounding roots.")
            return
        }
        val percent = (health * 100) / maxHealth.coerceAtLeast(1)
        statement("This is an $name. A Woodcutting and Firemaking level of at least $level is required to interact with this tree and the surrounding roots.<br>There is $percent% of this tree left.")
    }

    internal class Root(var obj: GameObject, var life: Int)

    companion object {
        var place: String = ""
        var type: String = "normal"
        var spawnTile: Tile = Tile.EMPTY
        var spawnId: Int = 0
        var grownTick: Long = NOT_GROWN
        var dead: Boolean = false
            private set

        val active: Boolean
            get() = spawnTile != Tile.EMPTY

        val grown: Boolean
            get() = grownTick != NOT_GROWN

        /**
         * A fully grown tree that hasn't been cut down yet.
         */
        val alive: Boolean
            get() = grown && !dead

        val sapling: Boolean
            get() = active && !grown && !dead

        /**
         * Centre of the three by three tree, where the one by one sapling stages sit.
         */
        val centre: Tile
            get() = spawnTile.add(1, 1)

        /**
         * Whether [obj] is the current evil tree, at any stage of its growth.
         */
        private fun isTree(obj: GameObject): Boolean = active && (obj.tile == spawnTile || obj.tile == centre)

        const val NOT_GROWN = -1L
        const val TICK_INTERVAL = 10
        const val STARTUP_MINUTES = 2
        const val ROOT_BURST_TICKS = 2
        const val ROOT_RESPAWN_TICKS = 100
        const val STUN_TICKS = 2
        const val CHOP_TICKS = 3
        const val LIGHT_TICKS = 4
        const val DEATH_TICKS = 20
        const val LIGHTNING_STRIKES = 3
        const val LIGHTNING_INTERVAL_MINUTES = 10
        const val MAX_BUFF_MINUTES = 30
        const val NOTICE_RADIUS = 8
        val ROOT_LIFE = 3..7
    }
}

/**
 * Whether the player still has "evil tree magic" left over from an evil tree reward.
 */
val Player.evilTreeMagic: Boolean
    get() = this["evil_tree_buff", 0] > 0

/**
 * Rolls the players daily evil tree counters over when the day changes.
 */
fun Player.evilTreeDailyReset() {
    val day = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
    if (this["evil_tree_day", -1L] == day) {
        return
    }
    this["evil_tree_day"] = day
    this["evil_tree_trees"] = 0
    this["evil_tree_kindling_handed"] = 0
    this["evil_tree_spawn_id"] = 0
}

/**
 * Whether the player is allowed to interact with the current evil tree, counting it
 * towards their daily limit the first time they do.
 */
fun Player.evilTreeInteract(): Boolean {
    evilTreeDailyReset()
    if (this["evil_tree_spawn_id", 0] == EvilTree.spawnId) {
        return true
    }
    if (this["evil_tree_trees", 0] >= Settings["events.evilTree.dailyTreeLimit", 2]) {
        message("You've already helped with as many evil trees as you can today.")
        return false
    }
    this["evil_tree_spawn_id"] = EvilTree.spawnId
    inc("evil_tree_trees")
    this["evil_tree_rewards"] = true
    return true
}
