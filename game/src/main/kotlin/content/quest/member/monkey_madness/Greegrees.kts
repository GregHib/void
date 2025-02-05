package content.quest.member.monkey_madness

import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.sound.playSound
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.move.exitArea
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

val items: FloorItems by inject()
val areas: AreaDefinitions by inject()

playerSpawn { player ->
    val item = player.equipped(EquipSlot.Weapon).id
    if (item.endsWith("_greegree")) {
        if (player.tile in areas["ape_atoll_multi_area"]) {
            player.transform(item.replace("_greegree", ""))
        } else {
            forceRemove(player)
        }
    }
}

itemAdded("*_greegree", EquipSlot.Weapon, "worn_equipment") { player ->
    player.gfx("monkey_transform")
    val sound = when {
        item.id.endsWith("gorilla_greegree") -> "human_into_zombie_monkey"
        item.id.endsWith("zombie_monkey_greegree") -> "human_into_gorilla"
        item.id.startsWith("small") -> "human_into_small_monkey"
        else -> "human_into_monkey"
    }
    player.playSound(sound)
    player.transform(item.id.replace("_greegree", ""))
}

itemRemoved("*_greegree", EquipSlot.Weapon, "worn_equipment") { player ->
    player.gfx("monkey_transform")
    player.clearTransform()
}

exitArea("ape_atoll_multi_area") {
    forceRemove(player)
}

fun forceRemove(player: Player) {
    if (player["logged_out", false]) {
        return // TODO check if removed on logout or not
    }
    val item = player.equipped(EquipSlot.Weapon).id
    if (item.endsWith("_greegree") && !player.equipment.move(EquipSlot.Weapon.index, player.inventory)) {
        if (player.equipment.remove(EquipSlot.Weapon.index, item)) {
            // FIXME issue with item spawning displaying twice if spawned on the same tick. #614
            World.queue("greegree_spawn", 1) {
                items.add(player.tile, item, disappearTicks = 300, owner = player)
            }
        }
    }
}