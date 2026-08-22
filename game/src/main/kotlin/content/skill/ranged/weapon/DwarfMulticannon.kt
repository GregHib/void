package content.skill.ranged.weapon

import content.area.wilderness.inMultiCombat
import content.entity.combat.Target
import content.entity.combat.attacker
import content.entity.combat.dead
import content.entity.combat.hit.Hit
import content.entity.combat.hit.hit
import content.entity.player.bank.bank
import content.entity.proj.shoot
import content.quest.questCompleted
import org.rsmod.game.pathfinder.LineValidator
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.Transaction
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import kotlin.math.abs
import kotlin.math.atan2

/**
 * The four pieces a dwarf multicannon is carried and assembled in, in build order.
 */
internal val CANNON_PARTS = listOf("cannon_base", "cannon_stand", "cannon_barrels", "cannon_furnace")

/**
 * Hands a dismantled cannon back - the first [parts] pieces plus any loaded cannonballs. It all
 * goes to the inventory in one transaction, or all to the bank when it won't fit, rather than
 * splitting a cannon across the two.
 */
internal fun Player.returnCannon(parts: Int, balls: Int) {
    if (inventory.transaction { addCannon(parts, balls) }) {
        return
    }
    bank.transaction { addCannon(parts, balls) }
    message("Your cannon was sent to your bank.")
}

private fun Transaction.addCannon(parts: Int, balls: Int) {
    for (part in CANNON_PARTS.take(parts)) {
        add(part)
    }
    if (balls > 0) {
        add("cannonball", balls)
    }
}

/**
 * The dwarf multicannon assembles from four parts, then rotates one octant per tick, firing a
 * single cannonball into the wedge it faces - eight balls per 4.8 second revolution. Neighbouring
 * wedges overlap, so a target sitting in an overlap is fired on twice in one revolution.
 */
class DwarfMulticannon(private val lineValidator: LineValidator) : Script {

    init {
        itemOption("Set-up", "cannon_base") { setUpCannon() }

        objectOperate("Fire", "dwarf_multicannon") { (target) ->
            loadCannon(target)
        }

        itemOnObjectOperate("cannonball", "dwarf_multicannon") { (target) ->
            loadCannon(target)
        }

        objectOperate("Pick-up", "dwarf_multicannon,dwarf_multicannon_base,dwarf_multicannon_stand,dwarf_multicannon_barrels") { (target) ->
            pickUpCannon(target)
        }

        timerStart("cannon") { 1 }

        timerTick("cannon") { tickCannon() }

        timerStop("cannon") { logout -> dismantleCannon(logout) }
    }

    private suspend fun Player.setUpCannon() {
        if (!questCompleted("dwarf_cannon")) {
            message("You have no idea how to operate this machine.")
            return
        }
        if (contains("cannon_tile")) {
            message("You can only have one cannon set up at a time.")
            return
        }
        val banned = bannedMessage(tile)
        if (banned != null) {
            message(banned)
            return
        }
        if (!CANNON_PARTS.all { inventory.contains(it) }) {
            message("You don't have all of the cannon parts.")
            return
        }
        // Two west and three south leaves the 3x3 footprint directly south-west of the player,
        // so the player is never standing inside the cannon once it is built.
        val corner = tile.add(-2, -3)
        if (!vacant(corner)) {
            message("There isn't enough space to set up your cannon here.")
            return
        }

        face(corner.add(1, 1))
        anim("cannon_setup")
        inventory.remove(CANNON_PARTS[0])
        var obj = GameObjects.add(STAGES[0], corner, ObjectShape.CENTRE_PIECE_STRAIGHT)
        message(STAGE_MESSAGES[0])
        // Claim the cannon before the remaining stages so an interrupted assembly still hands the
        // parts back rather than stranding a half-built cannon in the world.
        set("cannon_tile", corner.id)
        set("cannon_balls", 0)
        set("cannon_direction", OCTANTS.lastIndex)
        set("cannon_age", 0)
        softTimers.start("cannon")

        for (stage in 1 until STAGES.size) {
            delay(2)
            anim("cannon_setup")
            inventory.remove(CANNON_PARTS[stage])
            obj = obj.replace(STAGES[stage])
            message(STAGE_MESSAGES[stage])
        }
    }

    private fun Player.loadCannon(target: GameObject) {
        if (!owns(target)) {
            message("This is not your cannon.")
            return
        }
        val max = Settings["world.objs.cannon.maxAmmo", 30]
        val loaded = get("cannon_balls", 0)
        if (loaded >= max) {
            message("Your cannon is full.")
            return
        }
        val held = inventory.count("cannonball")
        if (held <= 0) {
            message("You need to load your cannon with cannon balls before firing it!")
            return
        }
        val amount = minOf(held, max - loaded)
        if (!inventory.remove("cannonball", amount)) {
            return
        }
        set("cannon_balls", loaded + amount)
        message("You load the cannon with $amount cannon ${"ball".plural(amount)}.")
    }

