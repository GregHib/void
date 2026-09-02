package content.activity.evil_tree

import com.github.michaelbull.logging.InlineLogger
import content.entity.effect.transform
import content.entity.player.dialogue.type.statement
import content.skill.woodcutting.Hatchet
import content.skill.woodcutting.chopSuccess
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.World
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
            if (!EvilTreeState.active && !World.timers.contains("evil_tree_spawn")) {
                schedule(STARTUP_MINUTES)
            }
        }

        worldTimerStart("evil_tree_spawn") { EvilTreeState.respawnTicks }

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
            EvilTreeState.fires.values.remove(this)
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
    }

    private fun command(player: Player, args: List<String>) {
        clear()
        schedule(args.getOrNull(0)?.toIntOrNull() ?: 0)
        player.message("Evil tree event scheduled.")
    }

    private fun schedule(minutes: Int) {
        EvilTreeState.respawnTicks = TimeUnit.MINUTES.toTicks(minutes)
        World.timers.clear("evil_tree_spawn")
        World.timers.start("evil_tree_spawn")
    }

    private fun spawn() {
        clear()
        val state = EvilTreeState
        val place = Tables.get("evil_tree_place").rows().random(random)
        state.place = place.rowId
        state.spawnTile = place.tileList("tiles").random(random)
        state.type = Tables.get("evil_tree_type").rows().random(random).rowId
        state.spawnId++
        state.tree = GameObjects.add("evil_tree_seedling", state.centre)
        displace()
        state.leprechaun = NPCs.add("leprechaun_evil_tree", state.spawnTile.add(-1, -1))
        World.timers.clear("evil_tree")
        World.timers.start("evil_tree")
        announce(place)
        logger.info { "Evil tree event has started at: ${state.place} (${state.spawnTile.x}, ${state.spawnTile.y}) type ${state.type}." }
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
        val state = EvilTreeState
        if (!state.active) {
            return
        }
        World.timers.clear("evil_tree")
        clearRoots()
        clearFires()
        GameObjects.remove(state.tree)
        NPCs.remove(state.leprechaun)
        state.reset()
    }

    private fun clearRoots() {
        for (root in EvilTreeState.roots.values) {
            GameObjects.remove(root.obj)
        }
        EvilTreeState.roots.clear()
    }

    private fun clearFires() {
        for (fire in EvilTreeState.fires.values) {
            GameObjects.remove(fire)
        }
        EvilTreeState.fires.clear()
    }

    /*
     * World tick
     */

    private fun tick(): Int {
        val state = EvilTreeState
        if (!state.active) {
            return Timer.CANCEL
        }
        if (state.deathDelay > 0) {
            state.deathDelay -= TICK_INTERVAL
            if (state.deathDelay <= 0) {
                state.tree = state.tree.replace("evil_tree_${state.type}_stump", state.spawnTile)
                return Timer.CANCEL
            }
            return Timer.CONTINUE
        }
        if (!state.grown) {
            growthTick()
            return Timer.CONTINUE
        }
        if (state.fires.isNotEmpty()) {
            damage(state.fires.size)
            if (state.deathDelay > 0) {
                return Timer.CONTINUE
            }
        }
        rootTick()
        lightning()
        return Timer.CONTINUE
    }

    private fun growthTick() {
        val state = EvilTreeState
        notice()
        state.growthDelay += TICK_INTERVAL
        if (state.growthDelay < Settings["events.evilTree.growthTicks", 100]) {
            return
        }
        state.growthDelay = 0
        addGrowth()
    }

    /**
     * Roots briefly shoot out of the ground around a sprouting sapling, once per player per tree.
     */
    private fun notice() {
        val state = EvilTreeState
        Players.forEachInRadius(state.centre, NOTICE_RADIUS) { player ->
            if (player["evil_tree_noticed", 0] == state.spawnId) {
                return@forEachInRadius
            }
            player["evil_tree_noticed"] = state.spawnId
            player.gfx("evil_root")
            player.say("What was that?")
        }
    }

    private fun rootTick() {
        val state = EvilTreeState
        state.rootDelay += TICK_INTERVAL
        if (state.rootDelay < ROOT_RESPAWN_TICKS) {
            return
        }
        state.rootDelay = 0
        val row = Tables.get("evil_branches").rows().filterNot { state.roots.containsKey(it.rowId) }.randomOrNull(random) ?: return
        spawnRoot(row)
    }

    private fun lightning() {
        val state = EvilTreeState
        if (state.strikes >= LIGHTNING_STRIKES) {
            return
        }
        val minutes = (GameLoop.tick - state.grownTick) / TimeUnit.MINUTES.toTicks(1)
        if (minutes < (state.strikes + 1) * LIGHTNING_INTERVAL_MINUTES) {
            return
        }
        state.strikes++
        say("A bolt of lightning strikes the evil tree!")
        if (state.strikes >= LIGHTNING_STRIKES) {
            damage(state.health)
            return
        }
        val cap = state.maxHealth shr state.strikes
        if (state.health > cap) {
            damage(state.health - cap)
        }
    }

    private fun say(message: String) {
        val state = EvilTreeState
        state.leprechaun.say(message)
        Players.forEachInRadius(state.centre, 15) { player ->
            player.message("${Colours.DARK_RED.toTag()}$message")
        }
    }

    /*
     * Growth
     */

    private fun Player.startNurture(target: GameObject) {
        val state = EvilTreeState
        if (!state.isTree(target) || !state.sapling) {
            return
        }
        val level = Tables.intOrNull("evil_tree_type.${state.type}.farming") ?: return
        if (!has(Skill.Farming, level, " to help this sapling grow")) {
            return
        }
        if (!interact()) {
            return
        }
        message("You begin tending to the sapling.", ChatType.Filter)
        nurture(target)
    }

    private fun Player.nurture(target: GameObject) {
        val state = EvilTreeState
        if (!state.isTree(target) || !state.sapling) {
            return
        }
        anim("nurture_sapling")
        weakQueue("nurture_evil_sapling", 3) {
            if (!state.isTree(target) || !state.sapling) {
                return@weakQueue
            }
            exp(Skill.Farming, nurtureExperience())
            addGrowth()
            nurture(state.tree)
        }
    }

    private fun nurtureExperience(): Double {
        val state = EvilTreeState
        val row = Rows.get("evil_tree_type.${state.type}")
        return row.int("nurture_xp") / 10.0 / row.int("seed_health")
    }

    private fun addGrowth() {
        val state = EvilTreeState
        state.growth++
        if (state.growth < Tables.int("evil_tree_type.${state.type}.seed_health")) {
            return
        }
        state.growth = 0
        grow()
    }

    private fun grow() {
        val state = EvilTreeState
        state.tree = when (state.tree.id) {
            "evil_tree_seedling" -> state.tree.replace("evil_tree_sapling", state.centre)
            "evil_tree_sapling" -> state.tree.replace("evil_tree_sapling_large", state.centre)
            "evil_tree_sapling_large" -> {
                displace()
                state.tree.replace("evil_tree_young", state.spawnTile)
            }
            "evil_tree_young" -> state.tree.replace("evil_tree_young_large", state.spawnTile)
            "evil_tree_young_large" -> return mature()
            else -> return
        }
        state.leprechaun.say("Whoa!")
    }

    private fun mature() {
        val state = EvilTreeState
        state.maxHealth = Tables.int("evil_tree_type.${state.type}.health")
        state.health = state.maxHealth
        state.grownTick = GameLoop.tick.toLong()
        state.tree = state.tree.replace("evil_tree_${state.type}_full", state.spawnTile)
        state.leprechaun.transform("leprechaun_panic")
        state.leprechaun.say("It's alive!")
        for (row in Tables.get("evil_branches").rows()) {
            spawnRoot(row)
        }
    }

    /**
     * Moves anyone standing where the tree is about to grow out of the way.
     */
    private fun displace() {
        val state = EvilTreeState
        for (tile in state.spawnTile.toCuboid(3, 3)) {
            for (player in Players.at(tile)) {
                push(player, state.centre)
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
        val state = EvilTreeState
        if (state.roots.containsKey(row.rowId)) {
            return
        }
        val tile = state.spawnTile.add(row.int("deltaX"), row.int("deltaY"))
        GameObjects.add(row.obj("spawn"), tile, rotation = row.int("dir"), ticks = ROOT_BURST_TICKS)
        burst(tile)
    }

    /**
     * The burst animation object has expired, replace it with the root players can chop.
     */
    private fun settle(spawn: GameObject) {
        val state = EvilTreeState
        if (!state.alive) {
            return
        }
        val row = Tables.get("evil_branches").rows().firstOrNull { it.obj("spawn") == spawn.id } ?: return
        if (state.roots.containsKey(row.rowId)) {
            return
        }
        val root = GameObjects.add(spawn.id.removeSuffix("_spawn"), spawn.tile, rotation = spawn.rotation)
        state.roots[row.rowId] = EvilTreeState.Root(root, random.nextInt(ROOT_LIFE.first, ROOT_LIFE.last + 1))
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
        val root = EvilTreeState.roots.remove(row) ?: return
        GameObjects.remove(root.obj)
    }

    /*
     * Chopping
     */

    private suspend fun Player.chop(target: GameObject) {
        val state = EvilTreeState
        if (!state.isTree(target) || !state.alive) {
            return
        }
        val hatchet = hatchet() ?: return
        val row = Rows.get("evil_tree_type.${state.type}")
        if (!has(Skill.Woodcutting, row.int("woodcutting"), message = true) || !interact()) {
            return
        }
        val rates = Rows.get("logs.${row.itemList("reward_logs").first()}")
        while (awaitDialogues()) {
            if (!state.alive || state.deathDelay > 0) {
                break
            }
            if (!Hatchet.hasRequirements(this, hatchet, message = true)) {
                break
            }
            if (!swing(hatchet.id)) {
                continue
            }
            if (!chopSuccess(levels.get(Skill.Woodcutting), hatchet, rates)) {
                continue
            }
            exp(Skill.Woodcutting, row.int("tree_xp") / 10.0)
            damage(1)
        }
        clearAnim()
    }

    private suspend fun Player.chopRoot(target: GameObject) {
        val state = EvilTreeState
        val side = state.roots.entries.firstOrNull { it.value.obj == target }?.key ?: return
        val hatchet = hatchet() ?: return
        val row = Rows.get("evil_tree_type.${state.type}")
        if (!has(Skill.Woodcutting, row.int("woodcutting"), message = true) || !interact()) {
            return
        }
        val rates = Rows.get("logs.${row.itemList("reward_logs").first()}")
        while (awaitDialogues()) {
            val root = state.roots[side]
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
            if (!chopSuccess(levels.get(Skill.Woodcutting), hatchet, rates)) {
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
        val state = EvilTreeState
        if (!state.isTree(target) || !state.alive) {
            return
        }
        val row = Rows.get("evil_tree_type.${state.type}")
        if (!has(Skill.Firemaking, row.int("firemaking"), message = " to set fire to this evil tree") || !interact()) {
            return
        }
        var first = true
        while (awaitDialogues()) {
            if (!state.alive || state.deathDelay > 0) {
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
            if (!state.alive || state.deathDelay > 0 || state.fires.containsKey(spot.rowId)) {
                continue
            }
            if (!inventory.remove("evil_tree_kindling")) {
                break
            }
            exp(Skill.Firemaking, row.int("burn_xp") / 10.0)
            val tile = state.spawnTile.add(spot.int("deltaX"), spot.int("deltaY"))
            state.fires[spot.rowId] = GameObjects.add("evil_tree_fire", tile, rotation = spot.int("dir"), ticks = row.int("fire_life"))
        }
        clearAnim()
    }

    private fun freeFire(): RowDefinition? = Tables.get("evil_fires").rows().filterNot { EvilTreeState.fires.containsKey(it.rowId) }.randomOrNull(random)

    /*
     * Damage and death
     */

    private fun damage(amount: Int) {
        val state = EvilTreeState
        if (!state.alive || state.deathDelay > 0) {
            return
        }
        state.health = (state.health - amount).coerceAtLeast(0)
        if (state.health <= 0) {
            kill()
            return
        }
        val id = when {
            state.health * 3 > state.maxHealth * 2 -> "evil_tree_${state.type}_full"
            state.health * 3 > state.maxHealth -> "evil_tree_${state.type}_half"
            else -> "evil_tree_${state.type}_weak"
        }
        if (state.tree.id == id) {
            return
        }
        state.tree = state.tree.replace(id, state.spawnTile)
    }

    private fun kill() {
        val state = EvilTreeState
        state.dead = true
        clearRoots()
        clearFires()
        state.leprechaun.transform("leprechaun_evil_tree")
        state.tree = state.tree.replace("evil_tree_${state.type}_death", state.spawnTile)
        state.deathDelay = DEATH_TICKS
        val minutes = Settings["events.evilTree.minRespawnTimeMinutes", 120]..Settings["events.evilTree.maxRespawnTimeMinutes", 120]
        schedule(minutes.random(random))
    }

    /*
     * Inspect
     */

    private suspend fun Player.inspect() {
        val state = EvilTreeState
        val level = Tables.intOrNull("evil_tree_type.${state.type}.woodcutting") ?: return
        val name = if (state.type == "normal") "Evil tree" else "Evil ${state.type.toLowerSpaceCase()} tree"
        if (!state.alive) {
            statement("This is going to be an $name. A Woodcutting and Firemaking level of at least $level is required to interact with it and the surrounding roots.")
            return
        }
        val percent = (state.health * 100) / state.maxHealth.coerceAtLeast(1)
        statement("This is an $name. A Woodcutting and Firemaking level of at least $level is required to interact with this tree and the surrounding roots.<br>There is $percent% of this tree left.")
    }

    companion object {
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
        const val NOTICE_RADIUS = 8
        val ROOT_LIFE = 3..7
    }
}
