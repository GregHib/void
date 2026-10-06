package content.skill.construction

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.intEntry
import content.skill.construction.House.Companion.houseOwner
import content.skill.construction.House.Companion.inOwnHouse
import content.skill.construction.House.Companion.notImplemented
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

/**
 * Games room furniture: game spaces, prize chests which hold the owners prize money, attack stones, elemental balances and ranging games
 * https://oldschool.runescape.wiki/w/Games_room
 */
class HouseGamesRoom : Script {
    init {
        interfaceOpened("poh_ranging") { id ->
            interfaces.sendText(id, "name_1", accountName)
            for (player in 2..4) {
                interfaces.sendText(id, "name_$player", "")
                interfaces.sendText(id, "shots_$player", "")
                interfaces.sendText(id, "score_$player", "")
            }
        }

        interfaceClosed("poh_ranging") {
            clear("ranging_shots")
            clear("ranging_score")
        }

        moved { from ->
            if (tile.zone != from.zone && hasOpen("poh_ranging")) {
                close("poh_ranging")
            }
        }

        objectOperate("Activate", "jester,treasure_hunt,elemental_balance_*") {
            notImplemented()
        }

        objectOperate("Set-up", "clay_attack_stone,limestone_attack_stone,marble_attack_stone") {
            notImplemented()
        }

        objectOperate("Open", "oak_prize_chest,teak_prize_chest,mahogany_prize_chest") { (target) ->
            if (!inOwnHouse()) {
                val prize = houseOwner()?.get("house_prize_money", 0) ?: 0
                message(if (prize > 0) "The chest holds $prize coins for the winner of the games." else "The chest is empty.") // TODO proper message
                return@objectOperate
            }
            prizeMoney(Tables.int("house_prize_chests.${target.id}.capacity"))
        }

        objectApproach("Hoop", "hoop_and_stick") { (target) ->
            play(target, "throw_dart", "hoop")
        }

        objectOperate("Throw-at", "dartboard") { (target) ->
            if (!equipped(EquipSlot.Weapon).id.contains("_dart")) {
                message("You need to be wielding some darts to play darts.") // TODO proper message
                return@objectOperate
            }
            play(target, "throw_dart", "dart")
        }

        objectOperate("Shoot-at", "house_archery_target") { (target) ->
            val weapon = equipped(EquipSlot.Weapon).id
            if (!weapon.endsWith("bow") || weapon.endsWith("crossbow") || !equipped(EquipSlot.Ammo).id.contains("_arrow")) {
                message("You need a bow and arrows to use the archery target.") // TODO proper message
                return@objectOperate
            }
            play(target, "bow_accurate", "arrow")
        }
    }

    /**
     * Throws or shoots at a ranging game [target], scoring higher with better Ranged levels.
     * The score board shows the shots taken and the total score, the game ends after [SHOTS] shots.
     */
    private suspend fun Player.play(target: GameObject, animation: String, thing: String) {
        face(target)
        anim(animation)
        delay(2)
        val score = (random.nextInt(levels.get(Skill.Ranged) + 1) * MAX_SCORE / 99).coerceAtMost(MAX_SCORE)
        val shots = inc("ranging_shots")
        val total = get("ranging_score", 0) + score
        set("ranging_score", total)
        open("poh_ranging")
        interfaces.sendText("poh_ranging", "shots_1", shots.toString())
        interfaces.sendText("poh_ranging", "score_1", total.toString())
        message(
            when {
                score == 0 -> "Your $thing misses completely." // TODO proper message
                score == MAX_SCORE -> "Your $thing hits dead centre, you score $score!" // TODO proper message
                else -> "Your $thing hits, you score $score." // TODO proper message
            },
        )
        if (shots >= SHOTS) {
            message("You finish the game with a score of $total.") // TODO proper message
            delay(3)
            close("poh_ranging")
        }
    }

    /**
     * Lets the owner add or take coins from the prize money held in the chest, up to the chests [capacity]
     */
    private suspend fun Player.prizeMoney(capacity: Int) {
        val prize = get("house_prize_money", 0)
        choice("The chest holds $prize coins.") {
            // TODO proper message
            option("Add coins") {
                val stored = get("house_prize_money", 0)
                val amount = intEntry("How many coins would you like to add?").coerceAtMost(inventory.count("coins")).coerceAtMost(capacity - stored)
                if (amount > 0 && inventory.remove("coins", amount)) {
                    set("house_prize_money", stored + amount)
                }
            }
            option("Take coins") {
                val stored = get("house_prize_money", 0)
                val amount = intEntry("How many coins would you like to take?").coerceAtMost(stored)
                if (amount > 0 && inventory.add("coins", amount)) {
                    set("house_prize_money", stored - amount)
                }
            }
            option("Cancel")
        }
    }

    companion object {
        private const val MAX_SCORE = 50 // Guessed
        private const val SHOTS = 5 // Guessed
    }
}
