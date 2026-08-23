package content.area.misthalin.ham_hideout

import content.entity.obj.TrapDoors.Companion.registerCloseHandler
import content.entity.obj.TrapDoors.Companion.registerOpenHandler
import content.entity.obj.door.Door.openDoor
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.network.login.protocol.encode.send
import world.gregs.voidps.network.login.protocol.encode.updateZone
import world.gregs.voidps.network.login.protocol.encode.zone.ObjectAddition
import world.gregs.voidps.network.login.protocol.encode.zone.ObjectRemoval
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import world.gregs.voidps.type.random
import kotlin.math.roundToInt

class HamHideout : Script {

    enum class BlowResult(val message: String, val countsTowardCaught: Boolean) {
        PartialAvoid("Your agility helps you partially avoid the blow.", false),
        Concussed("You feel slightly concussed from the blow.", true),
    }

    companion object {
        private const val AUTO_PICKLOCK_QUEUE = "ham_hideout_trapdoor_picklock_auto"
        private const val AUTO_PRISON_DOOR_PICKLOCK_QUEUE = "ham_hideout_prison_door_picklock_auto"
        private const val LOCKED_TRAPDOOR_OPEN_TICK = "ham_hideout_trapdoor_open_tick"
        private const val OPEN_TRAPDOOR_CLOSE_TICK = "ham_hideout_trapdoor_close_tick"
        private const val LOCKED_PRISON_DOOR_OPEN_TICK = "ham_hideout_prison_door_open_tick"

        // Queued actions start decrementing on the next tick, so 6 leaves 5 full ticks between attempts.
        private const val PICKLOCK_RETRY_DELAY = 6
        private const val PRISON_DOOR_PICKLOCK_ANIMATION_DELAY = 2
        private const val TRAPDOOR_FAIL_XP_CHANCE = 4
        private const val FUMBLE_MESSAGE_CHANCE = 5
        private const val FUMBLE_DEBUFF_CHANCE = 5
        private const val FUMBLE_DEBUFF_LEVELS = 1
        private const val OPEN_DURATION = 300
        private const val LOCAL_TRAPDOOR_KEY = 0
        private const val LOCAL_TRAPDOOR_RESET_QUEUE = "ham_hideout_trapdoor_reset"
        private const val CLOSED_TRAPDOOR_ID = 5490
        private const val OPEN_TRAPDOOR_ID = 5491
        private const val CLOSED_PRISON_DOOR_ID = 5501
        private const val PRISON_DOOR_ATTEMPTS = "ham_hideout_prison_door_attempts"
        private const val CONCUSSION_COUNTER_VAR = "ham_hideout_concussion_counter"
        private const val HAM_HIDEOUT_REGION_ID = 12694
        private const val PICKPOCKET_KNOCKOUT_COUNT = 3
        private const val CHATBOX_MEMBER_NAME = "H.A.M. Member"
        private const val HAM_ROBE_PROTECTION_PER_PIECE = 4
        private const val HAM_ROBE_PROTECTION_ROLL_MAX = 100
        private val TRAPDOOR_TILE = Tile(3165, 3252)
        private val PRISON_DOOR_TILE = Tile(3183, 9611)
        private val PRISON_DOOR_INTERACT_TILE = Tile(3183, 9611)
        private val PRISON_DOOR_BLOCKED_TILES = setOf(Tile(3183, 9610), Tile(3183, 9612))
        private val PRISON_DOOR_EXIT_TILE = Tile(3182, 9611)
        private val PICKPOCKET_KNOCKOUT_TILE = Tile(3185, 9609)
        private val PICKPOCKET_SURFACE_TILES = listOf(
            Tile(3139, 3260),
            Tile(3139, 3228),
            Tile(3186, 3210),
            Tile(3148, 3216),
            Tile(3164, 3238),
        )
        private val PICKPOCKET_PUNISHMENT_TILES = listOf(PICKPOCKET_KNOCKOUT_TILE) + PICKPOCKET_SURFACE_TILES
        private val PICKPOCKETABLE_MEMBERS = setOf("ham_member_ham_cave", "ham_member_ham_cave_2")
        private val HAM_ROBE_PIECES = mapOf(
            EquipSlot.Hat to "ham_hood",
            EquipSlot.Cape to "ham_cloak",
            EquipSlot.Amulet to "ham_logo",
            EquipSlot.Chest to "ham_shirt",
            EquipSlot.Legs to "ham_robe",
            EquipSlot.Hands to "ham_gloves",
            EquipSlot.Feet to "ham_boots",
        )
        private val NORMAL_FAIL_LINES = listOf(
            "What do you think you're doing?",
            "We deal harshly with thieves around here!",
            "Stop! %s is a thief!",
            "Keep thine hands to thineself %s.",
        )
        private const val AGILITY_AVOID_ROLL_MAX = 256

        // OSRS wiki success chart data: 10/256 -> 250/256 without a lockpick, 60/256 -> 254/256 with one.
        private val PICKLOCK_CHANCES_WITH_LOCKPICK = 60..254
        private val PICKLOCK_CHANCES_BY_HAND = 10..250
        private val PRISON_DOOR_CHANCES_WITH_LOCKPICK = arrayOf(60..300, 80..320, 100..340, 120..360)
        private val PRISON_DOOR_CHANCES_BY_HAND = arrayOf(10..252, 30..273, 50..294, 70..315)
        private var registered = false

        private fun registerSender() {
            if (!registered) {
                ZoneBatchUpdates.register(LocalTrapdoorSender)
                registered = true
            }
        }

        fun handleLockedTrapdoorOpen(player: Player, target: GameObject): Boolean {
            if (target.tile != TRAPDOOR_TILE) {
                return false
            }
            if (player[LOCKED_TRAPDOOR_OPEN_TICK, -1] == GameLoop.tick) {
                return true
            }
            player[LOCKED_TRAPDOOR_OPEN_TICK] = GameLoop.tick
            player.clearAnim()
            player.message("This trapdoor seems totally locked.")
            return true
        }

        fun handlePrivateTrapdoorClose(player: Player, target: GameObject): Boolean {
            if (target.tile != TRAPDOOR_TILE || target.intId != OPEN_TRAPDOOR_ID) {
                return false
            }
            if (player[OPEN_TRAPDOOR_CLOSE_TICK, -1] == GameLoop.tick) {
                return true
            }
            player[OPEN_TRAPDOOR_CLOSE_TICK] = GameLoop.tick
            closeTrapdoor(player, target)
            return true
        }

        fun handleLockedPrisonDoorOpen(player: Player, target: GameObject): Boolean {
            if (target.tile != PRISON_DOOR_TILE || target.intId != CLOSED_PRISON_DOOR_ID) {
                return false
            }
            if (player[LOCKED_PRISON_DOOR_OPEN_TICK, -1] == GameLoop.tick) {
                return true
            }
            player[LOCKED_PRISON_DOOR_OPEN_TICK] = GameLoop.tick
            player.clearAnim()
            player.message("The door is locked.")
            return true
        }

        private fun closeTrapdoor(player: Player, trapdoor: GameObject) {
            val local = player.localObjects[LOCAL_TRAPDOOR_KEY] ?: return
            if (local.intId == CLOSED_TRAPDOOR_ID) {
                return
            }
            player.localObjects[LOCAL_TRAPDOOR_KEY] = GameObject(id = CLOSED_TRAPDOOR_ID, tile = trapdoor.tile, shape = trapdoor.shape, rotation = trapdoor.rotation)
            sendLocalObjectUpdate(player, trapdoor.tile.zone)
            player.client?.send(ObjectRemoval(trapdoor.tile.id, trapdoor.shape, trapdoor.rotation))
            player.client?.send(ObjectAddition(trapdoor.tile.id, CLOSED_TRAPDOOR_ID, trapdoor.shape, trapdoor.rotation))
            World.clearQueue("$LOCAL_TRAPDOOR_RESET_QUEUE:${player.accountName}")
        }

        private fun sendLocalObjectUpdate(player: Player, zone: Zone) {
            val viewport = player.viewport ?: return
            if (!viewport.loaded) {
                return
            }
            val zoneOffset = getZoneOffset(viewport, zone)
            player.client?.updateZone(zoneOffset.x, zoneOffset.y, zone.level)
        }

        private fun getZoneOffset(viewport: Viewport, zone: Zone): Zone {
            val base = viewport.lastLoadZone.safeMinus(viewport.zoneRadius, viewport.zoneRadius)
            return zone.safeMinus(base)
        }

        fun isPickpocketableMember(target: NPC): Boolean = target.id in PICKPOCKETABLE_MEMBERS

        fun pickpocketTargetName(target: NPC): String = when (target.id) {
            "ham_member_ham_cave" -> "H.A.M. male follower"
            "ham_member_ham_cave_2" -> "H.A.M. female follower"
            else -> target.def.name
        }

        fun sendFailText(player: Player, target: NPC): Boolean {
            if (!isPickpocketableMember(target)) {
                return false
            }
            player.message("You fail to pick the ${pickpocketTargetName(target)}'s pocket.", ChatType.Filter)
            return true
        }

        fun sendFailMessage(player: Player, target: NPC): Boolean {
            if (!sendFailText(player, target)) {
                return false
            }
            val blowResult = rollBlowResult(player, target) ?: return false
            sendBlowMessage(player, blowResult)
            return true
        }

        fun rollBlowResult(player: Player, target: NPC): BlowResult? {
            if (!isPickpocketableMember(target)) {
                return null
            }
            val agility = player.levels.get(Skill.Agility).coerceIn(1, 99)
            // OSRS/2011-era wiki chart: Agility 1 -> 1/256 avoid chance, Agility 99 -> 255/256.
            val avoidChance = ((agility - 1) * 254.0 / 98.0).roundToInt() + 1
            return if (random.nextInt(AGILITY_AVOID_ROLL_MAX) < avoidChance) {
                BlowResult.PartialAvoid
            } else {
                BlowResult.Concussed
            }
        }

        fun sendBlowMessage(player: Player, target: NPC): Boolean {
            val blowResult = rollBlowResult(player, target) ?: return false
            sendBlowMessage(player, blowResult)
            return true
        }

        fun sendBlowMessage(player: Player, blowResult: BlowResult) {
            player.message(blowResult.message, ChatType.Filter)
        }

        fun sendCaughtDialogue(player: Player, target: NPC): Boolean {
            if (!isPickpocketableMember(target)) {
                return false
            }
            val line = NORMAL_FAIL_LINES[random.nextInt(NORMAL_FAIL_LINES.size)].format(player.name)
            sendNpcDialogue(player, target, line)
            return true
        }

        private fun sendNpcDialogue(player: Player, target: NPC, line: String) {
            player.message("$CHATBOX_MEMBER_NAME: $line", ChatType.Filter)
            target.say(line)
        }

        suspend fun applyConcussionCounter(player: Player, target: NPC, blowResult: BlowResult): Boolean {
            if (target.id !in PICKPOCKETABLE_MEMBERS) {
                return false
            }
            if (player.tile.region.id != HAM_HIDEOUT_REGION_ID) {
                player.clear(CONCUSSION_COUNTER_VAR)
                return false
            }
            if (!blowResult.countsTowardCaught) {
                return false
            }
            if (player.preventsCaughtPickpocketWithHamRobes()) {
                return false
            }
            val count = player.inc(CONCUSSION_COUNTER_VAR)
            if (count == PICKPOCKET_KNOCKOUT_COUNT) {
                player.punishForCaughtPickpocket(target, blowResult)
                player.clear(CONCUSSION_COUNTER_VAR)
                return true
            }
            return false
        }

        private suspend fun Player.punishForCaughtPickpocket(target: NPC, blowResult: BlowResult) {
            val destination = PICKPOCKET_PUNISHMENT_TILES[random.nextInt(PICKPOCKET_PUNISHMENT_TILES.size)]
            target.face(this)
            sendFailText(this, target)
            if (destination == PICKPOCKET_KNOCKOUT_TILE) {
                sendNpcDialogue(this, target, "Guards! We have a thief!")
            }
            sendBlowMessage(this, blowResult)
            message("You're beaten unconscious and bundled out of the HAM camp.", ChatType.Filter)
            target.anim("human_attack")
            anim("human_fall_back")
            gfx("dragon_spear_stun")
            open("fade_out")
            delay(2)
            tele(destination)
            clearAnim()
            clearGfx()
            open("fade_in")
        }

        suspend fun pickLockPrisonDoor(player: Player, door: GameObject) {
            val movedToDoor = player.isAtBlockedPrisonDoorTile()
            if (movedToDoor) {
                player.walkOverDelay(PRISON_DOOR_INTERACT_TILE)
            }
            if (!player.isInsidePrisonCell()) {
                player.queue.clear(AUTO_PRISON_DOOR_PICKLOCK_QUEUE)
                player.clearAnim()
                player.message("It is not possible to pick the lock on the door from the outside.", ChatType.Filter)
                return
            }
            if (movedToDoor) {
                player.face(Direction.WEST)
            }
            val attempt = player[PRISON_DOOR_ATTEMPTS, 0].coerceIn(0, 3)
            player.message("You attempt to pick the lock on the door.", ChatType.Filter)
            player.anim("human_lockedchest")
            player.sound("locked")
            player.delay(PRISON_DOOR_PICKLOCK_ANIMATION_DELAY)
            val chances = if (player.inventory.contains("lockpick")) PRISON_DOOR_CHANCES_WITH_LOCKPICK else PRISON_DOOR_CHANCES_BY_HAND
            if (!Level.success(player.levels.get(Skill.Thieving), chances[attempt])) {
                player.exp(Skill.Thieving, 1.0)
                player[PRISON_DOOR_ATTEMPTS] = (attempt + 1).coerceAtMost(3)
                if (random.nextInt(FUMBLE_MESSAGE_CHANCE) == 0) {
                    player.message("You fail to pick the lock - your fingers get numb from fumbling with the lock.", ChatType.Filter)
                    if (random.nextInt(FUMBLE_DEBUFF_CHANCE) == 0) {
                        player.levels.drain(Skill.Thieving, FUMBLE_DEBUFF_LEVELS)
                        player.softTimers.restart("restore_stats")
                    }
                }
                player.clearAnim()
                player.queue.clear(AUTO_PRISON_DOOR_PICKLOCK_QUEUE)
                player.weakQueue(AUTO_PRISON_DOOR_PICKLOCK_QUEUE, PICKLOCK_RETRY_DELAY) {
                    pickLockPrisonDoor(this, door)
                }
                return
            }
            player.queue.clear(AUTO_PRISON_DOOR_PICKLOCK_QUEUE)
            player.clear(PRISON_DOOR_ATTEMPTS)
            player.message("You pick the lock on the prison door.", ChatType.Filter)
            player.sound("chest_open")
            player.exp(Skill.Thieving, 4.0)
            player.clearAnim()
            openDoor(player, door, door.def(player), ticks = 3, collision = false)
            player.walkTo(PRISON_DOOR_EXIT_TILE, noCollision = true, forceWalk = true)
        }

        private fun Player.isInsidePrisonCell(): Boolean = tile.x >= 3183 && tile.y <= 9611

        private fun Player.isAtBlockedPrisonDoorTile(): Boolean = tile in PRISON_DOOR_BLOCKED_TILES

        private fun Player.preventsCaughtPickpocketWithHamRobes(): Boolean {
            val protectionChance = equippedHamRobePieces() * HAM_ROBE_PROTECTION_PER_PIECE
            return protectionChance > 0 && random.nextInt(HAM_ROBE_PROTECTION_ROLL_MAX) < protectionChance
        }

        private fun Player.equippedHamRobePieces(): Int = HAM_ROBE_PIECES.count { (slot, itemId) -> equipped(slot).id == itemId }
    }

