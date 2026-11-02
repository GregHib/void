package content.area.misthalin.ham_hideout

import content.entity.obj.door.Door.openDoor
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class HamHideout : Script {

    init {
        objectOperate("Pick-lock", "trapdoor_24_closed") { (target) ->
            pickLockTrapdoor(target)
        }

        objectOperate("Open", "trapdoor_24_closed") { (target) ->
            if (target.tile != Tile(3165, 3252)) {
                return@objectOperate
            }
            sound("locked")
            message("You try to open the trapdoor.")
            message("This trapdoor seems totally locked.")
        }

        objectOperate("Open", "door_148_closed") { (target) ->
            if (target.tile == PRISON_DOOR_TILE) {
                message("This door seems very secure.")
            }
        }

        objectOperate("Pick-lock", "door_148_closed") { (target) ->
            if (target.tile == PRISON_DOOR_TILE) {
                pickLockPrisonDoor(target)
            }
        }

        objTeleportLand("Climb-down", "trapdoor_24_opened") { _, _ ->
            message("You climb down through the trapdoor...")
            delay(1)
            message("...and enter a dimly lit cavern area.")
        }

        objectOperate("Climb-up", "ham_hideout_ladder") {
            tele(SURFACE_TILE)
            message("You climb back up to the surface.")
        }

        moved { from ->
            if (from.region.id == HAM_HIDEOUT_REGION_ID && tile.region.id != HAM_HIDEOUT_REGION_ID) {
                clear(CONCUSSION_COUNTER)
            }
        }
    }

    private fun Player.pickLockTrapdoor(trapdoor: GameObject) {
        message("You attempt to pick the lock on the trapdoor.", ChatType.Filter)
        anim("open_chest")
        sound("locked")
        weakQueue(PICKLOCK_QUEUE, PICKLOCK_DELAY) {
            val chances = if (inventory.contains("lockpick")) PICKLOCK_CHANCES_WITH_LOCKPICK else PICKLOCK_CHANCES_BY_HAND
            if (!Level.success(levels.get(Skill.Thieving), chances)) {
                if (random.nextInt(FAILED_ATTEMPT_XP_CHANCE) == 0) {
                    message("You fail to pick the lock - your fingers get numb from fumbling with the lock.", ChatType.Filter)
                    levels.drain(Skill.Thieving, FUMBLE_LEVELS)
                    softTimers.restart("restore_stats")
                }
                if (random.nextInt(FAILED_ATTEMPT_XP_CHANCE) == 0) {
                    exp(Skill.Thieving, 1.0)
                }
                clearAnim()
                pickLockTrapdoor(trapdoor)
                return@weakQueue
            }

            message("You pick the lock on the trapdoor.", ChatType.Filter)
            sound("chest_open")
            exp(Skill.Thieving, TRAPDOOR_PICKLOCK_XP)
            trapdoor.replace("trapdoor_24_opened", ticks = OPEN_DURATION)
            clearAnim()
        }
    }

    private suspend fun Player.pickLockPrisonDoor(door: GameObject) {
        if (tile.x < PRISON_DOOR_TILE.x || tile.y > PRISON_DOOR_INSIDE_MAX_Y) {
            message("The gate seems unpickable from this side.", ChatType.Filter)
            return
        }
        if (tile != PRISON_DOOR_TILE) {
            walkToDelay(PRISON_DOOR_TILE, forceWalk = true)
            if (tile != PRISON_DOOR_TILE) {
                return
            }
        }
        val startTile = tile
        message("You attempt to pick the lock on the prison door.", ChatType.Filter)
        anim("human_lockedchest")
        face(Direction.WEST)
        sound("locked")
        weakQueue(PICKLOCK_QUEUE, PICKLOCK_DELAY) {
            val attempt = this[PRISON_DOOR_ATTEMPTS, 0].coerceIn(0, 3)
            val chances = if (inventory.contains("lockpick")) PRISON_DOOR_CHANCES_WITH_LOCKPICK else PRISON_DOOR_CHANCES_BY_HAND
            if (!Level.success(levels.get(Skill.Thieving), chances[attempt])) {
                exp(Skill.Thieving, 0.5)
                this[PRISON_DOOR_ATTEMPTS] = (attempt + 1).coerceAtMost(3)
                message("You fail to pick the lock.", ChatType.Filter)
                clearAnim()
                face(Direction.WEST)
                if (tile != startTile) {
                    clear(PRISON_DOOR_ATTEMPTS)
                    return@weakQueue
                }
                pickLockPrisonDoor(door)
                return@weakQueue
            }
            clear(PRISON_DOOR_ATTEMPTS)
            message("You pick the lock on the prison door.", ChatType.Filter)
            sound("chest_open")
            exp(Skill.Thieving, PRISON_DOOR_PICKLOCK_XP)
            clearAnim()
            openDoor(this, door, door.def(this), ticks = 3, collision = false)
            walkTo(PRISON_DOOR_TILE, noCollision = true, forceWalk = true)
            delay(1)
            walkTo(PRISON_DOOR_EXIT_TILE, noCollision = true, forceWalk = true)
        }
    }

    companion object {
        private const val PICKLOCK_QUEUE = "ham_hideout_picklock"
        private const val PICKLOCK_DELAY = 6
        private const val TRAPDOOR_PICKLOCK_XP = 4.0
        private const val PRISON_DOOR_PICKLOCK_XP = 3.8
        private const val FAILED_ATTEMPT_XP_CHANCE = 5
        private const val FUMBLE_LEVELS = 1
        private val PICKLOCK_CHANCES_WITH_LOCKPICK = 60..254
        private val PICKLOCK_CHANCES_BY_HAND = 10..250
        private val OPEN_DURATION = TimeUnit.MINUTES.toTicks(5)
        private val SURFACE_TILE = Tile(3165, 3251)
        private val PRISON_DOOR_TILE = Tile(3183, 9611)
        private const val PRISON_DOOR_INSIDE_MAX_Y = 9612
        private val PRISON_DOOR_EXIT_TILE = Tile(3182, 9611)
        private const val PRISON_DOOR_ATTEMPTS = "ham_hideout_prison_door_attempts"
        private val PRISON_DOOR_CHANCES_WITH_LOCKPICK = arrayOf(60..300, 80..320, 100..340, 120..360)
        private val PRISON_DOOR_CHANCES_BY_HAND = arrayOf(10..252, 30..273, 50..294, 70..315)

        fun isHamMember(target: NPC): Boolean = target.id in HAM_MEMBERS

        suspend fun handleFailedPickpocket(player: Player, target: NPC): Boolean {
            if (!isHamMember(target)) {
                return false
            }
            val blow = if (random.nextInt(AGILITY_ROLL) < player.agilityAvoidChance()) {
                BlowResult.PartialAvoid
            } else {
                BlowResult.Concussed
            }
            player.message("You fail to pick the ${pickpocketTargetName(target)}'s pocket.", ChatType.Filter)
            player.message(blow.message, ChatType.Filter)
            if (blow == BlowResult.PartialAvoid || player.preventsCaughtPickpocket()) {
                return false
            }
            val count = player[CONCUSSION_COUNTER, 0] + 1
            player[CONCUSSION_COUNTER] = count
            if (count < KNOCKOUT_COUNT) {
                return false
            }
            player[CONCUSSION_COUNTER] = 0
            player.message("You're beaten unconscious and bundled out of the H.A.M. camp.", ChatType.Filter)
            player.anim("human_fall_back", override = true)
            player.gfx("dragon_spear_stun")
            player.open("fade_out")
            player.delay(4)
            player.tele(KNOCKOUT_TILES.random(random))
            player.clearAnim()
            player.open("fade_in")
            return true
        }

        private fun Player.agilityAvoidChance(): Int {
            val agility = levels.get(Skill.Agility).coerceIn(1, 99)
            return ((agility - 1) * 254.0 / 98.0).roundToInt() + 1
        }

        private fun Player.preventsCaughtPickpocket(): Boolean = random.nextInt(100) < hamRobeProtection()

        private fun Player.hamRobeProtection(): Int = HAM_ROBE_PIECES.count { (slot, item) -> equipped(slot).id == item } * HAM_ROBE_PROTECTION_PER_PIECE

        private const val HAM_HIDEOUT_REGION_ID = 12694
        private const val CONCUSSION_COUNTER = "ham_hideout_concussion_counter"
        private const val KNOCKOUT_COUNT = 3
        private const val AGILITY_ROLL = 256
        private const val HAM_ROBE_PROTECTION_PER_PIECE = 4
        private val HAM_MEMBERS = setOf("ham_member_male_ham_cave", "ham_member_female_ham_cave", "ham_member_ham_cave_3")
        private val HAM_ROBE_PIECES = mapOf(
            EquipSlot.Hat to "ham_hood",
            EquipSlot.Cape to "ham_cloak",
            EquipSlot.Amulet to "ham_logo",
            EquipSlot.Chest to "ham_shirt",
            EquipSlot.Legs to "ham_robe",
            EquipSlot.Hands to "ham_gloves",
            EquipSlot.Feet to "ham_boots",
        )
        private val KNOCKOUT_TILES = listOf(
            Tile(3185, 9609),
            Tile(3139, 3260),
            Tile(3139, 3228),
            Tile(3186, 3210),
            Tile(3148, 3216),
            Tile(3164, 3238),
        )

        fun pickpocketTargetName(target: NPC): String = when (target.id) {
            "ham_member_male_ham_cave" -> "H.A.M. male follower"
            "ham_member_female_ham_cave" -> "H.A.M. female follower"
            else -> target.def.name
        }
    }

    enum class BlowResult(val message: String) {
        PartialAvoid("Your agility helps you partially avoid the blow."),
        Concussed("You feel slightly concussed from the blow."),
    }
}
