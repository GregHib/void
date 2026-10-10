package content.skill.construction

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.skill.construction.House.Companion.houseOwner
import content.entity.proj.shoot
import content.skill.construction.House.Companion.inOwnHouse
import content.skill.melee.weapon.attackRange
import content.skill.ranged.Ammo
import content.skill.ranged.ammo
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

/**
 * Games room furniture: the prize chest which holds the owners prize money, and the ranging games hoop and stick, darts and archery.
 * One game at a time can be offered the prize, the winner is given a prize key to take it from the chest with.
 * https://oldschool.runescape.wiki/w/Games_room
 */
class HouseGamesRoom : Script {
    init {
        interfaceClosed("poh_ranging") {
            leaveBoard()
            softTimers.stop("house_ranging")
        }

        timerStart("house_ranging") { SCORE_BOARD_TICKS }

        timerTick("house_ranging") {
            close("poh_ranging")
            Timer.CANCEL
        }

        moved { from ->
            if (tile.zone != from.zone && hasOpen("poh_ranging")) {
                close("poh_ranging")
            }
        }

        objectOperate("Open", CHESTS) { (target) ->
            chest(target)
        }

        itemOnObjectOperate("prize_key", CHESTS) { (target) ->
            chest(target)
        }

        objectApproach("Hoop", "hoop_stick,hoop_and_stick") { (target) ->
            shoot(target, Game.Hoop)
        }

        objectApproach("Throw-at", "dartboard,dartboard_hit") { (target) ->
            shoot(target, Game.Darts)
        }

        objectApproach("Shoot-at", "house_archery_target,house_archery_target_hit") { (target) ->
            shoot(target, Game.Archery)
        }
    }

    /**
     * Tells the player how much is in the [chest] and what it's being played for, and lets them take it with a prize key or add coins.
     */
    private suspend fun Player.chest(chest: GameObject) {
        val owner = houseOwner() ?: return
        val prize = owner["house_prize_money", 0]
        val id = owner.get<String>("house_prize_game")
        val status = when (id) {
            null -> "No game is currently being played for the prize."
            FAIRY -> "The prize will be awarded to the finder of the treasure fairy."
            else -> "The prize will be awarded to the winner of the ${(Game.entries.firstOrNull { it.id == id }?.label ?: id)} game."
        }
        val text = "You must have a prize key or be in building mode to take the prize. Prize amount: <col=7f0000>${prize.toDigitGroupString()}</col>. $status"
        message(text, ChatType.Broadcast)
        item("prize_key", text)
        val own = inOwnHouse()
        val canTake = prize > 0 && (carriesItem("prize_key") || (own && get("house_build_mode", false)))
        val capacity = Tables.int("house_prize_chests.${chest.id}.capacity")
        if (canTake) {
            choice("Select an option") {
                option("Take prize") {
                    takePrize(owner, chest)
                }
                if (own) {
                    option("Add coins") {
                        addCoins(chest, capacity)
                    }
                }
            }
        } else if (own) {
            choice("Add coins to the prize chest?") {
                option("Yes") {
                    addCoins(chest, capacity)
                }
                option("No")
            }
        }
    }

    private fun Player.takePrize(owner: Player, chest: GameObject) {
        val prize = owner["house_prize_money", 0]
        if (prize <= 0) {
            return
        }
        if (!inventory.add("coins", prize)) {
            inventoryFull()
            return
        }
        owner["house_prize_money"] = 0
        inventory.remove("prize_key")
        message("You take the prize.", ChatType.Broadcast)
        openChest(chest)
    }

    private fun Player.addCoins(chest: GameObject, capacity: Int) {
        val last = get("house_prize_added", 0)
        if (last != 0 && GameLoop.tick - last < TimeUnit.MINUTES.toTicks(COOLDOWN_MINUTES)) {
            message("After you add coins to a chest, you must wait a few minutes before doing so again.", ChatType.Broadcast)
            return
        }
        val stored = get("house_prize_money", 0)
        val amount = ADD_AMOUNT.coerceAtMost(capacity - stored)
        if (amount <= 0) {
            message("The prize chest can't hold any more coins.") // TODO proper message
            return
        }
        if (!inventory.remove("coins", amount)) {
            message("You don't have enough coins.") // TODO proper message
            return
        }
        set("house_prize_money", stored + amount)
        set("house_prize_added", GameLoop.tick)
        message("You add ${amount.toDigitGroupString()} coins.", ChatType.Broadcast)
        openChest(chest)
        sound("coins_jingle_1")
    }

