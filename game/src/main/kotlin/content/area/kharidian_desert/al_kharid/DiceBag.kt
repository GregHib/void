package content.area.kharidian_desert.al_kharid

import content.entity.gfx.areaGfx
import content.entity.player.dialogue.type.choice
import content.social.clan.clan
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.type.Direction
import kotlin.random.Random

/**
 * The dice bag, bought from Faruq in Al Kharid. Operating ("Choose-dice") the bag
 * takes a die out into the inventory; each die can then be rolled privately or to
 * the player's clan chat. Authentic cache options drive every handler:
 *  - dice_bag: Choose-dice
 *  - each die: Private-roll, Clan-roll, Choose-dice, Put-away
 */
class DiceBag : Script {

    private data class Die(val id: String, val noun: String, val gfx: String, val roll: () -> Int)

    private val dice = listOf(
        Die("die_6_sides", "a six-sided die", "dice_roll_6_sides") { Random.nextInt(1, 7) },
        Die("dice_2_6_sides", "two six-sided dice", "dice_roll_2_6_sides") { Random.nextInt(1, 7) + Random.nextInt(1, 7) },
        Die("die_8_sides", "an eight-sided die", "dice_roll_8_sides") { Random.nextInt(1, 9) },
        Die("die_10_sides", "a ten-sided die", "dice_roll_10_sides") { Random.nextInt(1, 11) },
        Die("die_12_sides", "a twelve-sided die", "dice_roll_12_sides") { Random.nextInt(1, 13) },
        Die("die_20_sides", "a twenty-sided die", "dice_roll_20_sides") { Random.nextInt(1, 21) },
        Die("dice_up_to_100", "the percentile dice", "dice_roll_up_to_100") { Random.nextInt(1, 101) },
        Die("die_4_sides", "a four-sided die", "dice_roll_4_sides") { Random.nextInt(1, 5) },
    )

    init {
        itemOption("Choose-dice", "dice_bag") {
            val chosen = chooseDie() ?: return@itemOption
            takeOut(chosen)
        }

        for (die in dice) {
            itemOption("Choose-dice", die.id) {
                val chosen = chooseDie() ?: return@itemOption
                if (chosen.id != die.id) {
                    inventory.replace(it.slot, die.id, chosen.id)
                }
            }
            itemOption("Private-roll", die.id) { roll(die, toClan = false) }
            itemOption("Clan-roll", die.id) { roll(die, toClan = true) }
            itemOption("Put-away", die.id) { (_, slot) ->
                inventory.remove(slot, die.id)
            }
        }
    }

    private fun Player.takeOut(die: Die) {
        val current = dice.firstOrNull { inventory.contains(it.id) }
        if (current != null) {
            if (current.id != die.id) {
                inventory.replace(current.id, die.id)
            }
            return
        }
        if (!inventory.add(die.id)) {
            message("You don't have enough inventory space to take out a die.")
        }
    }

    private suspend fun Player.roll(die: Die, toClan: Boolean) {
        val channel = clan
        if (toClan && channel == null) {
            message("You need to be in a clan chat to use this option.")
            return
        }
        val facing = if (direction == Direction.NONE) Direction.SOUTH else direction
        anim("dice_roll")
        areaGfx(die.gfx, tile.add(facing))
        delay(1)
        val result = "<col=db3535>${die.roll()}</col>"
        if (toClan && channel != null) {
            for (member in channel.members) {
                member.message("Clan channel-mate $name rolled $result on ${die.noun}.", ChatType.ClanChat)
            }
        } else {
            message("You roll $result on ${die.noun}.")
        }
    }

    private suspend fun Player.chooseDie(): Die? {
        var selected: Die? = null
        choice("Choose a die to take out") {
            option("Six-sided die") { selected = die("die_6_sides") }
            option("Two six-sided dice") { selected = die("dice_2_6_sides") }
            option("Four-sided die") { selected = die("die_4_sides") }
            option("More dice...") {
                choice("Choose a die to take out") {
                    option("Eight-sided die") { selected = die("die_8_sides") }
                    option("Ten-sided die") { selected = die("die_10_sides") }
                    option("Twelve-sided die") { selected = die("die_12_sides") }
                    option("Twenty-sided die") { selected = die("die_20_sides") }
                    option("Percentile dice (1-100)") { selected = die("dice_up_to_100") }
                }
            }
        }
        return selected
    }

    private fun die(id: String): Die = dice.first { it.id == id }
}
