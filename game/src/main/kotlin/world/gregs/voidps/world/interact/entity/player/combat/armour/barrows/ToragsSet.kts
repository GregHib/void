package world.gregs.voidps.world.interact.entity.player.combat.armour.barrows

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatAttack
import content.entity.player.energy.runEnergy

playerSpawn { player ->
    if (player.hasFullSet()) {
        player["torags_set_effect"] = true
    }
}

itemRemoved("torags_*", BarrowsArmour.slots, "worn_equipment") { player ->
    player.clear("torags_set_effect")
}

itemAdded("torags_*", BarrowsArmour.slots, "worn_equipment") { player ->
    if (player.hasFullSet()) {
        player["torags_set_effect"] = true
    }
}

fun Player.hasFullSet() = BarrowsArmour.hasSet(this,
    "torags_hammers",
    "torags_helm",
    "torags_platebody",
    "torags_platelegs")

characterCombatAttack("torags_hammers*", "melee") { character ->
    if (damage <= 0 || target !is Player || !character.contains("torags_set_effect") || random.nextInt(4) != 0) {
        return@characterCombatAttack
    }
    if (target.runEnergy > 0) {
        target.runEnergy -= target.runEnergy / 5
        target.gfx("torags_effect")
    }
}