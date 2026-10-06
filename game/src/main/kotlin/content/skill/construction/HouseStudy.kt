package content.skill.construction

import content.activity.shooting_star.ShootingStarHandler
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.intEntry
import content.skill.magic.spell.SpellRunes
import content.skill.magic.spell.SpellRunes.removeItems
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

/**
 * Study furniture: lecterns for making magic tablets, charts, telescopes for spotting shooting stars and the statue plinth,
 * along with bookcases which are also built in parlours and quest halls.
 */
class HouseStudy : Script {
    init {
        objectOperate("Study", LECTERNS) { (target) ->
            set("house_lectern", target.id)
            set("house_lectern_tier", LECTERNS.split(",").indexOf(target.id) + 1)
            anim("study_lectern")
            open("teleport_tablets")
        }


        itemOnObjectOperate(STAVES, "crystal_of_power") { (_, item, slot) ->
            val (type, element) = StaffType.entries.firstNotNullOfOrNull { type ->
                ELEMENTS.firstOrNull { type.id(it) == item.id }?.let { type to it }
            } ?: return@itemOnObjectOperate
            choice("Change to which element?") {
                for (new in ELEMENTS) {
                    if (new != element) {
                        option(new.replaceFirstChar { it.uppercase() }) {
                            changeElement(slot, item.id, type.id(new), "${new}_rune", type.cost)
                        }
                    }
                }
            }
        }

        interfaceClosed("teleport_tablets") {
            clear("house_lectern")
            clear("house_lectern_tier")
        }

        interfaceOption("Close", "teleport_tablets:close") {
            closeMenu()
        }

        interfaceOption(id = "teleport_tablets:*") {
            val lectern: String = get("house_lectern") ?: return@interfaceOption
            val tablet = it.component
            val amount = when (it.option) {
                "Make" -> 1
                "Make-5" -> 5
                "Make-10" -> 10
                "Make-X" -> intEntry("Enter amount:")
                "Make-All" -> inventory.count("soft_clay")
                else -> return@interfaceOption
            }
            closeMenu()
            if (tablet !in Tables.itemList("house_lecterns.$lectern.tablets")) {
                message("You can't make that tablet on this lectern.") // TODO proper message
                return@interfaceOption
            }
            makeTablets(tablet, Tables.int("house_lecterns.$lectern.ticks"), amount)
        }

        objectOperate("Study", "alchemical_chart") {
            message("The chart shows the symbols used by alchemists for each of the elements.") // TODO proper message
        }

        objectOperate("Study", "astronomical_chart") {
            message("The chart shows the positions of the stars and planets in the night sky.") // TODO proper message
        }

        objectOperate("Study", "infernal_chart") {
            message("The chart shows the many layers of the infernal planes.") // TODO proper message
        }

        objectOperate("Observe", "wooden_telescope,teak_telescope,mahogany_telescope") { (target) ->
            observe(target.id)
        }

        objectOperate("Check", "dahmaroc_statue_plinth") {
            // TODO replica statue pieces from Shattered Heart
            message("You haven't added any replica statue pieces to the plinth.") // TODO proper message
        }

        objectOperate("Search", "wooden_bookcase,oak_bookcase,mahogany_bookcase") {
            message("You search the bookcase but find nothing of interest.") // TODO proper message
        }
    }

    /**
     * Swaps the element of the staff in [slot] for [cost] runes of the new element, combination staves can't be changed
     */
    private fun Player.changeElement(slot: Int, old: String, new: String, rune: String, cost: Int) {
        inventory.transaction {
            if (cost > 0) {
                remove(rune, cost)
            }
            replace(slot, old, new)
        }
        when (inventory.transaction.error) {
            TransactionError.None -> {
                anim("study_lectern") // TODO proper anim
                message("The crystal of power changes your staff.") // TODO proper message
            }
            is TransactionError.Deficient -> message("You need $cost ${ItemDefinitions.get(rune).name.lowercase()}s to change that staff.") // TODO proper message
            else -> {}
        }
    }

    /**
     * Makes [amount] of [tablet] from soft clay and the runes for its spell, one every [ticks]
     * TODO quest requirements for Ardougne (Plague City) and Watchtower (Watchtower)
     */
    private fun Player.makeTablets(tablet: String, ticks: Int, amount: Int) {
        if (amount <= 0) {
            return
        }
        val spell = Tables.string("house_tablets.$tablet.spell")
        val level = SpellRunes.magicLevel(BOOK, spell) ?: return
        if (!has(Skill.Magic, level, message = true)) {
            return
        }
        if (!inventory.contains("soft_clay")) {
            message("You need some soft clay to make a tablet.") // TODO proper message
            return
        }
        val runes = SpellRunes.requiredItems(BOOK, spell)?.toMutableMap() ?: return
        inventory.transaction {
            remove("soft_clay")
            removeItems(this@makeTablets, runes, spell)
            add(tablet)
        }
        if (inventory.transaction.error != TransactionError.None) {
            return
        }
        anim("make_tablet")
        exp(Skill.Magic, Tables.int("house_tablets.$tablet.xp") / 10.0)
        weakQueue("make_tablets", ticks) {
            makeTablets(tablet, ticks, amount - 1)
        }
    }

    /**
     * Shows roughly when and where the next shooting star will land, more accurately with better [telescope]s
     * https://runescape.wiki/w/Telescope
     */
    private fun Player.observe(telescope: String) {
        val location = ShootingStarHandler.nextLocation
        if (location == null) {
            message("You look through the telescope but can't see any shooting stars.") // TODO proper message
            return
        }
        val window = when (telescope) {
            "wooden_telescope" -> 24
            "teak_telescope" -> 9
            else -> 2
        }
        val minutes = ((ShootingStarHandler.nextStarTick - GameLoop.tick) / TimeUnit.MINUTES.toTicks(1)).coerceAtLeast(0)
        val earliest = (minutes - random.nextInt(window + 1)).coerceAtLeast(0)
        message("You see a shooting star! It looks like it will land near ${location.description}") // TODO proper message
        message("It should land in $earliest to ${earliest + window} minutes.") // TODO proper message
        val tier = ShootingStarHandler.nextTier
        message(
            when (telescope) {
                "wooden_telescope" -> if (tier <= 3) {
                    "It looks quite small."
                } else if (tier <= 6) {
                    "It looks a reasonable size."
                } else {
                    "It looks very large." // TODO proper message
                }
                "teak_telescope" -> "It looks to be about size ${(tier - 1).coerceAtLeast(1)} to ${(tier + 1).coerceAtMost(9)}." // TODO proper message
                else -> "It looks to be size $tier." // TODO proper message
            },
        )
    }

    private enum class StaffType(val cost: Int, val id: (String) -> String) {
        Basic(0, { "staff_of_$it" }),
        Battle(100, { "${it}_battlestaff" }),
        Mystic(1000, { "mystic_${it}_staff" }),
    }

    companion object {
        private const val BOOK = "modern_spellbook"
        private val ELEMENTS = listOf("air", "water", "earth", "fire")
        private val STAVES = StaffType.entries.flatMap { type -> ELEMENTS.map { type.id(it) } }.joinToString(",")
        private const val LECTERNS = "oak_lectern,eagle_lectern,demon_lectern,teak_eagle_lectern,teak_demon_lectern,mahogany_eagle_lectern,mahogany_demon_lectern"
    }
}