    private fun Player.openChest(chest: GameObject) {
        anim("human_openheavychest")
        areaSound("chest_open", chest.tile, radius = 5)
        chest.replace("${chest.id}_open", ticks = 3)
    }

    /**
     * Throws a hoop, dart or arrow at a ranging game [target] which swaps for a moment to show the hit.
     * The score board shows the shots taken and the total score, the game ends after [SHOTS] shots.
     */
    private suspend fun Player.shoot(target: GameObject, game: Game) {
        if (!game.canPlay(this)) {
            return
        }
        if (isFull(target)) {
            return
        }
        approachRange(game.range(this))
        // Someone else may have taken the last space while walking over
        if (isFull(target)) {
            return
        }
        var shots = get("ranging_shots", 0)
        if (shots >= SHOTS || get<Tile>("ranging_target") != target.tile) {
            leaveBoard()
            clear("ranging_shots")
            clear("ranging_score")
            shots = 0
        }
        if (!contains("ranging_target")) {
            set("ranging_target", target.tile)
            set("ranging_joined", GameLoop.tick)
        }
        // Counts as a player of the game straight away so the space can't be taken while mid shot
        set("ranging_shots", shots)
        steps.clear()
        face(target)
        game.start(this, target.tile)
        delay(game.flight)
        val score = game.score(this)
        shots++
        val total = get("ranging_score", 0) + score
        set("ranging_shots", shots)
        set("ranging_score", total)
        if (target.id == game.base) {
            target.replace(game.hit, ticks = 4)
        }
        areaSound(game.land, target.tile, radius = 5)
        exp(Skill.Ranged, game.experience())
        softTimers.start("house_ranging", restart = true)
        refreshBoard(target.tile)
        interfaces.sendText("poh_ranging", "last_shot", if (score == 0) "You missed" else "You scored $score")
        if (shots == 1) {
            offerPrize(game.id)
        }
        if (shots >= SHOTS) {
            winPrize(game.id)
        }
    }

    /**
     * Players currently taking part in the game at [target] in the order they joined
     */
    private fun participants(target: Tile) = Players
        .filter { it.contains("ranging_shots") && it.get<Tile>("ranging_target") == target }
        .sortedBy { it["ranging_joined", 0] }

    /**
     * Whether four other players are already playing the game at [target] and this player isn't one of them
     */
    private fun Player.isFull(target: GameObject): Boolean {
        if (participants(target.tile).count { it != this } < MAX_PLAYERS) {
            return false
        }
        message("This game already has $MAX_PLAYERS players, wait for a space to free up.")
        return true
    }

    /**
     * Removes this player from the game they were playing and updates the score board for everyone else
     */
    private fun Player.leaveBoard() {
        val target = get<Tile>("ranging_target") ?: return
        clear("ranging_target")
        clear("ranging_joined")
        clear("ranging_shots")
        clear("ranging_score")
        refreshBoard(target)
    }

    /**
     * Shows every player of the game at [target] their name, shots and score
     * with the names set as variables as the name components run scripts reading them.
     */
    private fun refreshBoard(target: Tile) {
        val players = participants(target)
        for (viewer in players) {
            if (!viewer.hasOpen("poh_ranging")) {
                viewer.open("poh_ranging")
            }
            for (row in 1..MAX_PLAYERS) {
                val player = players.getOrNull(row - 1)
                val visible = player != null
                viewer.interfaces.sendVisibility("poh_ranging", "name_$row", visible)
                viewer.interfaces.sendVisibility("poh_ranging", "shots_$row", visible)
                viewer.interfaces.sendVisibility("poh_ranging", "score_$row", visible)
                if (player == null) {
                    viewer.interfaces.sendText("poh_ranging", "winner_$row", "")
                    continue
                }
                val shots = player["ranging_shots", 0]
                viewer["ranging_name_$row"] = player.name
                viewer.interfaces.sendText("poh_ranging", "shots_$row", shots.toString())
                viewer.interfaces.sendText("poh_ranging", "score_$row", player["ranging_score", 0].toString())
                viewer.interfaces.sendText("poh_ranging", "winner_$row", if (shots >= SHOTS) "Winner!" else "")
            }
        }
    }