    init {
        registerSender()
        registerOpenHandler("ham_hideout") { target ->
            handleLockedTrapdoorOpen(this, target)
        }
        registerCloseHandler("ham_hideout") { target ->
            handlePrivateTrapdoorClose(this, target)
        }
        moved { from ->
            if (from.region.id == HAM_HIDEOUT_REGION_ID && tile.region.id != HAM_HIDEOUT_REGION_ID) {
                clear(CONCUSSION_COUNTER_VAR)
                clear(PRISON_DOOR_ATTEMPTS)
                queue.clear(AUTO_PRISON_DOOR_PICKLOCK_QUEUE)
                message("You leave the HAM Fanatics' Camp.")
            }
        }
        objectOperate("Pick-lock", "trapdoor_24_closed") { (target) ->
            if (target.tile != TRAPDOOR_TILE) {
                return@objectOperate
            }
            queue.clear(AUTO_PICKLOCK_QUEUE)
            pickLockTrapdoor(target)
        }

        objectOperate("Pick-lock") { (target) ->
            if (target.tile != PRISON_DOOR_TILE || target.intId != CLOSED_PRISON_DOOR_ID) {
                return@objectOperate
            }
            queue.clear(AUTO_PRISON_DOOR_PICKLOCK_QUEUE)
            pickLockPrisonDoor(this, target)
        }

        objTeleportLand("Climb-down", "trapdoor_24_opened") { target, _ ->
            if (target.tile != TRAPDOOR_TILE) {
                return@objTeleportLand
            }
            face(Direction.SOUTH)
            message("You climb down through the trapdoor...")
            delay(1)
            message("... and enter a dimly lit cavern area.")
        }
    }

