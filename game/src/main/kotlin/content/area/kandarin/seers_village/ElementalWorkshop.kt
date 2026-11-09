package content.area.kandarin.seers_village

import content.entity.effect.transform
import content.entity.obj.door.enterDoor
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import content.skill.mining.Pickaxe
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Tile

class ElementalWorkshop : Script {

    init {
        objectOperate("Search", "elemental_workshop_bookcase") {
            if (ownsItem("battered_book") || ownsItem("slashed_book")) {
                item("battered_book", "There is a book here titled 'The Elemental Shield'. It can stay here, as you have a copy in your backpack.")
                return@objectOperate
            }
            if (inventory.add("battered_book")) {
                item("battered_book", "You find a book titled 'The Elemental Shield'.")
            }
        }

        itemOnItem("knife", "battered_book") { _, _ ->
            message("You make a small cut in the spine of the book.")
            inventory.transaction {
                remove("battered_book")
                add("slashed_book")
                add("battered_key")
            }
            if (inventory.transaction.error == TransactionError.None) {
                message("Inside you find a small, old, battered key.")
            }
        }

        objectOperate("Open", "elemental_workshop_odd_wall*") { (target) ->
            if (tile.y >= target.tile.y) {
                if (!inventory.contains("battered_key") && !questCompleted("elemental_workshop_1")) {
                    message("You see a small hole in the wall but no way to open it.")
                    return@objectOperate
                }
                if (inventory.contains("battered_key")) {
                    message("You use the battered key to open the doors.")
                }
            }
            enterDoor(target)
        }

        objectOperate("Search", "elemental_workshop_boxes") {
            if (inventory.contains("a_stone_bowl") || inventory.contains("a_stone_bowl_full")) {
                message("It's empty.")
                return@objectOperate
            }
            if (inventory.add("a_stone_bowl")) {
                message("You find a stone bowl.")
            }
        }

        objectOperate("Search", "crate_27") { (target) ->
            if (target.tile != Tile(2713, 9898)) {
                return@objectOperate
            }
            crate("elemental_workshop_needle", "needle", "You find a needle.")
        }

        objectOperate("Search", "crate_23") { (target) ->
            if (target.tile != Tile(2717, 9894)) {
                return@objectOperate
            }
            crate("elemental_workshop_leather", "leather", "You find some leather.")
        }

        itemOnObjectOperate("a_stone_bowl", "elemental_workshop_lava_trough*") {
            sound("fill_stone_bowl")
            anim("fill_stone_bowl")
            delay(2)
            if (inventory.replace("a_stone_bowl", "a_stone_bowl_full")) {
                message("You fill the bowl with hot lava.")
            }
        }

        itemOnObjectOperate("a_stone_bowl_full", "elemental_workshop_lava_trough*") {
            message("The bowl is already full of lava.")
        }

        itemOnObjectOperate("a_stone_bowl_full", "elemental_workshop_furnace*") {
            if (!inventory.replace("a_stone_bowl_full", "a_stone_bowl")) {
                return@itemOnObjectOperate
            }
            sound("fill_furnace_with_lava")
            if (get("elemental_workshop_furnace", false)) {
                message("The lava makes little difference to the furnace's searing heat.")
                return@itemOnObjectOperate
            }
            message("You empty the lava into the furnace.")
            delay(2)
            set("elemental_workshop_furnace", true)
            message("The furnace bursts to life.")
        }

        itemOnObjectOperate("elemental_ore", "elemental_workshop_furnace*") {
            if (!get("elemental_workshop_furnace", false) || !get("elemental_workshop_bellows", false)) {
                message("The furnace needs to be hotter to be of any use.")
                return@itemOnObjectOperate
            }
            if (!has(Skill.Smithing, 20)) {
                message("You need a Smithing level of 20 to smelt elemental ore.")
                return@itemOnObjectOperate
            }
            if (inventory.count("coal") < 4) {
                message("You need four heaps of coal to smelt elemental ore.")
                return@itemOnObjectOperate
            }
            inventory.transaction {
                remove("coal", 4)
                remove("elemental_ore")
            }
            if (inventory.transaction.error != TransactionError.None) {
                return@itemOnObjectOperate
            }
            anim("elemental_furnace_smelt")
            message("You place the elemental ore and four heaps of coal into the furnace.")
            delay(5)
            inventory.add("elemental_bar")
            exp(Skill.Smithing, 7.0)
            message("You retrieve a bar of elemental metal.")
        }

        objectOperate("Turn", "elemental_workshop_water_controls*") { (target) ->
            if (get("elemental_workshop_water_wheel", false)) {
                message("Now that the water wheel is running, the valve seems locked off.")
                return@objectOperate
            }
            val left = target.tile.x < 2720
            anim("turn_water_controls")
            message("You turn the handle.")
            delay(3)
            if (left) {
                set("elemental_workshop_left_water_control", !get("elemental_workshop_left_water_control", false))
            } else if (get("elemental_workshop_left_water_control", false)) {
                set("elemental_workshop_right_water_control", false)
            } else {
                set("elemental_workshop_right_water_control", !get("elemental_workshop_right_water_control", false))
            }
        }

        objectOperate("Pull", "elemental_workshop_water_wheel_lever") { (target) ->
            message("You pull the lever")
            pullLever(target)
            when {
                get("elemental_workshop_water_wheel", false) -> {
                    sound("water_wheel_stop")
                    message("You hear the sound of a water wheel coming to a standstill.")
                    set("elemental_workshop_water_wheel", false)
                    set("elemental_workshop_bellows", false)
                }
                !get("elemental_workshop_left_water_control", false) || !get("elemental_workshop_right_water_control", false) -> {
                    sound("water_gates_reset")
                    message("You hear the sound of the flow gates resetting.")
                    set("elemental_workshop_left_water_control", false)
                    set("elemental_workshop_right_water_control", false)
                }
                else -> {
                    sound("water_wheel_start")
                    message("You hear the sound of a water wheel starting up.")
                    set("elemental_workshop_water_wheel", true)
                }
            }
        }

        objectOperate("Fix", "elemental_workshop_bellows*") {
            if (get("elemental_workshop_bellows_fixed", false)) {
                message("The bellows already look fixed to you.")
                return@objectOperate
            }
            if (!has(Skill.Crafting, 20)) {
                message("You need a Crafting level of 20 to fix the bellows.")
                return@objectOperate
            }
            when {
                !inventory.contains("needle") -> message("You need a needle to fix the bellows.")
                !inventory.contains("thread") -> message("You need some thread to fix the bellows.")
                !inventory.contains("leather") -> message("You need a piece of leather to fix the bellows.")
                else -> {
                    inventory.transaction {
                        remove("leather")
                        remove("thread")
                    }
                    if (inventory.transaction.error != TransactionError.None) {
                        return@objectOperate
                    }
                    anim("fix_bellows")
                    message("You stitch the leather over the hole in the bellows.")
                    set("elemental_workshop_bellows_fixed", true)
                }
            }
        }

        objectOperate("Pull", "elemental_workshop_bellows_lever") { (target) ->
            if (!get("elemental_workshop_bellows_fixed", false)) {
                player<Neutral>("I shouldn't risk damaging the bellows any further.")
                return@objectOperate
            }
            message("You pull the lever")
            pullLever(target)
            sound("lever")
            when {
                !get("elemental_workshop_water_wheel", false) -> message("Nothing happens; the lever resets itself.")
                get("elemental_workshop_bellows", false) -> {
                    message("The bellows stop pumping air.")
                    set("elemental_workshop_bellows", false)
                }
                else -> {
                    message("The bellows pump air down the pipe.")
                    set("elemental_workshop_bellows", true)
                }
            }
        }

        itemOnObjectOperate("elemental_bar", "elemental_workshop_workbench") {
            if (!has(Skill.Smithing, 20)) {
                message("You need a Smithing level of 20 to create an elemental shield.")
                return@itemOnObjectOperate
            }
            if (!inventory.contains("slashed_book") && !inventory.contains("battered_book")) {
                statement("This workbench is too complicated. You need instructions to follow.")
                return@itemOnObjectOperate
            }
            if (!inventory.contains("hammer")) {
                statement("You don't have a tool available to shape the metal.")
                return@itemOnObjectOperate
            }
            anim("spinning")
            if (inventory.replace("elemental_bar", "elemental_shield")) {
                exp(Skill.Smithing, 20.0)
                message("Following the instructions in the book you make an elemental shield.")
            }
        }

        npcOperate("Mine", "elemental_rock_elemental_workshop") { (target) ->
            if (target.transform == "earth_elemental_2") {
                return@npcOperate
            }
            if (!has(Skill.Mining, 20)) {
                statement("You need a Mining level of at least 20 to mine elemental ore.")
                return@npcOperate
            }
            if (Pickaxe.best(this) == null) {
                statement("You do not have a pickaxe which you have the Mining level to use.")
                return@npcOperate
            }
            target.anim("elemental_rock_wake")
            target.queue("elemental_rock_wake", 1) {
                say("Grr... Ge'roff us!")
                transform("earth_elemental_2")
                queue("elemental_rock_attack", 2) {
                    interactPlayer(this@npcOperate, "Attack")
                }
            }
        }
    }

    private fun Player.crate(variable: String, item: String, found: String) {
        if (get(variable, false)) {
            message("You search the crate but find nothing.")
            return
        }
        if (inventory.add(item)) {
            set(variable, true)
            message(found)
        }
    }

    private fun Player.pullLever(lever: GameObject) {
        anim("pull_lever")
        lever.replace("elemental_workshop_lever_pulled", ticks = 2)
    }
}
