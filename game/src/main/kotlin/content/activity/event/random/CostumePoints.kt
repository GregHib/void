package content.activity.event.random

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * Award a costume point locked to one outfit - each costume's own random event grants one on
 * completion, spendable only on that outfit at Iffie's costume store. Points from the random
 * event gift's "Save up for a costume!" choice go to the any-outfit `costume_points` balance
 * instead.
 */
fun Player.rewardCostumePoint(costume: String) {
    inc("${costume}_costume_points")
    message("You've earned a point towards a costume; visit Iffie in Varrock's clothes shop to spend it.")
}
