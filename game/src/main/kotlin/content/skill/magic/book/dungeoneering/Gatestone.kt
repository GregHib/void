package content.skill.magic.book.dungeoneering

import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonMembers
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.inDungeoneering
import content.entity.player.inv.item.addOrDrop
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.carriesItem

class Gatestone : Script {
    init {
        interfaceOption("Cast", "dungeoneering_spellbook:create_gatestone") {
            if (hasClock("action_delay")) {
                return@interfaceOption
            }
            if (!inDungeoneering) {
                return@interfaceOption
            }
            if (!has(Skill.Magic, 32, message = true)) {
                return@interfaceOption
            }
            if (carriesItem("gatestone")) {
                message("You already have a gatestone in your pack. Making another would be pointless.")
                return@interfaceOption
            }
            if (contains("gatestone_tile")) {
                message("You already have an active gatestone.")
                return@interfaceOption
            }
            if (!removeSpellItems("create_gatestone")) {
                return@interfaceOption
            }
            start("action_delay", 2)
            anim("high_alch")
            gfx("high_alch")
            addOrDrop("gatestone")
        }

        dropped("gatestone") {
            set("gatestone_tile", tile)
        }

        taken("gatestone") {
            clear("gatestone_tile")
        }

        dropped("group_gatestone") {
            for (member in dungeonMembers) {
                member["group_gatestone_tile"] = tile
            }
        }

        taken("group_gatestone") {
            for (member in dungeonMembers) {
                member.clear("group_gatestone_tile")
                member["group_gatestone_player"] = index
            }
        }

        teleportLand("dungeoneering") {
            val gatestone = FloorItems.firstOrNull(tile) { it.id == "gatestone" && it.owner == name } ?: return@teleportLand
            clear("gatestone_tile")
            FloorItems.remove(gatestone)
        }
    }
}