    private suspend fun Player.pickUpCannon(target: GameObject) {
        if (!owns(target)) {
            message("This is not your cannon.")
            return
        }
        val balls = get("cannon_balls", 0)
        val needed = CANNON_PARTS.size + if (balls > 0 && !inventory.contains("cannonball")) 1 else 0
        if (inventory.spaces < needed) {
            message("You need at least $needed inventory ${"space".plural(needed)} to pick up your cannon.")
            return
        }
        anim("cannon_setup")
        delay(1)
        message("You pick up the cannon. It's really heavy.")
        softTimers.stop("cannon")
    }

    private fun Player.tickCannon(): Int {
        val corner = Tile(get("cannon_tile", -1))
        val obj = GameObjects.findOrNull(corner) { STAGES.contains(it.id) } ?: return Timer.CANCEL

        val age = inc("cannon_age")
        if (age == Settings["world.objs.cannon.warningTicks", 2000]) {
            message("Your cannon is starting to decay.")
        }
        if (age >= Settings["world.objs.cannon.decayTicks", 2500]) {
            message("Your cannon has decayed and the parts have been returned to you.")
            return Timer.CANCEL
        }
        if (obj.id != STAGES.last() || get("cannon_balls", 0) <= 0) {
            return Timer.CONTINUE
        }

        val direction = (get("cannon_direction", OCTANTS.lastIndex) + 1) % OCTANTS.size
        set("cannon_direction", direction)
        obj.anim(ROTATION_ANIMS[direction])

        val centre = corner.add(1, 1)
        val facing = OCTANTS[direction]
        // Cannonballs leave the end of the barrel...
        val barrel = centre.add(facing.delta)
        // ...but line of sight is traced from a tile clear of the cannon's own 3x3 footprint,
        // otherwise the cannon blocks its own shots.
        val sight = centre.add(facing.delta.x * 2, facing.delta.y * 2)
        val target = findTarget(centre, sight, facing) ?: return Timer.CONTINUE
        fireAt(barrel, target)

        if (dec("cannon_balls") <= 0) {
            message("Your cannon is out of ammo!")
        }
        return Timer.CONTINUE
    }

    private fun Player.fireAt(barrel: Tile, target: NPC) {
        val flight = barrel.shoot("cannonball", target)
        val accurate = Hit.success(this, target, "range", Item.EMPTY, special = false, defensiveType = "range")
        // A flat roll - the cannon's damage does not scale with the owner's Ranged level.
        val damage = if (accurate) random.nextInt(Settings["world.objs.cannon.maxHit", 300] + 1) else -1
        hit(
            target = target,
            weapon = Item.EMPTY,
            offensiveType = "cannon",
            defensiveType = "range",
            delay = if (flight == -1) 0 else flight,
            spell = "",
            special = false,
            damage = damage,
        )
        // Claim the target as ours. A bare hit() never emits combatStart, which is what normally
        // assigns this, so the target would be left flagged under attack by nobody - and
        // Target.attackable then rejects it as "someone else is fighting that" on every later
        // tick, stopping the cannon from ever firing at the same monster twice.
        if (target.attacker == null) {
            target.attacker = this
        }
    }

    private fun Player.findTarget(centre: Tile, sight: Tile, facing: Direction): NPC? {
        val range = Settings["world.objs.cannon.range", 10]
        // In single-way combat the cannon only fires at whoever the owner is already fighting.
        val restricted = if (inMultiCombat) null else attacker
        for (tile in Spiral.spiral(centre, range)) {
            for (npc in NPCs.at(tile)) {
                if (!validTarget(npc, centre, sight, facing, restricted)) {
                    continue
                }
                return npc
            }
        }
        return null
    }

    private fun Player.validTarget(npc: NPC, centre: Tile, sight: Tile, facing: Direction, restricted: Character?): Boolean {
        if (npc.tile.level != centre.level) {
            return false
        }
        if (npc.dead || npc["owner_index", -1] == index) {
            return false
        }
        if (npc.transformDef["immune_cannon", false]) {
            return false
        }
        if (restricted != null && npc != restricted) {
            return false
        }
        if (!inArc(npc.tile.delta(centre), facing)) {
            return false
        }
        if (!Target.attackable(this, npc, message = false)) {
            return false
        }
        return lineValidator.hasLineOfSight(
            level = sight.level,
            srcX = sight.x,
            srcZ = sight.y,
            srcSize = 1,
            destX = npc.tile.x,
            destZ = npc.tile.y,
            destWidth = npc.size,
            destHeight = npc.size,
        )
    }

