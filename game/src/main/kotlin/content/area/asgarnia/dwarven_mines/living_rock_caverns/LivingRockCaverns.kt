package content.area.asgarnia.dwarven_mines.living_rock_caverns

import content.entity.effect.transform
import content.entity.player.bank.BankDeposit
import content.entity.player.dialogue.type.warning
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class LivingRockCaverns : Script {
    init {
        objectOperate("Climb", "living_rock_caverns_entrance") { (target) ->
            if (!warning("living_rock_caverns")) {
                return@objectOperate
            }
            walkToDelay(target.tile)
            face(Direction.WEST)
            delay(1)
            anim("living_rock_caverns_climb_ledge")
            delay(2)
            tele(3651, 5122)
            val rope = GameObjects.find(Tile(3651, 5123), "living_rock_caverns_rope")
            anim("living_rock_caverns_rope") // TODO proper anim
            rope.anim("living_rock_caverns_rope_drop")
            face(Direction.SOUTH)
            delay(3)
        }

        objectOperate("Climb", "living_rock_caverns_rope") { (target) ->
            face(Direction.NORTH)
            anim("living_rock_caverns_climb_rope")
            delay(2)
            tele(3012, 9832)
        }

        worldSpawn {
            update()
        }

        settingsReload {
            if (Settings["events.livingRockCaverns.respawnTimeMinutes", 60] > 0 && !World.contains("living_rock_caverns_timer")) {
                update()
            } else if (Settings["events.livingRockCaverns.respawnTimeMinutes", 60] <= 0 && World.contains("living_rock_caverns_timer")) {
                World.clearQueue("living_rock_caverns_timer")
            }
        }

        objectOperate("Deposit", "pulley_lift") {
            open("bank_deposit_box")
        }

        itemOnObjectOperate(obj = "pulley_lift", handler = BankDeposit::itemOnDeposit)
    }

    fun update() {
        val minutes = Settings["events.livingRockCaverns.respawnTimeMinutes", 60]
        if (minutes < 0) {
            return
        }
        rockSpawn(minutes, "coal")
        rockSpawn(minutes, "gold")
        if (minutes == 0) {
            return
        }
        World.queue("living_rock_caverns_timer", TimeUnit.MINUTES.toTicks(minutes)) {
            var patriarchSpawned = false
            for (region in Areas["living_rock_caverns"].toRegions()) {
                for (npc in NPCs.at(region.toLevel(0))) {
                    if (npc.id == "living_rock_patriarch") {
                        if (npc.transform == "") {
                            patriarchSpawned = true
                        } else {
                            NPCs.remove(npc)
                        }
                        continue
                    }
                    npc.anim("${npc.id}_idle")
                }
            }
            if (!patriarchSpawned) {
                NPCs.add("living_rock_patriarch", Tables.tileList("living_rock_cavern_spawns.patriarch.tiles").random(random))
            }
        }
    }

    private fun rockSpawn(minutes: Int, type: String) {
        val tiles = Tables.tileList("living_rock_cavern_spawns.$type.tiles")
            .shuffled(random)
            .take(Settings["events.livingRockCaverns.${type}Deposits", 1])
        for (tile in tiles) {
            GameObjects.add("mineral_deposit_$type", tile, ticks = if (minutes <= 0) GameObjects.NEVER else TimeUnit.MINUTES.toTicks(minutes))
        }
    }
}
