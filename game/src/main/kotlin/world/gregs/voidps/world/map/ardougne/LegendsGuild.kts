package world.gregs.voidps.world.map.ardougne

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.world.interact.dialogue.type.item

objectOperate("Look", "legends_guild_totem_pole") {
    // TODO proper message
    if (player.inventory.contains("combat_bracelet") && player.inventory.replace("combat_bracelet", "combat_bracelet_4")) {
        combatBracelet(player)
    } else if (player.inventory.contains("skills_necklace") && player.inventory.replace("skills_necklace", "skills_necklace_4")) {
        skillsNecklace(player)
    } else {
        player.message("You don't have any jewellery that the totem can recharge.")
    }
}

itemOnObjectOperate("combat_bracelet", "legends_guild_totem_pole") {
    if (player.inventory.replace(itemSlot, item.id, "combat_bracelet_4")) {
        combatBracelet(player)
    }
}

itemOnObjectOperate("skills_necklace", "legends_guild_totem_pole") {
    if (player.inventory.replace(itemSlot, item.id, "skills_necklace_4")) {
        skillsNecklace(player)
    }
}

suspend fun CharacterContext.combatBracelet(player: Player) {
    player.message("You touch the jewellery against the totem pole...")
    player.setAnimation("bend_down")
    item("combat_bracelet", 300, "You feel a power emanating from the totem pole as it recharges your bracelet. You can now rub the bracelet to teleport and wear it to get information while on a Slayer assignment.")
}

suspend fun CharacterContext.skillsNecklace(player: Player) {
    player.message("You touch the jewellery against the totem pole...")
    player.setAnimation("bend_down")
    // TODO proper message
    item("skills_necklace", 200, "You feel a power emanating from the totem pole as it recharges your necklace. You can now rub the necklace to teleport to many skilling guilds.")
}