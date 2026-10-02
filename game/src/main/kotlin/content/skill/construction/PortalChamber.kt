package content.skill.construction

import content.entity.player.dialogue.type.choice
import content.skill.construction.House.Companion.furnishRoom
import content.skill.construction.House.Companion.houseBase
import content.skill.construction.House.Companion.houseFurniture
import content.skill.construction.House.Companion.inOwnHouse
import content.skill.construction.House.Companion.roomPosition
import content.skill.construction.House.Companion.roomZone
import content.skill.construction.HouseFurniture.Companion.pick
import content.skill.magic.spell.SpellRunes
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Zone

/**
 * Portal chamber portals are directed to a teleport destination by the centrepiece, costing a hundred casts worth of runes.
 * Each portal hotspot's variant stores the destination so the room is furnished with the directed portal.
 * https://oldschool.runescape.wiki/w/Portal_chamber
 */
class PortalChamber : Script {
    init {
        objectOperate("Enter", PORTALS) { (target) ->
            val spell = Tables.stringOrNull("house_portals.${target.id.substringAfter("_portal_")}.spell") ?: return@objectOperate
            Teleport.teleport(this, spell, "modern")
        }

        objectOperate("Direct-portal", FOCUSES) { (target) ->
            val base = houseBase() ?: return@objectOperate
            if (!inOwnHouse()) {
                message("You can only do that in your own house.") // TODO proper message
                return@objectOperate
            }
            val position = roomPosition(base, target.tile.zone) ?: return@objectOperate
            val portals = PORTAL_SPACES.filter { houseFurniture(position, it) != null }
            if (portals.isEmpty()) {
                message("You need to build a portal frame first.") // TODO proper message
                return@objectOperate
            }
            // TODO proper portal names
            val portal = if (portals.size == 1) 1 else choice(portals.indices.map { "Portal ${it + 1}" }, "Direct which portal?")
            if (portal == -1) {
                return@objectOperate
            }
            val destinations = Tables.get("house_portals").rows()
            val index = pick(destinations.map { it.string("name") })
            if (index == -1) {
                return@objectOperate
            }
            direct(base, position, portals[portal - 1], destinations[index], index + 1)
        }
    }

    /**
     * Directs the portal on [hotspot] in the room at [position] to [destination]
     * TODO quest requirements for Ardougne (Plague City), Yanille (Watchtower) and Kharyrll (Desert Treasure)
     */
    private fun Player.direct(base: Zone, position: Int, hotspot: String, destination: RowDefinition, direction: Int) {
        if (houseBase() != base || houseFurniture(position, hotspot) == null) {
            return
        }
        val variant = Tables.string("house_hotspots.$hotspot.variant")
        if (get(variant, 0) == direction) {
            message("That portal is already directed there.") // TODO proper message
            return
        }
        val book = destination.string("spellbook")
        val spell = destination.string("spell")
        val level = SpellRunes.magicLevel(book, spell) ?: return
        if (!has(Skill.Magic, level, message = true)) {
            return
        }
        val runes = SpellRunes.requiredItems(book, spell)?.map { (id, amount) -> Item(id, amount * RUNE_MULTIPLIER) } ?: return
        if (!inventory.remove(runes)) {
            message("You need ${runes.joinToString { "${it.amount} ${it.def.name.lowercase()}s" }} to direct the portal there.") // TODO proper message
            return
        }
        set(variant, direction)
        GameObjects.reset(roomZone(base, position))
        furnishRoom(base, position, get("house_build_mode", false))
        message("Your portal is now directed to ${destination.string("name")}.") // TODO proper message
    }

    companion object {
        private const val PORTALS = "teak_portal_*,mahogany_portal_*,marble_portal_*"
        private const val FOCUSES = "teleport_focus,greater_teleport_focus,scrying_pool"
        private const val RUNE_MULTIPLIER = 100
        private val PORTAL_SPACES = listOf("portal_chamber_portal_space", "portal_chamber_portal_space_2", "portal_chamber_portal_space_3")
    }
}
