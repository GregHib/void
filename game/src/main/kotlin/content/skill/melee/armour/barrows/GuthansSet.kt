package content.skill.melee.armour.barrows

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.ItemAdded
import world.gregs.voidps.engine.inv.ItemRemoved
import world.gregs.voidps.type.random

class GuthansSet : Script {

    init {
        playerSpawn {
            if (hasFullSet()) {
                set("guthans_set_effect", true)
            }
        }

        for (slot in BarrowsArmour.slots) {
            itemAdded("guthans_*", "worn_equipment", slot, ::added)
            itemRemoved("guthans_*", "worn_equipment", slot, ::removed)
        }

        combatAttack("melee", handler = ::attack)
        npcCombatAttack(style = "melee", handler = ::attack)
    }

    fun attack(source: Character, attack: world.gregs.voidps.engine.entity.character.mode.combat.CombatAttack) {
        if (source.contains("guthans_set_effect") && random.nextInt(4) == 0) {
            source.levels.boost(Skill.Constitution, attack.damage)
            attack.target.gfx("guthans_effect")
        }
    }

    fun added(player: Player, update: ItemAdded) {
        if (player.hasFullSet()) {
            player["guthans_set_effect"] = true
        }
    }

    fun removed(player: Player, update: ItemRemoved) {
        player.clear("guthans_set_effect")
    }

    fun Player.hasFullSet() = BarrowsArmour.hasSet(
        this,
        "guthans_warspear",
        "guthans_helm",
        "guthans_platebody",
        "guthans_chainskirt",
    )
}
