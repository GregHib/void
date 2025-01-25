package content.skill.melee.armour.barrows

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatAttack

playerSpawn { player ->
    if (player.hasFullSet()) {
        player["ahrims_set_effect"] = true
    }
}

itemRemoved("ahrims_*", BarrowsArmour.slots, "worn_equipment") { player ->
    player.clear("ahrims_set_effect")
}

itemAdded("ahrims_*", BarrowsArmour.slots, "worn_equipment") { player ->
    if (player.hasFullSet()) {
        player["ahrims_set_effect"] = true
    }
}

fun Player.hasFullSet() = BarrowsArmour.hasSet(
    this,
    "ahrims_staff",
    "ahrims_hood",
    "ahrims_robe_top",
    "ahrims_robe_skirt"
)

characterCombatAttack(type = "magic") { character ->
    if (damage <= 0) {
        return@characterCombatAttack
    }
    if (!character.contains("ahrims_set_effect") || random.nextInt(4) != 0) {
        return@characterCombatAttack
    }
    val drain = target.levels.drain(Skill.Strength, 5)
    if (drain < 0) {
        target.gfx("ahrims_effect")
    }
}