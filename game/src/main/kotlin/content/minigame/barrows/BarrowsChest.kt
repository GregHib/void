package content.minigame.barrows

import content.entity.combat.hit.directHit
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.shakeCamera
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random
import kotlin.random.nextInt

class BarrowsChest(val drops: DropTables) : Script {
    init {
        objectOperate("Open", "barrows_chest_closed") {
            set("barrows_chest_open", true)
        }

        objectOperate("Close", "barrows_chest_open") {
            set("barrows_chest_open", false)
        }

        objectOperate("Search", "barrows_chest_open") {
            if (!interfaces.contains("barrows_overlay") || get("barrows_looted", false)) {
                message("The chest is empty.")
                return@objectOperate
            }
            val brother = get("barrows_selected_brother", "dharok")
            if (!get("${brother}_killed", false) || !contains("${brother}_spawn")) {
                val tile = Areas["barrows_chest"].random()
                BarrowsCrypts.spawnBrother(this, brother, tile)
                return@objectOperate
            }

            val drops = reward(this)
            AuditLog.event(this, "barrows_chest", *drops.toTypedArray())
            for (drop in drops) {
                addOrDrop(drop.id, drop.amount)
            }
            reset(this)
            softTimers.start("barrows_cave_shake")
            set("barrows_looted", true)
        }

        timerStart("barrows_cave_shake") {
            shakeCamera(type = 0, intensity = 5)
            message("The cave begins to collapse!")
            9
        }

        timerTick("barrows_cave_shake") {
            gfx("falling_rocks")
            say("Ouch!")
            directHit(random.nextInt(30..50))
            message("Some rocks fall from the ceiling and hit you.")
            Timer.CONTINUE
        }

        entered("barrows_tunnels") {
            if (get("barrows_looted", false)) {
                softTimers.restart("barrows_cave_shake")
            }
        }

        exited("barrows_tunnels") {
            set("barrows_chest_open", false)
        }
    }

    private fun reward(player: Player): List<Item> {
        // This is based off of the osrs algorithm which differs from rs2, but the original alg isn't known
        val armour = drops.getValue("barrows_chest_armour")
        val kills = player["barrows_kills", 0]
        val items = mutableListOf<ItemDrop>()
        repeat(kills) {
            armour.roll(maximumRoll = 450 - (58 * kills), list = items)
        }
        val runes = drops.getValue("barrows_chest_runes")
        val levels = player["barrows_kill_levels", 0]
        runes.roll(maximumRoll = levels.coerceAtMost(1012), list = items)
        return items.map { it.toItem() }
    }

    private fun reset(player: Player) {
        player.close("barrows_overlay")
        player.clear("barrows_kill_levels")
        player.clear("barrows_killed_monsters")
        player.clear("barrows_kills")
    }
}
