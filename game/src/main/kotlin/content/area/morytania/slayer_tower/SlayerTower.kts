package content.area.morytania.slayer_tower

import content.entity.combat.hit.hit
import content.entity.obj.door.enterDoor
import content.entity.obj.objTeleportLand
import content.entity.obj.objTeleportTakeOff
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.objectDespawn
import world.gregs.voidps.engine.entity.objectSpawn
import world.gregs.voidps.engine.inject

val objects: GameObjects by inject()

objectSpawn("slayer_tower_entrance_door_*_opened") { obj ->
    val statue = if (obj.id == "slayer_tower_entrance_door_west_opened") {
        objects[obj.tile.add(-2, -2), "slayer_tower_statue"]
    } else {
        objects[obj.tile.add(1, -2), "slayer_tower_statue"]
    } ?: return@objectSpawn
    statue.anim("slayer_tower_statue_stand")
}

objectDespawn("slayer_tower_entrance_door_*_opened") { obj ->
    val statue = if (obj.id == "slayer_tower_entrance_door_west_opened") {
        objects[obj.tile.add(-2, -2), "slayer_tower_statue"]
    } else {
        objects[obj.tile.add(1, -2), "slayer_tower_statue"]
    } ?: return@objectDespawn
    statue.anim("slayer_tower_statue_hide")
}

objTeleportTakeOff("Climb-*", "slayer_tower_chain*") {
    val requirement = if (target.tile.x == 3422 && target.tile.y == 3550) 61 else 71
    if (!player.has(Skill.Agility, requirement)) {
        player.message("You need an Agility level of $requirement to negotiate this obstacle.")
        cancel()
        return@objTeleportTakeOff
    }
    val success = Level.success(player.levels.get(Skill.Agility), 90) // Unknown success rate
    player["slayer_chain_success"] = success
    if (!success) {
        player.hit(player, damage = 20, offensiveType = "damage", weapon = Item.EMPTY)
        player.message("You rip your hands to pieces on the chain as you climb.", type = ChatType.Filter)
    }
}

objTeleportLand("Climb-*", "slayer_tower_chain*") {
    player.exp(Skill.Agility, if (player["slayer_chain_success", true]) 3.0 else 6.0)
}

objectOperate("Open", "slayer_tower_door*_closed") {
    enterDoor(target)
}