    private fun Player.dismantleCannon(logout: Boolean) {
        val id: Int = remove("cannon_tile") ?: return
        val balls = remove<Int>("cannon_balls") ?: 0
        clear("cannon_direction")
        clear("cannon_age")

        val corner = Tile(id)
        val obj = GameObjects.findOrNull(corner) { STAGES.contains(it.id) }
        // Only hand back the parts that were actually built in - an assembly interrupted part way
        // through has the rest still sitting in the inventory.
        val built = if (obj == null) CANNON_PARTS.size else STAGES.indexOf(obj.id) + 1
        if (obj != null) {
            GameObjects.remove(obj)
        }

        if (logout) {
            // A cannon doesn't survive a world switch. Rather than posting the pieces to someone
            // who isn't there to read the message, note what they were owed - Nulodion replaces it
            // free of charge when they next speak to him.
            set("cannon_lost_parts", built)
            set("cannon_lost_balls", balls)
            return
        }

        returnCannon(built, balls)
    }

    private fun Player.owns(target: GameObject) = get("cannon_tile", -1) == target.tile.id

    private fun vacant(corner: Tile): Boolean {
        for (x in 0 until 3) {
            for (y in 0 until 3) {
                val tile = corner.add(x, y)
                if (Collisions.check(tile, BLOCKED)) {
                    return false
                }
                if (GameObjects.getLayer(tile, ObjectLayer.GROUND) != null) {
                    return false
                }
            }
        }
        return true
    }

    private fun bannedMessage(tile: Tile): String? {
        val area = Areas.get(tile.zone).firstOrNull { it.tags.contains("no_cannon") && tile in it.area } ?: return null
        return area.tags.firstNotNullOfOrNull { BANNED_MESSAGES[it] } ?: "It is not permitted to set up a cannon here."
    }

    /**
     * Whether [delta] falls inside the wedge the barrel currently covers. The wedge is wider than
     * the 45 degrees between two octants, so neighbouring wedges overlap and a target sitting in
     * the overlap is fired on twice per revolution, as in 2011.
     */
    private fun inArc(delta: Delta, facing: Direction): Boolean {
        if (delta.x == 0 && delta.y == 0) {
            return false
        }
        val bearing = Math.toDegrees(atan2(delta.y.toDouble(), delta.x.toDouble()))
        val barrel = Math.toDegrees(atan2(facing.delta.y.toDouble(), facing.delta.x.toDouble()))
        val difference = abs(((bearing - barrel + 540.0) % 360.0) - 180.0)
        return difference <= Settings["world.objs.cannon.arcDegrees", 30.0]
    }

    private companion object {
        private val OCTANTS = Direction.clockwise

        private val ROTATION_ANIMS = arrayOf(
            "cannon_turn_north",
            "cannon_turn_north_east",
            "cannon_turn_east",
            "cannon_turn_south_east",
            "cannon_turn_south",
            "cannon_turn_south_west",
            "cannon_turn_west",
            "cannon_turn_north_west",
        )

        private val STAGES = listOf(
            "dwarf_multicannon_base",
            "dwarf_multicannon_stand",
            "dwarf_multicannon_barrels",
            "dwarf_multicannon",
        )

        private val STAGE_MESSAGES = listOf(
            "You place the cannon base on the ground.",
            "You add the stand.",
            "You add the barrels.",
            "You add the furnace.",
        )

        private val BANNED_MESSAGES = mapOf(
            "no_cannon_black_guard" to "It is not permitted to set up a cannon this close to the Dwarf Black Guard.",
            "no_cannon_dank" to "The air is too dank for you to set up a cannon here.",
            "no_cannon_grand_exchange" to "The Grand Exchange staff prefer not to have heavy artillery operated around their premises.",
            "no_cannon_electric" to "The electricity bursting through this plane would render the cannon useless.",
            "no_cannon_temple" to "This temple is ancient and would probably collapse if you started firing a cannon.",
            "no_cannon_dark_forces" to "Dark forces are preventing the dwarven construction from working.",
            "no_cannon_damp" to "The ground is too damp.",
        )

        private val BLOCKED = CollisionFlag.FLOOR or
            CollisionFlag.FLOOR_DECORATION or
            CollisionFlag.OBJECT or
            CollisionFlag.BLOCK_PLAYERS or
            CollisionFlag.BLOCK_NPCS
    }
}