    private fun Player.pickLockTrapdoor(trapdoor: GameObject) {
        message("You attempt to pick the lock on the trapdoor.", ChatType.Filter)
        anim("open_chest")
        sound("locked")
        val chances = if (inventory.contains("lockpick")) PICKLOCK_CHANCES_WITH_LOCKPICK else PICKLOCK_CHANCES_BY_HAND
        if (!Level.success(levels.get(Skill.Thieving), chances)) {
            if (random.nextInt(TRAPDOOR_FAIL_XP_CHANCE) == 0) {
                exp(Skill.Thieving, 1.0)
            }
            if (random.nextInt(FUMBLE_MESSAGE_CHANCE) == 0) {
                message("You fail to pick the lock - your fingers get numb from fumbling with the lock.", ChatType.Filter)
                if (random.nextInt(FUMBLE_DEBUFF_CHANCE) == 0) {
                    levels.drain(Skill.Thieving, FUMBLE_DEBUFF_LEVELS)
                    softTimers.restart("restore_stats")
                }
            }
            queue.clear(AUTO_PICKLOCK_QUEUE)
            weakQueue(AUTO_PICKLOCK_QUEUE, PICKLOCK_RETRY_DELAY) {
                pickLockTrapdoor(trapdoor)
            }
            return
        }
        message("You pick the lock on the trapdoor.", ChatType.Filter)
        sound("chest_open")
        exp(Skill.Thieving, 4.0)
        openTrapdoor(trapdoor)
    }

