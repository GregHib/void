package content.skill.constitution.drink

import content.entity.player.bank.bank
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random

/**
 * Herblore Habitat potions all last five minutes. The mining and woodcutting potions open a
 * banking window rather than sending every resource to the bank, though the roll is repeated on
 * each resource gathered, so in practice the window is usually refreshed before it closes.
 */
private const val INTERVAL = 25
private const val DURATION = 500 / INTERVAL
private const val BANK_DURATION = 75 / INTERVAL
private const val BANK_PERCENT = 11

const val JUJU_HERB_ONE_IN = 3
const val JUJU_SHARK_PERCENT = 30

private val EFFECTS = listOf(
    "juju_mining",
    "juju_woodcutting",
    "juju_farming",
    "juju_fishing",
    "scentless",
    "saradomins_blessing",
    "guthixs_gift",
    "zamoraks_favour",
)

private val WINDOWS = listOf("juju_mining_bank", "juju_woodcutting_bank")

fun Player.startJuju(effect: String) {
    set(effect, DURATION)
    timers.restart(effect)
}

fun Player.jujuActive(effect: String): Boolean = timers.contains(effect)

/**
 * Rolls for the banking window on every resource gathered, then banks [amount] of [item] while
 * that window is open. Returns false when the bank cannot take them, so the caller keeps its
 * normal inventory handling rather than destroying the resource.
 */
fun Player.jujuBank(effect: String, item: String, amount: Int): Boolean {
    if (jujuActive(effect) && random.nextInt(100) < BANK_PERCENT) {
        set("${effect}_bank", BANK_DURATION)
        timers.restart("${effect}_bank")
    }
    if (!jujuActive("${effect}_bank")) {
        return false
    }
    if (!bank.add(item, amount)) {
        return false
    }
    gfx("${effect}_bank")
    return true
}

class Juju : Script {

    init {
        playerSpawn {
            for (timer in EFFECTS + WINDOWS) {
                if (get(timer, 0) > 0) {
                    timers.restart(timer)
                }
            }
        }

        playerDeath {
            for (timer in EFFECTS + WINDOWS) {
                clear(timer)
            }
        }

        for (timer in EFFECTS + WINDOWS) {
            timerStart(timer) { INTERVAL }
            timerTick(timer) {
                if (dec(timer) <= 0) Timer.CANCEL else Timer.CONTINUE
            }
            timerStop(timer) { logout ->
                if (!logout) {
                    clear(timer)
                }
            }
        }

        consumable("juju_hunter_potion*") {
            message("You decide that only plants are likely to enjoy the taste of this potion.")
            false
        }
    }
}
