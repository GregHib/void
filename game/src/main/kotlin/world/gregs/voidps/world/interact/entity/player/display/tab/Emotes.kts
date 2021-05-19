package world.gregs.voidps.world.interact.entity.player.display.tab

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.clearAnimation
import world.gregs.voidps.engine.entity.character.update.visual.player.direction
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.instruct.Command
import world.gregs.voidps.world.interact.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.effect.transform
import world.gregs.voidps.world.interact.entity.gfx.areaGraphic
import world.gregs.voidps.world.interact.entity.player.equip.getMaxedSkill
import world.gregs.voidps.world.interact.entity.player.equip.isSkillCape
import world.gregs.voidps.world.interact.entity.player.equip.isTrimmedSkillCape
import kotlin.random.Random

BooleanVariable(2309, Variable.Type.VARBIT, true).register("unlocked_emote_flap")
BooleanVariable(2310, Variable.Type.VARBIT, true).register("unlocked_emote_slap_head")
BooleanVariable(2311, Variable.Type.VARBIT, true).register("unlocked_emote_idea")
BooleanVariable(2312, Variable.Type.VARBIT, true).register("unlocked_emote_stomp")
IntBooleanVariable(532, Variable.Type.VARBIT, trueIntValue = 7, persistent = true).register("unlocked_emote_lost_tribe")
BooleanVariable(1367, Variable.Type.VARBIT, true).register("unlocked_emote_glass_wall")
BooleanVariable(1368, Variable.Type.VARBIT, true).register("unlocked_emote_glass_box")
BooleanVariable(1369, Variable.Type.VARBIT, true).register("unlocked_emote_climb_rope")
BooleanVariable(1370, Variable.Type.VARBIT, true).register("unlocked_emote_lean")
BooleanVariable(1371, Variable.Type.VARBIT, true).register("unlocked_emote_scared")
BooleanVariable(1920, Variable.Type.VARBIT, true).register("unlocked_emote_zombie_dance")
BooleanVariable(1921, Variable.Type.VARBIT, true).register("unlocked_emote_zombie_walk")
BooleanVariable(2055, Variable.Type.VARBIT, true).register("unlocked_emote_bunny_hop")
BooleanVariable(2787, Variable.Type.VARBIT, true).register("unlocked_emote_skillcape")
IntBooleanVariable(4075, Variable.Type.VARBIT, trueIntValue = 12, persistent = true).register("unlocked_emote_zombie_hand")
BooleanVariable(4202, Variable.Type.VARBIT, true).register("unlocked_emote_snowman_dance")
BooleanVariable(4394, Variable.Type.VARBIT, true).register("unlocked_emote_air_guitar")
BooleanVariable(4476, Variable.Type.VARBIT, true).register("unlocked_emote_safety_first")
BooleanVariable(4884, Variable.Type.VARBIT, true).register("unlocked_emote_explore")
BooleanVariable(5490, Variable.Type.VARBIT, true).register("unlocked_emote_trick")
BooleanVariable(5732, Variable.Type.VARBIT, true).register("unlocked_emote_freeze")
BooleanVariable(5641, Variable.Type.VARBIT, true).register("unlocked_emote_give_thanks")
IntBooleanVariable(6014, Variable.Type.VARBIT, trueIntValue = 85, persistent = true).register("unlocked_emote_around_the_world_in_eggty_days")
BooleanVariable(6936, Variable.Type.VARBIT, true).register("unlocked_emote_dramatic_point")
BooleanVariable(6095, Variable.Type.VARBIT, true).register("unlocked_emote_faint")
IntBooleanVariable(8300, Variable.Type.VARBIT, trueIntValue = 20, persistent = true).register("unlocked_emote_puppet_master")
BooleanVariable(8688, Variable.Type.VARBIT, true).register("unlocked_emote_seal_of_approval")
IntBooleanVariable(8601, Variable.Type.VARBIT, trueIntValue = 417, persistent = true).register("unlocked_emote_taskmaster")


