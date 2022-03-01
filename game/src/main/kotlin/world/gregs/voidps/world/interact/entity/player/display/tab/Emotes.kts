package world.gregs.voidps.world.interact.entity.player.display.tab

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.clearAnimation
import world.gregs.voidps.engine.entity.character.update.visual.player.direction
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentId
import world.gregs.voidps.engine.entity.definition.getComponentIntId
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.item.getMaxedSkill
import world.gregs.voidps.engine.entity.item.isSkillCape
import world.gregs.voidps.engine.entity.item.isTrimmedSkillCape
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.visual.EquipSlot
import world.gregs.voidps.world.interact.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.effect.transform
import world.gregs.voidps.world.interact.entity.gfx.areaGraphic
import world.gregs.voidps.world.interact.entity.sound.playJingle
import kotlin.random.Random

val definitions: InterfaceDefinitions by inject()

fun isUnlockableId(id: Int): Boolean = id in 26..52

on<InterfaceOpened>({ id == "emotes" }) { player: Player ->
    val definition = definitions.get(id)
    definition.components?.forEach { (intId, _) ->
        if (isUnlockableId(intId)) {
            val id = definition.getComponentId(intId)
            player.sendVar("unlocked_emote_$id")
        }
    }
    player.sendVar("unlocked_emote_lost_tribe")
}

on<InterfaceRefreshed>({ id == "emotes" }) { player: Player ->
    player.interfaceOptions.unlockAll("emotes", "emotes", 0..190)
}

on<InterfaceOption>({ id == "emotes" }) { player: Player ->
    val id = option.toSnakeCase()
    val definition = definitions.get(this.id)
    val componentId = definition.getComponentIntId(component)!!
    if (componentId > 23 && !unlocked(player, id, option)) {
        return@on
    }
    if (player.action.type == ActionType.Emote) {
        player.message("Please wait till you've finished performing your current emote.")
        return@on
    }
    player.action(ActionType.Emote) {
        withContext(NonCancellable) {
            when {
                id == "skillcape" -> {
                    val cape = player.equipped(EquipSlot.Cape)
                    val skill = cape.def.getMaxedSkill()
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
}

fun unlocked(player: Player, id: String, emote: String): Boolean {
    if (emote.startsWith("Goblin")) {
        if (player.getVar("unlocked_emote_lost_tribe", false)) {
            return true
        }
        player.dialogue {
            statement("This emote can be unlocked during the Lost Tribe quest.")
        }
        return false
    }
    if (!player.getVar("unlocked_emote_$id", false)) {
        when (emote) {
            "Glass Wall", "Glass Box", "Climb Rope", "Lean" -> player.dialogue {
                statement("This emote can be unlocked during the mine random event.")
            }
            "Zombie Dance", "Zombie Walk" -> player.dialogue {
                statement("This emote can be unlocked during the gravedigger random event.")
            }
            "Scared", "Trick", "Puppet master", "Zombie Hand" -> player.dialogue {
                statement("This emote can be unlocked by playing a Halloween seasonal quest.")
            }
            "Bunny Hop", "Around the World in Eggty Days" -> player.dialogue {
                statement("This emote can be unlocked by playing an Easter seasonal event.")
            }
            "Skillcape" -> player.message("You need to wearing a skillcape in order to perform that emote.")
            "Air Guitar" -> player.message("You need to have 500 music tracks unlocked to perform that emote.")
            "Safety First" -> player.dialogue {
                statement("""
                   You can't use this emote yet. Visit the Stronghold of Player Safety to
                   unlock it.
                """)
            }
            "Explore" -> player.dialogue {
                statement("""
                    You can't use this emote yet. You will need to complete the Beginner
                    Tasks in the Lumbridge and Draynor Achievement Diary to use it.
                """)
            }
            "Give Thanks" -> player.message("This emote can be unlocked by playing a Thanksgiving seasonal event.")
            "Snowman Dance", "Freeze", "Dramatic Point", "Seal of Approval" -> player.dialogue {
                statement("This emote can be unlocked by playing a Christmas seasonal event.")
            }
            "Flap", "Slap Head", "Idea", "Stomp" -> player.dialogue {
                statement("""
                    You can't use that emote yet. Visit the Stronghold of Security to
                    unlock it.
                """)
            }
            "Faint" -> player.dialogue {
                statement("This emote can be unlocked by completing the mime court case.")
            }
            "Taskmaster" -> player.dialogue {
                statement("Complete the Task Master achievement to unlock this emote.")
            }
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

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Cape.index }) { player: Player ->
    player.setVar("unlocked_emote_skillcape", item.def.isSkillCape() || item.def.isTrimmedSkillCape() || item.id == "quest_point_cape")
}

suspend fun Action.playEnhancedEmote(player: Player, type: String) {
    player.playAnimation("emote_enhanced_$type")
}

suspend fun Action.playEnhancedYawnEmote(player: Player) {
    player.setGraphic("emote_enhanced_yawn")
    player.playAnimation("emote_enhanced_yawn")
}

suspend fun Action.playGiveThanksEmote(player: Player) {
    player.setGraphic("emote_give_thanks")
    player.playAnimation("emote_turkey_transform")
    player.transform("turkey")
    player.playAnimation("emote_turkey_dance")
    player.setGraphic("emote_give_thanks")
    player.transform("")
    player.playAnimation("emote_turkey_return")
}

suspend fun Action.playSealOfApprovalEmote(player: Player) {
    player.setGraphic("emote_seal_of_approval")
    player.playAnimation("emote_seal_of_approval")
    player.transform("seal")
    player.playAnimation("emote_seal_clap")
    player.playAnimation("emote_seal_return")
    player.setGraphic("emote_seal_of_approval")
    player.transform("")
    player.playAnimation("emote_seal_stand")
}

suspend fun Action.playSkillCapeEmote(player: Player, skill: String) {
    player.setGraphic("emote_$skill")
    player.playAnimation("emote_$skill")
}

suspend fun Action.playDungeoneeringCapeEmote(player: Player) {
    player.setGraphic("emote_dungeoneering_start")
    player.playAnimation("emote_dungeoneering_start")
    when (Random.nextInt(3)) {
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

suspend fun Action.playDungeoneeringMasterCapeEmote(player: Player) {
    val direction = player.direction

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
    val definition = definitions.get("emotes")
    definition.components?.forEach { (intId, _) ->
        if (isUnlockableId(intId) && intId != 39) {
            val id = definition.getComponentId(intId)
            player.setVar("unlocked_emote_$id", true)
        }
    }
    player.setVar("unlocked_emote_lost_tribe", true)
}