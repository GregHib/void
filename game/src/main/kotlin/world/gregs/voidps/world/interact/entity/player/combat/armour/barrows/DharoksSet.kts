package world.gregs.voidps.world.interact.entity.player.combat.armour.barrows

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved

playerSpawn { player: Player ->
    if (player.hasFullSet()) {
        player["dharoks_set_effect"] = true
    }
}

itemRemoved("dharoks_*", BarrowsArmour.slots, "worn_equipment") { player: Player ->
    player.clear("dharoks_set_effect")
}

itemAdded("dharoks_*", BarrowsArmour.slots, "worn_equipment") { player: Player ->
    if (player.hasFullSet()) {
        player["dharoks_set_effect"] = true
    }
}

fun Player.hasFullSet() = BarrowsArmour.hasSet(this,
    "dharoks_greataxe",
    "dharoks_helm",
    "dharoks_platebody",
    "dharoks_platelegs")