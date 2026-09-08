package content.minigame.mage_training_arena

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.skill.magic.spell.SpellRunes.removeItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.engine.timer.Timer

/**
 * Shapes taken from piles are enchanted into orbs for points; a bonus shape rotates on a world
 * timer and private dragonstones respawn around the room for double points.
 */
class EnchantingChamber : Script {

    init {
        objectOperate("Take-from", "cube_pile,cylinder_pile,icosahedron_pile,pentamid_pile") { (target) ->
            val row = shapes.firstOrNull { it.obj("pile") == target.id } ?: return@objectOperate
            if (!inventory.add(row.item("item"))) {
                message("You have no space left in your inventory.")
                return@objectOperate
            }
            anim("climb_down")
        }

        onItem("modern_spellbook:enchant_level_*", "cube,cylinder,icosahedron,pentamid,dragonstone_mage_training_arena") { item, id ->
            if (!MageTrainingArena.inRoom(this, "enchanting")) {
                message("You can't use this spell on this item.")
                return@onItem
            }
            if (hasClock("action_delay")) {
                return@onItem
            }
            val spell = id.substringAfter(":")
            val level = Tables.int("jewellery_enchant.$spell.level")
            if (!has(Skill.Magic, level, message = true)) {
                return@onItem
            }
            inventory.transaction {
                removeItems(this@onItem, spell, message = false)
                replace(item.id, "orb")
            }
            when (inventory.transaction.error) {
                is TransactionError.Deficient -> {
                    message("You do not have the required items to cast this spell.")
                    return@onItem
                }
                TransactionError.None -> {}
                else -> return@onItem
            }
            start("action_delay", 1)
            val type = Tables.string("jewellery_enchant.$spell.type")
            gfx("enchant_ring")
            gfx("mta_enchant_orb")
            anim("enchant_ring")
            sound("enchant_${type}_ring")
            exp(Skill.Magic, Tables.int("jewellery_enchant.$spell.xp") / 10.0 * 0.75)
            val points = points(this, item.id, spell.removePrefix("enchant_level_").toInt())
            PizazzPoints.add(this, "enchanting", points)
        }

        objectOperate("Deposit", "orb_hole") {
            val orbs = inventory.count("orb")
            if (orbs == 0) {
                message("You don't have any orbs to deposit.")
                return@objectOperate
            }
            if (!inventory.remove("orb", orbs)) {
                return@objectOperate
            }
            anim("take")
            sound("mta_deposit_orb")
            val perReward = Tables.int("mta_enchanting.settings.orbs_per_reward")
            var total = get("mage_training_arena_orbs_deposited", 0) + orbs
            AuditLog.event(this, "mta_orbs_deposited", orbs, total)
            while (total >= perReward) {
                total -= perReward
                set("mage_training_arena_orbs_deposited", total)
                addOrDrop(Tables.itemList("mta_enchanting.settings.rune_rewards").random(), Tables.int("mta_enchanting.settings.rune_amount"))
                statement("Congratulations! You've been rewarded with an item for your efforts.")
            }
            set("mage_training_arena_orbs_deposited", total)
        }

        npcOperate("Talk-to", "enchantment_guardian") {
            npc<Neutral>("Greetings young one. How can I enlighten you?")
            guardianMenu()
        }

        taken("dragonstone_mage_training_arena") { floorItem ->
            val stones = dragonstones[index] ?: return@taken
            stones.remove(floorItem)
            val tile = floorItem.tile
            World.queue("mta_dragonstone_${index}_${tile.id}", Tables.int("mta_enchanting.settings.dragonstone_respawn")) {
                if (!MageTrainingArena.inRoom(this, "enchanting")) {
                    return@queue
                }
                dragonstones.getOrPut(index) { mutableListOf() }.add(FloorItems.add(tile, "dragonstone_mage_training_arena", owner = this))
            }
        }

        entered("mage_training_arena_enchanting_chamber") {
            World.timers.startIfAbsent("mta_enchanting")
            spawnDragonstones(this)
            refresh(this)
        }

        exited("mage_training_arena_enchanting_chamber") {
            removeDragonstones(this)
        }

        worldTimerStart("mta_enchanting") {
            Tables.int("mta_enchanting.settings.bonus_interval")
        }

        worldTimerTick("mta_enchanting") {
            if (Players.none { MageTrainingArena.inRoom(it, "enchanting") }) {
                return@worldTimerTick Timer.CANCEL
            }
            changeBonus()
            Timer.CONTINUE
        }
    }

