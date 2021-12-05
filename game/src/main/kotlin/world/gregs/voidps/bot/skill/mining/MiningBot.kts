import world.gregs.voidps.bot.*
import world.gregs.voidps.bot.bank.closeBank
import world.gregs.voidps.bot.bank.depositAll
import world.gregs.voidps.bot.bank.openBank
import world.gregs.voidps.bot.bank.withdraw
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.engine.action.ActionFinished
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.items
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.engine.utility.*
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.activity.skill.mining.Pickaxe
import world.gregs.voidps.world.activity.skill.mining.rock.RegularRock
import world.gregs.voidps.world.activity.skill.mining.rock.Rock

val areas: Areas by inject()
val tasks: TaskManager by inject()

on<ActionFinished>({ type == ActionType.Mining }) { bot: Bot ->
    bot.resume("mining")
}

on<World, Startup> {
    for (area in areas.getTagged("mine")) {
        val spaces: Int = area["spaces", 1]
        val type = area["rocks", emptyList<String>()].map { RegularRock.valueOf(it.capitalize()) }.firstOrNull() ?: continue
        val range: IntRange = area["levels", "1-5"].toIntRange()
        val task = Task(
            name = "mine ${type.id.plural(2).toLowerCase()} at ${area.name}".replace("_", " "),
            block = {
                while (player.levels.getMax(Skill.Mining) < range.last + 1) {
                    mineRocks(area, type)
                }
            },
            area = area.area,
            spaces = spaces,
            requirements = listOf(
                { player.levels.getMax(Skill.Mining) in range },
                { hasUsablePickaxe() || hasCoins(150) }
            )
        )
        tasks.register(task)
    }
}

suspend fun Bot.mineRocks(map: MapArea, type: Rock) {
    setupInventory()
    goToArea(map)
    while (player.inventory.isNotFull()) {
        val rocks = player.viewport.objects
            .filter { isAvailableRock(map, it, type) }
            .map { rock -> rock to tile.distanceTo(rock) }
        val rock = weightedSample(rocks, invert = true)
        if (rock == null) {
            await("tick")
            if (player.inventory.spaces < 4) {
                break
            }
            continue
        }
        player.instructions.emit(InteractObject(rock.def.id, rock.tile.x, rock.tile.y, 1))
        await("mining")
    }
}

fun Bot.isAvailableRock(map: MapArea, obj: GameObject, type: Rock): Boolean {
    if (!map.area.contains(obj.tile)) {
        return false
    }
    if (!obj.def.options.contains("Mine")) {
        return false
    }
    val rock = Rock.get(player, obj) ?: return false
    if (type != rock) {
        return false
    }
    return player.has(Skill.Mining, rock.level, false)
}

fun Bot.getBestUsableShopPickaxe(shop: String): Pickaxe? {
    val container: ContainerDefinition = get<ContainerDefinitions>().get(shop)
    return container.items()
        .mapNotNull { Pickaxe.get(it) }
        .filter { Pickaxe.hasRequirements(player, it, false) }
        .minByOrNull { it.delay }
}

fun Bot.getBestOwnedUsablePickaxe(): Pickaxe? {
    val weapon = player.equipped(EquipSlot.Weapon)
    if (Pickaxe.hasRequirements(player, weapon)) {
        return Pickaxe.get(weapon.id)
    }
    val inventoryPickaxe = player.inventory.getItems()
        .mapNotNull { Pickaxe.get(it.id) }
        .filter { Pickaxe.hasRequirements(player, it) }
        .minByOrNull { it.delay }
    if (inventoryPickaxe != null) {
        return inventoryPickaxe
    }
    return player.bank.getItems()
        .mapNotNull { Pickaxe.get(it.id) }
        .filter { Pickaxe.hasRequirements(player, it) }
        .minByOrNull { it.delay }
}

suspend fun Bot.setupInventory() {
    val bestOwned = getBestOwnedUsablePickaxe()
    if (bestOwned == null || bestOwned.delay > 2) {
        val bestShop = getBestUsableShopPickaxe("bobs_brilliant_axes")
        if (bestShop != null && (bestOwned?.delay ?: 10) > bestShop.delay) {
            buyItem(bestShop.id)
            equip(bestShop.id)
            return
        }
    }

    val equipped = Pickaxe.hasRequirements(player, player.equipped(EquipSlot.Weapon))
    val hasPickaxe = equipped || player.inventory.getItems().any { Pickaxe.hasRequirements(player, it) }
    if (hasPickaxe && player.inventory.spaces > 10) {
        return
    }
    openBank()
    depositAll()
    if (!equipped) {
        val bestPickaxe = player.bank.getItems()
            .mapNotNull { Pickaxe.get(it.id) }
            .filter { Pickaxe.hasRequirements(player, it, false) }
            .minByOrNull { it.delay }!!
        withdraw(bestPickaxe.id)
        equip(bestPickaxe.id)
    }
    closeBank()
}

fun Bot.hasUsablePickaxe(): Boolean {
    if (Pickaxe.hasRequirements(player, player.equipped(EquipSlot.Weapon))) {
        return true
    }
    if (player.inventory.getItems().any { Pickaxe.hasRequirements(player, it) }) {
        return true
    }
    if (player.bank.getItems().any { Pickaxe.hasRequirements(player, it) }) {
        return true
    }
    return false
}