    private fun Player.openTrapdoor(trapdoor: GameObject) {
        localObjects[LOCAL_TRAPDOOR_KEY] = GameObject(id = OPEN_TRAPDOOR_ID, tile = trapdoor.tile, shape = trapdoor.shape, rotation = trapdoor.rotation)
        sendLocalObjectUpdate(this, trapdoor.tile.zone)
        client?.send(ObjectRemoval(trapdoor.tile.id, trapdoor.shape, trapdoor.rotation))
        client?.send(ObjectAddition(trapdoor.tile.id, OPEN_TRAPDOOR_ID, trapdoor.shape, trapdoor.rotation))
        World.clearQueue("$LOCAL_TRAPDOOR_RESET_QUEUE:$accountName")
        World.queue("$LOCAL_TRAPDOOR_RESET_QUEUE:$accountName", initialDelay = OPEN_DURATION) {
            closeTrapdoor(trapdoor)
        }
    }

    private fun Player.closeTrapdoor(trapdoor: GameObject) {
        closeTrapdoor(this, trapdoor)
    }

    private object LocalTrapdoorSender : ZoneBatchUpdates.Sender {
        override fun send(player: Player, zone: Zone) {
            val trapdoor = player.localObjects[LOCAL_TRAPDOOR_KEY] ?: return
            if (trapdoor.tile.zone != zone) {
                return
            }
            val original = GameObjects.getLayer(trapdoor.tile, ObjectLayer.layer(trapdoor.shape)) ?: return
            sendLocalObjectUpdate(player, zone)
            player.client?.send(ObjectRemoval(trapdoor.tile.id, original.shape, original.rotation))
            player.client?.send(ObjectAddition(trapdoor.tile.id, trapdoor.intId, trapdoor.shape, trapdoor.rotation))
        }
    }
}
