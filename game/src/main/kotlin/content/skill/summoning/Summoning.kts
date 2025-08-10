package content.skill.summoning

import content.entity.player.inv.inventoryItem
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

val enums: EnumDefinitions by inject()
val npcs: NPCs by inject()
val npcDefinitions: NPCDefinitions by inject()

inventoryItem("Summon", "*_pouch") {
    val familiarLevel = enums.get("summoning_pouch_levels").getInt(item.def.id)
    val familiarId = enums.get("summoning_familiar_ids").getInt(item.def.id)
    val summoningXp = item.def["summon_experience", 0.0]
    val familiar = npcDefinitions.get(familiarId)

    if (player.levels.get(Skill.Summoning) <= familiarLevel) {
        //TODO: Get actual message
        player.message("You don't have the level needed to summon that familiar...")
    }

    val familiarNpc = summonFamiliar(player, familiar) ?: return@inventoryItem
    updateFamiliarInterface(player, familiarNpc, item)
    player.inventory.remove(item.id)
    player.experience.add(Skill.Summoning, summoningXp)
}

fun summonFamiliar(player: Player, familiar: NPCDefinition): NPC? {
    // TODO: Return null if there's not enough space around the player to spawn the familiar
    val familiarNpc = npcs.add(familiar.stringId, player.tile)
    familiarNpc.mode = Follow(familiarNpc, player)

    return familiarNpc
}

fun updateFamiliarInterface(player: Player, familiar: NPC, pouch: Item) {
    player.interfaces.open("pet_details")

    player["pet_details_pet_name"] = pouch.def.id
    player["pet_details_chathead"] = familiar.def.id

    player["pet_details_chathead_animation"] = 1
}