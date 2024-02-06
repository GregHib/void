package world.gregs.voidps.world.interact.entity.player.combat.armour.barrows

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatAttack
import world.gregs.voidps.world.interact.entity.player.energy.runEnergy

playerSpawn { player: Player ->
    if (player.hasFullSet()) {
        player["torags_set_effect"] = true
    }
}

itemRemoved("worn_equipment", "torags_*", BarrowsArmour.slots) { player: Player ->
    player.clear("torags_set_effect")
}

itemAdded("worn_equipment", "torags_*", BarrowsArmour.slots) { player: Player ->
    if (player.hasFullSet()) {
        player["torags_set_effect"] = true
    }
}

fun Player.hasFullSet() = BarrowsArmour.hasSet(this,
    "torags_hammers",
    "torags_helm",
    "torags_platebody",
    "torags_platelegs")

characterCombatAttack { character: Character ->
    if (type != "melee" || damage <= 0 || target !is Player || !weapon.id.startsWith("torags_hammers") || !character.contains("torags_set_effect") || random.nextInt(4) != 0) {
        return@characterCombatAttack
    }
    val target = target as Player
    if (target.runEnergy > 0) {
        target.runEnergy -= target.runEnergy / 5
        target.setGraphic("torags_effect")
    }
}