package content.skill.construction

import content.skill.construction.House.Companion.notImplemented
import world.gregs.voidps.engine.Script

/**
 * Games room furniture: game spaces, prize chests, attack stones, elemental balances and ranging games
 */
class HouseGamesRoom : Script {
    init {
        objectOperate("Activate", "jester,treasure_hunt,hangman_game,elemental_balance_*") {
            notImplemented()
        }

        objectOperate("Open", "oak_prize_chest,teak_prize_chest,mahogany_prize_chest") {
            notImplemented()
        }

        objectOperate("Set-up", "clay_attack_stone,limestone_attack_stone,marble_attack_stone") {
            notImplemented()
        }

        objectOperate("Hoop", "hoop_and_stick") {
            notImplemented()
        }

        objectOperate("Throw-at", "dartboard") {
            notImplemented()
        }

        objectOperate("Shoot-at", "house_archery_target") {
            notImplemented()
        }
    }
}
