package world.gregs.voidps.world.interact.entity.player.combat.armour.barrows

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved

playerSpawn { player: Player ->
    if (player.hasFullSet()) {
        player["veracs_set_effect"] = true
    }
}

itemRemoved("worn_equipment", "veracs_*", BarrowsArmour.slots) { player: Player ->
    player.clear("veracs_set_effect")
}

itemAdded("worn_equipment", "veracs_*", BarrowsArmour.slots) { player: Player ->
    if (player.hasFullSet()) {
        player["veracs_set_effect"] = true
    }
}

fun Player.hasFullSet() = BarrowsArmour.hasSet(this,
    "veracs_flail",
    "veracs_helm",
    "veracs_brassard",
    "veracs_plateskirt")