package world.gregs.voidps.world.interact.entity.player.display.tab

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.clearAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.area
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.instruct.Command
import world.gregs.voidps.world.interact.dialogue.type.statement

BooleanVariable(2309, Variable.Type.VARBIT, true).register("unlocked_emote_flap")
BooleanVariable(2310, Variable.Type.VARBIT, true).register("unlocked_emote_slap_head")
BooleanVariable(2311, Variable.Type.VARBIT, true).register("unlocked_emote_idea")
BooleanVariable(2312, Variable.Type.VARBIT, true).register("unlocked_emote_stomp")
IntBooleanVariable(532, Variable.Type.VARBIT, trueIntValue = 7, persistent = true).register("unlocked_emotes_lost_tribe")
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


on<InterfaceOpened>({ name == "emotes" }) { player: Player ->
    player.interfaceOptions.unlockAll("emotes", "emotes", 0..190)
}

on<InterfaceOption>({ name == "emotes" }) { player: Player ->
    val id = option.replace(" ", "_").toLowerCase()
    if (componentId > 23 && !unlocked(player, id, option)) {
        return@on
    }
    player.setVar("emote_$id", true)
    player.action(ActionType.Emote) {
        withContext(NonCancellable) {
            player.setGraphic("emote_$id")
            player.playAnimation("emote_$id")
            delay(1)
            player.clearAnimation()
        }
    }
}

fun unlocked(player: Player, id: String, emote: String): Boolean {
    if (emote.startsWith("Goblin") && !player.getVar("unlocked_emotes_lost_tribe", false)) {
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
            "Air Guitar" -> player.message("You need to have all music tracks unlocked to perform that emote.")
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
    if (emote == "Taskmaster" && !areaClear(player, 1)) {
        return false
    }
    return true
}

fun areaClear(player: Player, radius: Int): Boolean {
    for (tile in player.tile.area(radius)) {
        if (player.movement.traversal.blocked(tile, Direction.NONE)) {
            player.message("You need a clear area to perform this emote.")
            return false
        }
    }
    return true
}

val all = setOf("flap",
    "slap_head",
    "idea",
    "stomp",
    "_lost_tribe",
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
    "taskmaster")

on<Command>({ prefix == "emotes" }) { player: Player ->
    for (emote in all) {
        player.setVar("unlocked_emote_$emote", true)
    }
}