    private enum class Game(val id: String, val label: String, val flight: Int, val land: String, val base: String, val hit: String) {
        Hoop("hoop", "hoop and stick", 2, "poh_stick_on_hoop", "hoop_stick", "hoop_and_stick") {
            override fun canPlay(player: Player) = true

            override fun start(player: Player, target: Tile) {
                player.anim("human_throw_hoop2")
                player.sound("thrown")
                player.shoot("poh_hoop_projectile", target, flightTime = 30, height = 0, endHeight = 0, curve = 2, offset = 128)
            }

            override fun score(player: Player) = if (random.nextInt(100) < 40 + player.levels.get(Skill.Ranged) / 2) 1 else 0

            override fun experience() = 2.0 + random.nextInt(2)
        },
        Darts("darts", "darts", 2, "poh_dart_land", "dartboard", "dartboard_hit") {
            override fun range(player: Player) = player.attackRange

            override fun canPlay(player: Player): Boolean {
                val weapon = player.equipped(EquipSlot.Weapon).id
                if (!weapon.endsWith("_dart") && !weapon.endsWith("_knife") && !weapon.endsWith("_javelin") && !weapon.endsWith("_throwing_axe")) {
                    player.message("You must wield a thrown weapon.")
                    return false
                }
                player.ammo = weapon
                return true
            }

            override fun start(player: Player, target: Tile) {
                val ammo = player.ammo
                player.shoot(Ammo.graphic(ammo), target, endHeight = 40)
                player.gfx("${ammo.removePrefix("corrupt_").removeSuffix("_p++").removeSuffix("_p+").removeSuffix("_p")}_throw")
                player.anim("ii_human_dart_throw")
                player.sound("dart")
                player.equipment.remove(EquipSlot.Weapon.index, player.equipped(EquipSlot.Weapon).id, 1)
            }

            override fun score(player: Player) = 1 + random.nextInt(3)

            override fun experience() = 7.0
        },
        Archery("archery", "archery", 3, "poh_dart_land", "house_archery_target", "house_archery_target_hit") {
            override fun range(player: Player) = player.attackRange

            override fun canPlay(player: Player): Boolean {
                val weapon = player.equipped(EquipSlot.Weapon).id
                if (!weapon.endsWith("bow") || weapon.endsWith("crossbow")) {
                    player.message("You must have a bow equipped to play the ranging game.")
                    return false
                }
                if (player.equipped(EquipSlot.Ammo).isEmpty()) {
                    player.message("There is no ammo left in your quiver.")
                    return false
                }
                player.ammo = player.equipped(EquipSlot.Ammo).id
                return true
            }

            override fun start(player: Player, target: Tile) {
                val ammo = Ammo.graphic(player.ammo)
                player.shoot(ammo, target, endHeight = 20)
                player.gfx("${ammo}_shoot")
                player.anim("human_bow")
                player.sound("arrowlaunch2")
                player.equipment.remove(EquipSlot.Ammo.index, player.equipped(EquipSlot.Ammo).id, 1)
            }

            override fun score(player: Player): Int {
                val roll = random.nextInt(100) + player.levels.get(Skill.Ranged) / 4
                return when {
                    roll < 20 -> 0
                    roll < 45 -> 1
                    roll < 65 -> 2
                    roll < 85 -> 3
                    roll < 100 -> 5
                    else -> 10
                }
            }

            override fun experience() = 10.0
        }, ;

        /**
         * Distance the player shoots from, the hoop isn't a weapon so has a set distance, darts and arrows use the range of the wielded weapon.
         */
        open fun range(player: Player): Int = RANGE

        abstract fun canPlay(player: Player): Boolean

        abstract fun start(player: Player, target: Tile)

        abstract fun score(player: Player): Int

        abstract fun experience(): Double
    }

    companion object {
        private const val CHESTS = "oak_prize_chest,teak_prize_chest,mahogany_prize_chest"
        private const val SHOTS = 10
        private const val MAX_PLAYERS = 4
        private const val RANGE = 6 // Guessed
        private const val SCORE_BOARD_TICKS = 50 // Guessed
        private const val ADD_AMOUNT = 10_000
        private const val COOLDOWN_MINUTES = 5 // Guessed
        const val FAIRY = "fairy"

        /**
         * Lets the owner choose to put the prize up for the game [id] they have just started, if nothing else is being played for it.
         */
        suspend fun Player.offerPrize(id: String) {
            val owner = houseOwner() ?: return
            if (owner != this || owner["house_prize_money", 0] <= 0 || owner.contains("house_prize_game")) {
                return
            }
            choice("Offer the prize for this game?") {
                option("Yes") {
                    owner["house_prize_game"] = id
                }
                option("No")
            }
        }

        /**
         * Gives the winner of the game [id] a prize key if it's the game the prize was offered for.
         */
        fun Player.winPrize(id: String) {
            val owner = houseOwner() ?: return
            if (owner.get<String>("house_prize_game") != id) {
                return
            }
            if (!inventory.add("prize_key")) {
                inventoryFull()
                return
            }
            owner.clear("house_prize_game")
            message("A little key appears in your pack!")
            sound("ping")
        }
    }
}