    private suspend fun Player.guardianMenu() {
        choice {
            option<Neutral>("What do I have to do in this room?") {
                npc<Neutral>("In this chamber you will see various piles of shapes. You can pick up these shapes and enchant them using the enchant jewelry spells. By enchanting these shapes, you'll be converting them into orbs which can be")
                npc<Neutral>("placed in the hole in the centre. You will get Enchantment Pizazz Points for every ten shapes that you convert and the points you get depends on the level of enchantment spell you cast. You will get a")
                npc<Neutral>("bonus Pizazz Point for enchanting a certain shape at a certain time, which I will periodically shout out. You will also be rewarded with an item for every so many orbs you deposit into the hole.")
                guardianMenu()
            }
            option<Neutral>("What are the rewards?") {
                npc<Neutral>("As well as the magic experience from casting your enchantment spells, you will get Enchantment Pizazz Points for converting so many shapes, plus a bonus point for converting shape of the correct type. You")
                npc<Neutral>("should also note that you will occasionally be rewarded with items when you put one of the enchanted orbs in the floor.")
                guardianMenu()
            }
            option<Neutral>("Got any tips that may help me?") {
                npc<Neutral>("Try to guess or keep track of the time between the change of the best shape to enchant. This means you can be ready to run to a different shape when you know it is about to change. Look out for the dragon")
                npc<Neutral>("gems, as these will get you more Pizazz Points!")
                player<Neutral>("I see.")
                npc<Neutral>("Oh, and a word of warning: should you decide to leave this room by a method other than the exit portals, you will be teleported to the entrance and have any items that you picked up in the room removed.")
            }
            option<Neutral>("Thanks, bye!")
        }
    }

    companion object {
        var bonus: String = "cube"

        private val dragonstones = HashMap<Int, MutableList<FloorItem>>()

        val shapes: List<RowDefinition>
            get() = Tables.get("mta_shapes").rows()

        fun shape(item: String): String? = shapes.firstOrNull { it.item("item") == item }?.rowId

        /**
         * Dragonstones score double immediately; shapes score every tenth conversion plus one for the bonus shape.
         */
        fun points(player: Player, item: String, spellLevel: Int): Int {
            if (item == "dragonstone_mage_training_arena") {
                return spellLevel * 2
            }
            val shape = shape(item) ?: return 0
            var points = 0
            val converted = player.inc("mage_training_arena_shapes_converted")
            if (converted >= Tables.int("mta_enchanting.settings.shapes_per_reward")) {
                player["mage_training_arena_shapes_converted"] = 0
                points += spellLevel
            }
            if (shape == bonus) {
                points += 1
                player.message("You get $points bonus point${if (points != 1) "s" else ""}!")
            }
            return points
        }

        fun changeBonus() {
            val next = shapes.map { it.rowId }.filter { it != bonus }.random()
            bonus = next
            NPCs.firstOrNull { it.id == "enchantment_guardian" }?.say("The bonus shape has changed to the $next.")
            for (player in Players) {
                if (MageTrainingArena.inRoom(player, "enchanting")) {
                    refresh(player)
                }
            }
        }

        fun refresh(player: Player) {
            for (shape in shapes) {
                player.interfaces.sendVisibility("mage_training_arena_enchanting", "bonus_${shape.rowId}", shape.rowId == bonus)
            }
        }

        fun dragonstones(player: Player): List<FloorItem> = dragonstones[player.index] ?: emptyList()

        private fun spawnDragonstones(player: Player) {
            removeDragonstones(player)
            val stones = mutableListOf<FloorItem>()
            for (tile in Tables.tileList("mta_enchanting.settings.dragonstones")) {
                stones.add(FloorItems.add(tile, "dragonstone_mage_training_arena", owner = player))
            }
            dragonstones[player.index] = stones
        }

        private fun removeDragonstones(player: Player) {
            for (stone in dragonstones.remove(player.index) ?: return) {
                FloorItems.remove(stone)
            }
            for (tile in Tables.tileList("mta_enchanting.settings.dragonstones")) {
                World.clearQueue("mta_dragonstone_${player.index}_${tile.id}")
            }
        }
    }
}
