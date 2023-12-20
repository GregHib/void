package world.gregs.voidps.world.interact.entity.player.display.tab

import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.facing
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.ItemChanged
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.suspend.playAnimation
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.effect.transform
import world.gregs.voidps.world.interact.entity.gfx.areaGraphic
import world.gregs.voidps.world.interact.entity.sound.playJingle

val definitions: InterfaceDefinitions by inject()

val unlockableRange = 26..52

on<InterfaceOpened>({ id == "emotes" }) { player: Player ->
    for (compId in unlockableRange) {
        val component = definitions.getComponent(id, compId) ?: continue
        player.sendVariable("unlocked_emote_${component.stringId}")
    }
    player.sendVariable("unlocked_emote_lost_tribe")
}

on<InterfaceRefreshed>({ id == "emotes" }) { player: Player ->
    player.interfaceOptions.unlockAll("emotes", "emotes", 0..190)
}

on<InterfaceOption>({ id == "emotes" }) { player: Player ->
    val id = option.toSnakeCase()
    val componentId = definitions.getComponentId(this.id, component)!!
    if (componentId > 23 && !unlocked(id, option)) {
        return@on
    }
    player.strongQueue("emote") {
        println(id)
        when {
            id == "skillcape" -> {
                val cape = player.equipped(EquipSlot.Cape)
                val skill: Skill? = cape.def.getOrNull("skillcape_skill")
                when {
                    cape.id == "quest_point_cape" -> playSkillCapeEmote(player, "quest_point")
                    cape.id == "dungeoneering_master_cape" -> playDungeoneeringMasterCapeEmote(player)
                    skill == Skill.Dungeoneering -> playDungeoneeringCapeEmote(player)
                    skill != null -> playSkillCapeEmote(player, skill.name.lowercase())
                }
            }
            id == "seal_of_approval" -> playSealOfApprovalEmote(player)
            id == "give_thanks" -> playGiveThanksEmote(player)
            id == "angry" && player.equipped(EquipSlot.Hat).id == "a_powdered_wig" -> playEnhancedEmote(player, id)
            id == "yawn" && player.equipped(EquipSlot.Hat).id == "sleeping_cap" -> playEnhancedYawnEmote(player)
            id == "bow" && player.equipped(EquipSlot.Legs).id == "pantaloons" -> playEnhancedEmote(player, id)
            id == "dance" && player.equipped(EquipSlot.Legs).id == "flared_trousers" -> playEnhancedEmote(player, id)
            else -> {
                if (id == "air_guitar") {
                    player.playJingle(id)
                }
                player.setGraphic("emote_$id")
                player.playAnimation("emote_$id")
            }
        }
        player.clearAnimation()
    }
}

suspend fun CharacterContext.unlocked(id: String, emote: String): Boolean {
    if (emote.startsWith("Goblin")) {
        if (player["unlocked_emote_lost_tribe", false]) {
            return true
        }
        statement("This emote can be unlocked during the Lost Tribe quest.")
        return false
    }
    if (!player["unlocked_emote_$id", false]) {
        when (emote) {
            "Glass Wall", "Glass Box", "Climb Rope", "Lean" -> statement("This emote can be unlocked during the mine random event.")
            "Zombie Dance", "Zombie Walk" -> statement("This emote can be unlocked during the gravedigger random event.")
            "Scared", "Trick", "Puppet master", "Zombie Hand" -> statement("This emote can be unlocked by playing a Halloween seasonal quest.")
            "Bunny Hop", "Around the World in Eggty Days" -> statement("This emote can be unlocked by playing an Easter seasonal event.")
            "Skillcape" -> player.message("You need to be wearing a skillcape in order to perform that emote.")
            "Air Guitar" -> player.message("You need to have 500 music tracks unlocked to perform that emote.")
            "Safety First" -> {
                statement("""
                   You can't use this emote yet. Visit the Stronghold of Player Safety to
                   unlock it.
                """)
            }
            "Explore" -> {
                statement("""
                    You can't use this emote yet. You will need to complete the Beginner
                    Tasks in the Lumbridge and Draynor Achievement Diary to use it.
                """)
            }
            "Give Thanks" -> player.message("This emote can be unlocked by playing a Thanksgiving seasonal event.")
            "Snowman Dance", "Freeze", "Dramatic Point", "Seal of Approval" -> statement("This emote can be unlocked by playing a Christmas seasonal event.")
            "Flap", "Slap Head", "Idea", "Stomp" -> {
                statement("""
                    You can't use that emote yet. Visit the Stronghold of Security to
                    unlock it.
                """)
            }
            "Faint" -> statement("This emote can be unlocked by completing the mime court case.")
            "Taskmaster" -> statement("Complete the Task Master achievement to unlock this emote.")
        }
        return false
    }
    if (emote == "Taskmaster" && !areaClear(player)) {
        return false
    }
    if (emote == "Skillcape" && player.equipped(EquipSlot.Cape).id == "dungeoneering_master_cape" && !areaClear(player)) {
        return false
    }
    return true
}

