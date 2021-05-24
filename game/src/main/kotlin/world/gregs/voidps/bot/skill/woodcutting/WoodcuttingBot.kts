import world.gregs.voidps.bot.Task
import world.gregs.voidps.bot.TaskStore
import world.gregs.voidps.bot.bank.*
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.bot.shop.buy
import world.gregs.voidps.bot.shop.closeShop
import world.gregs.voidps.bot.shop.openShop
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
import world.gregs.voidps.engine.map.Distance
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.activity.skill.woodcutting.Hatchet
import world.gregs.voidps.world.activity.skill.woodcutting.tree.RegularTree
import world.gregs.voidps.world.activity.skill.woodcutting.tree.Tree

val areas: Areas by inject()
val tasks: TaskStore by inject()

on<ActionFinished>({ type == ActionType.Woodcutting }) { bot: Bot ->
    bot.resume("woodcutting")
}

on<World, Startup> {
    for (area in areas.getTagged("trees")) {
        val spaces = area.tags.firstOrNull { it.startsWith("spaces_") }?.removePrefix("spaces_")?.toIntOrNull() ?: 1
        repeat(spaces) {
            val type = when {
                area.tags.contains("willow") -> RegularTree.Willow
                area.tags.contains("maple") -> RegularTree.Maple_Tree
                area.tags.contains("yew") -> RegularTree.Yew
                area.tags.contains("ivy") -> RegularTree.Ivy
                area.tags.contains("magic") -> RegularTree.Magic_Tree
                else -> null
            }

            val range = when (type) {
                RegularTree.Willow -> 30 until 45
                RegularTree.Maple_Tree -> 45 until 60
                RegularTree.Yew -> 60 until 68
                RegularTree.Ivy -> 68 until 75
                RegularTree.Magic_Tree -> 75..99
                else -> 0 until 30
            }

            val task = Task(
                {
                    while (player.levels.getMax(Skill.Woodcutting) < range.last) {
                        cutTrees(area, type)
                    }
                },
                requirements = listOf(
                    { player.levels.getMax(Skill.Woodcutting) in range },
                    { hasUsableHatchet() || hasCoins(2000) }
                )

            )
            tasks.register(task)
        }
    }
}

suspend fun Bot.cutTrees(map: MapArea, type: Tree? = null) {
    setupInventory()
    goToArea(map)
    while (player.inventory.isNotFull()) {
        val tree = player.viewport.objects
            .filter { isAvailableTree(map, it, type) }
            .minByOrNull { tile.distanceTo(Distance.getNearest(it.tile, it.size, tile)) }
        if (tree == null) {
            await<Unit>("tick")
            if (player.inventory.spaces < 4) {
                break
            }
            continue
        }
        player.instructions.emit(InteractObject(tree.id, tree.tile.x, tree.tile.y, 1))
        await<Unit>("woodcutting")
    }
}

fun Bot.isAvailableTree(map: MapArea, obj: GameObject, type: Tree?): Boolean {
    if (!map.area.contains(obj.tile)) {
        return false
    }
    if (!obj.def.options.contains("Chop down")) {
        return false
    }
    val tree = Tree.get(obj) ?: return false
    if (type != null && type != tree) {
        return false
    }
    return player.has(Skill.Woodcutting, tree.level, false)
}

fun Bot.getBestUsableShopHatchet(shop: String): Hatchet? {
    val container: ContainerDefinition = get<ContainerDefinitions>().get(shop)
    return container.items()
        .mapNotNull { Hatchet.get(it) }
        .filter { Hatchet.hasRequirements(player, it, false) }
        .maxByOrNull { it.index }
}

fun Bot.getBestOwnedUsableHatchet(): Hatchet? {
    val weapon = player.equipped(EquipSlot.Weapon)
    if (Hatchet.hasRequirements(player, weapon)) {
        return Hatchet.get(weapon.name)
    }
    val inventoryHatchet = player.inventory.getItems()
        .mapNotNull { Hatchet.get(it.name) }
        .filter { Hatchet.hasRequirements(player, it) }
        .maxByOrNull { it.index }
    if (inventoryHatchet != null) {
        return inventoryHatchet
    }
    return player.bank.getItems()
        .mapNotNull { Hatchet.get(it.name) }
        .filter { Hatchet.hasRequirements(player, it) }
        .maxByOrNull { it.index }
}

suspend fun Bot.setupInventory() {
    val bestOwned = getBestOwnedUsableHatchet()
    if (bestOwned == null || bestOwned.index < 7) {
        val bestShop = getBestUsableShopHatchet("bobs_brilliant_axes")
        if (bestShop != null && bestOwned?.index ?: -1 < bestShop.index) {
            buyHatchet(bestShop)
            return
        }
    }

    val equipped = Hatchet.hasRequirements(player, player.equipped(EquipSlot.Weapon))
    val hasHatchet = equipped || player.inventory.getItems().any { Hatchet.hasRequirements(player, it) }
    if (hasHatchet && player.inventory.spaces > 10) {
        return
    }
    openBank()
    depositAll()
    if (!equipped) {
        val bestHatchet = player.bank.getItems()
            .mapNotNull { Hatchet.get(it.name) }
            .filter { Hatchet.hasRequirements(player, it, false) }
            .maxByOrNull { it.ordinal }!!
        withdraw(bestHatchet.id)
    }
    closeBank()
}

suspend fun Bot.buyHatchet(hatchet: Hatchet) {
    withdrawCoins()
    openShop("bobs_axe_shop")
    buy(hatchet.id)
    closeShop()
}

fun Bot.hasUsableHatchet(): Boolean {
    if (Hatchet.hasRequirements(player, player.equipped(EquipSlot.Weapon))) {
        return true
    }
    if (player.inventory.getItems().any { Hatchet.hasRequirements(player, it) }) {
        return true
    }
    if (player.bank.getItems().any { Hatchet.hasRequirements(player, it) }) {
        return true
    }
    return false
}

fun Bot.hasCoins(amount: Int): Boolean {
    if (player.inventory.contains("coins") && player.inventory.getCount("coins") >= amount) {
        return true
    }
    if (player.bank.contains("coins") && player.bank.getCount("coins") >= amount) {
        return true
    }
    return false
}