package content.skill.melee.armour.barrows

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.ItemAdded
import world.gregs.voidps.engine.inv.ItemRemoved
import world.gregs.voidps.type.random

class KarilsSet : Script {

    init {
        playerSpawn {
            if (hasFullSet()) {
                set("karils_set_effect", true)
            }
        }

        for (slot in BarrowsArmour.slots) {
            itemAdded("karils_*", "worn_equipment", slot, ::added)
            itemRemoved("karils_*", "worn_equipment", slot, ::removed)
        }

        combatAttack("range", handler = ::attack)
        npcCombatAttack(style = "range", handler = ::attack)
    }

    fun attack(source: Character, attack: world.gregs.voidps.engine.entity.character.mode.combat.CombatAttack) {
        val (target, damage, _, weapon) = attack
        if (damage <= 0 || !weapon.id.startsWith("karils_crossbow") ||target !is Player || !source.contains("karils_set_effect") || random.nextInt(4) != 0) {
            return
        }
        if (target.levels.drain(Skill.Agility, multiplier = 0.20) < 0) {
            target.gfx("karils_effect")
        }
    }

    fun added(player: Player, update: ItemAdded) {
        if (player.hasFullSet()) {
            player["karils_set_effect"] = true
        }
    }

    fun removed(player: Player, update: ItemRemoved) {
        player.clear("karils_set_effect")
    }

    fun Player.hasFullSet() = BarrowsArmour.hasSet(
        this,
        "karils_crossbow",
        "karils_coif",
        "karils_top",
        "karils_skirt",
    )
}
