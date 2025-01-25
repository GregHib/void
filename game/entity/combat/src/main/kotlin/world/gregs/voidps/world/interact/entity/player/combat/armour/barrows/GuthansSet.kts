package world.gregs.voidps.world.interact.entity.player.combat.armour.barrows

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatAttack

playerSpawn { player ->
    if (player.hasFullSet()) {
        player["guthans_set_effect"] = true
    }
}

itemRemoved("guthans_*", BarrowsArmour.slots, "worn_equipment") { player ->
    player.clear("guthans_set_effect")
}

itemAdded("guthans_*", BarrowsArmour.slots, "worn_equipment") { player ->
    if (player.hasFullSet()) {
        player["guthans_set_effect"] = true
    }
}

fun Player.hasFullSet() = BarrowsArmour.hasSet(this,
    "guthans_warspear",
    "guthans_helm",
    "guthans_platebody",
    "guthans_chainskirt")

characterCombatAttack(type = "melee") { character ->
    if (character.contains("guthans_set_effect") && random.nextInt(4) == 0) {
        character.levels.boost(Skill.Constitution, damage)
        target.gfx("guthans_effect")
    }
}