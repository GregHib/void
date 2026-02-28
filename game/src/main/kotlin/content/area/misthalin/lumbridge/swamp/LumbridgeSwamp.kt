package content.area.misthalin.lumbridge.swamp

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.dialogue.type.warning
import content.quest.messageScroll
import content.quest.quest
import content.skill.firemaking.Light
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

class LumbridgeSwamp : Script {

    init {
        objectOperate("Search", "rocks_skull_restless_ghost_quest") {
            if (quest("the_restless_ghost") != "mining_spot" && quest("the_restless_ghost") != "found_skull") {
                message("There's nothing there of any use to you.")
                return@objectOperate
            }
            if (inventory.isFull()) {
                message("You can see the skull under the rocks, but you don't have enough space to carry it.")
                return@objectOperate
            }
            statement("You take the skull from the pile of rocks.")
            inventory.add("muddy_skull")
            set("rocks_restless_ghost", "no_skull")
            set("the_restless_ghost", "found_skull")
            val index: Int? = remove("restless_ghost_warlock")
            if (index != null) {
                val skeleton = NPCs.indexed(index)
                if (skeleton != null) {
                    NPCs.remove(skeleton)
                }
            }
            message("A skeleton warlock has appeared.")
            val warlock = NPCs.add("skeleton_warlock", Tile(3236, 3149), Direction.SOUTH)
            set("restless_ghost_warlock", warlock.index)
            warlock.anim("restless_ghost_warlock_spawn")
            val player = this
            warlock.softQueue("delayed_attack", 4) {
                warlock.interactPlayer(player, "Attack")
            }
            World.queue("skeleton_warlock", TimeUnit.SECONDS.toTicks(60)) {
                NPCs.remove(warlock)
                clear("restless_ghost_warlock")
            }
        }

        objectOperate("Search", "rocks_no_skull_restless_ghost_quest") {
            if (quest("the_restless_ghost") == "completed") {
                message("There's nothing of any interest.")
            } else {
                message("You already have the ghost's skull.")
            }
        }

        playerDeath {
            if (!ownsItem("muddy_skull")) {
                set("rocks_restless_ghost", "skull")
            }
        }

        destroyed("muddy_skull") {
            set("rocks_restless_ghost", "skull")
        }

        objectOperate("Read", "lumbridge_swamp_cave_sign") {
            messageScroll(
                listOf(
                    "",
                    "",
                    "",
                    "",
                    "<maroon>~-~-~ WARNING ~-~-~",
                    "",
                    "<maroon>Noxious gases vent into this cave.",
                    "<maroon>Naked flames may cause an explosion!",
                    "",
                    "<maroon>Beware of vicious head-grabbing beasts!",
                    "<maroon>Contact a Slayer Master for protective headgear.",
                    "",
                    "",
                    "",
                    "",
                ),
            )
        }

        objectOperate("Climb-down", "goblin_cave_entrance") {
            if (get("cave_goblin_rope", false)) {
                val light = Light.hasLightSource(this)
                if (!light && !warning("lumbridge_cellar")) {
                    message("You should find a light source and a tinderbox before going down there.")
                    return@objectOperate
                }
                if (!inventory.contains("tinderbox") && !warning("lumbridge_swamp_cave_rope")) {
                    message("You should find a tinderbox before going down there.")
                    return@objectOperate
                }
                anim("climb_down")
                delay(2)
                if (light) {
                    open("level_one_darkness")
                } else {
                    open("level_three_darkness")
                }
                tele(3167, 9573)
            } else if (inventory.contains("rope")) {
                choice("Attach a rope to the top of the hole?") {
                    option("Yes.") {
                        if (!inventory.remove("rope")) {
                            return@option
                        }
                        anim("climb_down")
                        set("cave_goblin_rope", true)
                        sound("attach_rope")
                        item("rope", 400, "You tie the rope to the top of the hole and throw it down.")
                    }
                    option("No.")
                }
            } else {
                statement("There is a sheer drop below the hole. You will need a rope.")
            }
        }

        objTeleportLand("Climb", "swamp_cave_climbing_rope") { _, _ ->
            close("level_one_darkness")
            close("level_three_darkness")
        }
    }
}
