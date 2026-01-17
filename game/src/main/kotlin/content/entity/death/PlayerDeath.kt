package content.entity.death

import content.area.misthalin.lumbridge.church.Gravestone
import content.area.wilderness.inMultiCombat
import content.area.wilderness.inWilderness
import content.entity.combat.*
import content.entity.combat.Target
import content.entity.combat.hit.directHit
import content.entity.gfx.areaGfx
import content.entity.player.inv.item.tradeable
import content.entity.player.kept.ItemsKeptOnDeath
import content.entity.proj.shoot
import content.skill.prayer.getActivePrayerVarKey
import content.skill.prayer.praying
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class PlayerDeath(
    val floorItems: FloorItems,
    val enums: EnumDefinitions,
) : Script {

    val respawnTile: Tile
        get() = Tile(Settings["world.home.x", 0], Settings["world.home.y", 0], Settings["world.home.level", 0])

    init {
        playerDeath { onDeath ->
            dead = true
            strongQueue("death") {
                steps.clear()
                val dealer = damageDealers.maxByOrNull { it.value }
                val killer = dealer?.key
                AuditLog.event(player, "died", tile, killer)
                while (true) {
                    instructions.tryReceive().getOrNull() ?: break
                }
                val tile = tile.copy()
                set("death_tile", tile)
                val wilderness = inWilderness
                retribution(player)
                wrath(player)
                message("Oh dear, you are dead!")
                anim("human_death")
                delay(5)
                clearAnim()
                attackers.clear()
                damageDealers.clear()
                jingle("death")
                timers.stopAll()
                softTimers.stopAll()
                clear(getActivePrayerVarKey())
                if (onDeath.dropItems) {
                    dropItems(player, killer, tile, wilderness)
                }
                levels.clear()
                if (onDeath.teleport != null) {
                    tele(onDeath.teleport!!)
                } else {
                    tele(respawnTile)
                }
                face(Direction.SOUTH, update = false)
                dead = false
            }
        }
    }

    fun dropItems(player: Player, killer: Character?, tile: Tile, inWilderness: Boolean) {
        if (player.isAdmin()) {
            return
        }
        val items = ItemsKeptOnDeath.getAllOrdered(player)
        val kept = ItemsKeptOnDeath.kept(player, items, enums)

        // Remove kept so they aren't dropped
        for (item in kept) {
            if (player.inventory.remove(item.id, item.amount) || player.equipment.remove(item.id, item.amount)) {
                continue
            }
        }

        // Spawn grave
        val time = if (!inWilderness || killer !is Player) Gravestone.spawn(player, tile) else 0
        // Drop everything
        drop(player, Item("bones"), tile, inWilderness, killer, time)
        drop(player, player.inventory, tile, inWilderness, killer, time)
        drop(player, player.equipment, tile, inWilderness, killer, time)
        // Clear everything
        player.inventory.clear()
        player.equipment.clear()

        // Return kept items
        for (item in kept) {
            AuditLog.event(player, "kept", item)
            player.inventory.add(item.id, item.amount)
        }
    }

    fun drop(player: Player, inventory: Inventory, tile: Tile, inWilderness: Boolean, killer: Character?, time: Int) {
        for (item in inventory.items) {
            if (item.isEmpty()) {
                continue
            }
            drop(player, item, tile, inWilderness, killer, time)
        }
    }

    fun drop(
        player: Player,
        item: Item,
        tile: Tile,
        inWilderness: Boolean,
        killer: Character?,
        time: Int,
    ) {
        AuditLog.event(player, "lost", item)
        if (inWilderness && killer is Player) {
            if (item.tradeable) {
                floorItems.add(tile, item.id, item.amount, revealTicks = 180, disappearTicks = 240, owner = killer)
            } else {
                floorItems.add(tile, "coins", item.amount * item.def.cost, revealTicks = 180, disappearTicks = 240, owner = killer)
            }
        } else {
            floorItems.add(tile, item.id, item.amount, revealTicks = time, disappearTicks = time + 60, owner = player)
        }
    }

    // TODO get correct graphic heights, delays and curves

    fun retribution(source: Player) {
        if (!source.praying("retribution")) {
            return
        }
        source.gfx("retribution")
        val maxHit = (source.levels.get(Skill.Prayer) * 2.5).toInt()
        val target = source.target
        for (direction in Direction.all) {
            val tile = source.tile.add(direction)
            if (source.inMultiCombat) {
                hitCharacters(tile, source, maxHit)
            } else if (target?.tile == tile) {
                source.directHit(target, random.nextInt(maxHit))
            }
            source.shoot("retribution_splash", tile, height = 30, endHeight = 30)
        }
    }

    fun wrath(source: Player) {
        if (!source.praying("wrath")) {
            return
        }
        source.gfx("wrath")
        for (direction in Direction.all) {
            val inner = source.tile.add(direction)
            val outer = inner.add(direction)
            source.shoot("wrath_shoot", outer, height = 10, endHeight = 0)
            if (direction.isDiagonal()) {
                areaGfx("wrath_splash", inner, delay = 30)
            }
            areaGfx("wrath_splash", outer, delay = 10)
        }
        val maxHit = (source.levels.getMax(Skill.Prayer) * 2.5).toInt()
        if (source.inMultiCombat) {
            for (tile in Spiral.spiral(source.tile, 5)) {
                hitCharacters(tile, source, maxHit)
            }
        } else {
            val target = source.target ?: return
            if (source.tile.distanceTo(target.tile) <= 2) {
                source.directHit(target, random.nextInt(maxHit))
            }
        }
    }

    fun hitCharacters(tile: Tile, source: Player, maxHit: Int) {
        for (player in Players.at(tile)) {
            if (Target.attackable(source, player)) {
                source.directHit(player, random.nextInt(maxHit))
            }
        }
        for (npc in NPCs.at(tile)) {
            if (Target.attackable(source, npc)) {
                source.directHit(npc, random.nextInt(maxHit))
            }
        }
    }
}