val all = setOf("flap",
    "slap_head",
    "idea",
    "stomp",
    "lost_tribe",
    "glass_wall",
    "glass_box",
    "climb_rope",
    "lean",
    "scared",
    "zombie_dance",
    "zombie_walk",
    "bunny_hop",
    "skillcape",
    "zombie_hand",
    "snowman_dance",
    "air_guitar",
    "safety_first",
    "explore",
    "trick",
    "freeze",
    "give_thanks",
    "around_the_world_in_eggty_days",
    "dramatic_point",
    "faint",
    "puppet_master",
    "seal_of_approval",
    "taskmaster"
)

on<InterfaceOpened>({ name == "emotes" }) { player: Player ->
    player.interfaceOptions.unlockAll("emotes", "emotes", 0..190)
    for (emote in all) {
        player.sendVar("unlocked_emote_$emote")
    }
}

on<InterfaceOption>({ name == "emotes" }) { player: Player ->
    val id = option.replace(" ", "_").toLowerCase()
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
                        cape.name == "quest_point_cape" -> playSkillCapeEmote(player, "quest_point")
                        cape.name == "dungeoneering_master_cape" -> playDungeoneeringMasterCapeEmote(player)
                        skill == Skill.Dungeoneering -> playDungeoneeringCapeEmote(player)
                        skill != null -> playSkillCapeEmote(player, skill.name.toLowerCase())
                    }
                }
                id == "seal_of_approval" -> playSealOfApprovalEmote(player)
                id == "give_thanks" -> playGiveThanksEmote(player)
                id == "angry" && player.equipped(EquipSlot.Hat).name == "a_powdered_wig" -> playEnhancedEmote(player, id)
                id == "yawn" && player.equipped(EquipSlot.Hat).name == "sleeping_cap" -> playEnhancedYawnEmote(player)
                id == "bow" && player.equipped(EquipSlot.Legs).name == "pantaloons" -> playEnhancedEmote(player, id)
                id == "dance" && player.equipped(EquipSlot.Legs).name == "flared_trousers" -> playEnhancedEmote(player, id)
                else -> {
                    player.setGraphic("emote_$id")
                    player.playAnimation("emote_$id", walk = false, run = false)
                }
            }
            delay(1)
            player.clearAnimation()
        }
    }
}

fun unlocked(player: Player, id: String, emote: String): Boolean {
    if (emote.startsWith("Goblin") && !player.getVar("unlocked_emote_lost_tribe", false)) {
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
            "Taskmaster" -> {
                player.dialogue {
                    statement("Complete the Task Master achievement to unlock this emote.")
                }
            }
        }
        return false
    }
    if (emote == "Taskmaster" && !areaClear(player)) {
        return false
    }
    if (emote == "Skillcape" && player.equipped(EquipSlot.Cape).name == "dungeoneering_master_cape" && !areaClear(player)) {
        return false
    }
    return true
}

fun areaClear(player: Player): Boolean {
    Direction.all.forEach {
        if (player.movement.traversal.blocked(player.tile, it)) {
            player.message("You need a clear area to perform this emote.")
            return false
        }
    }
    return true
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Cape.index }) { player: Player ->
    player.setVar("unlocked_emote_skillcape", item.def.isSkillCape() || item.def.isTrimmedSkillCape() || item.name == "quest_point_cape")
}

suspend fun Action.playEnhancedEmote(player: Player, type: String) {
    player.playAnimation("emote_enhanced_$type")
}

suspend fun Action.playEnhancedYawnEmote(player: Player) {
    player.setAnimation("emote_enhanced_yawn")
    delay(6)
    player.setGraphic("emote_enhanced_yawn")
    delay(4)
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
    player.setGraphic("emote_dung_master_bow", height = 100)
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
    player.setGraphic("emote_dung_master_return", delay = 60)
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
    for (emote in all) {
        player.setVar("unlocked_emote_$emote", true)
    }
}