fun areaClear(player: Player): Boolean {
    Direction.all.forEach {
        if (player.blocked(it)) {
            player.message("You need a clear area to perform this emote.")
            return false
        }
    }
    return true
}

on<ItemChanged>({ inventory == "worn_equipment" && index == EquipSlot.Cape.index }) { player: Player ->
    player["unlocked_emote_skillcape"] = item.def.contains("skill_cape") || item.def.contains("skill_cape_t") || item.id == "quest_point_cape"
}

suspend fun CharacterContext.playEnhancedEmote(player: Player, type: String) {
    player.playAnimation("emote_enhanced_$type")
}

suspend fun CharacterContext.playEnhancedYawnEmote(player: Player) {
    player.setGraphic("emote_enhanced_yawn")
    player.playAnimation("emote_enhanced_yawn")
}

suspend fun CharacterContext.playGiveThanksEmote(player: Player) {
    player.setGraphic("emote_give_thanks")
    player.playAnimation("emote_turkey_transform")
    player.transform("turkey")
    player.playAnimation("emote_turkey_dance")
    player.setGraphic("emote_give_thanks")
    player.transform("")
    player.playAnimation("emote_turkey_return")
}

suspend fun CharacterContext.playSealOfApprovalEmote(player: Player) {
    player.setGraphic("emote_seal_of_approval")
    player.playAnimation("emote_seal_of_approval")
    player.transform("seal")
    player.playAnimation("emote_seal_clap")
    player.playAnimation("emote_seal_return")
    player.setGraphic("emote_seal_of_approval")
    player.transform("")
    player.playAnimation("emote_seal_stand")
}

suspend fun CharacterContext.playSkillCapeEmote(player: Player, skill: String) {
    player.setGraphic("emote_$skill")
    player.playAnimation("emote_$skill")
}

suspend fun CharacterContext.playDungeoneeringCapeEmote(player: Player) {
    player.setGraphic("emote_dungeoneering_start")
    player.playAnimation("emote_dungeoneering_start")
    when (random.nextInt(3)) {
        0 -> {
            player.transform("primal_warrior")
            player.playAnimation("emote_dungeoneering_melee")
        }
        1 -> {
            player.transform("celestial_mage")
            player.playAnimation("emote_dungeoneering_mage")
        }
        2 -> {
            player.transform("sagittarian_ranger")
            player.playAnimation("emote_dungeoneering_range")
        }
    }
    player.transform("")
}

suspend fun CharacterContext.playDungeoneeringMasterCapeEmote(player: Player) {
    val direction = player.facing

    player.transform("sagittarian_ranger")
    player.setGraphic("emote_dung_master_bow")
    var tile = player.tile.add(direction.rotate(1))
    var rotation = tile.delta(player.tile).toDirection().rotate(2)
    areaGraphic("emote_dung_master_hobgoblin", tile, rotation = rotation)
    player.playAnimation("emote_dung_master_bow")

    player.transform("celestial_mage")
    player.setGraphic("emote_dung_master_spell")
    tile = player.tile.add(direction.rotate(7))
    rotation = tile.delta(player.tile).toDirection().rotate(4)
    areaGraphic("emote_dung_master_gravecreeper", tile, rotation = rotation)
    player.playAnimation("emote_dung_master_spell")

    player.transform("primal_warrior")
    player.setGraphic("emote_dung_master_return")
    tile = player.tile.add(direction)
    rotation = direction.inverse().rotate(7)
    areaGraphic("emote_dung_master_flesh_spoiler", tile, rotation = rotation)
    tile = player.tile.add(direction.inverse())
    rotation = direction.rotate(3)
    areaGraphic("emote_dung_master_cursebearer", tile, rotation = rotation)
    player.playAnimation("emote_dung_master_sword")

    player.transform("")
}

on<Command>({ prefix == "emotes" }) { player: Player ->
    for (compId in unlockableRange) {
        if (compId == 39) {
            continue
        }
        val component = definitions.getComponent("emotes", compId) ?: continue
        player["unlocked_emote_${component.stringId}"] = true
    }
    player["unlocked_emote_lost_tribe"] = true
}