import world.gregs.voidps.bot.*
import world.gregs.voidps.bot.bank.closeBank
import world.gregs.voidps.bot.bank.depositAll
import world.gregs.voidps.bot.bank.openBank
import world.gregs.voidps.bot.bank.withdraw
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.bot.skill.combat.hasExactGear
import world.gregs.voidps.bot.skill.combat.setupGear
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
import world.gregs.voidps.world.activity.skill.woodcutting.Hatchet
import world.gregs.voidps.world.activity.skill.woodcutting.tree.RegularTree
import world.gregs.voidps.world.activity.skill.woodcutting.tree.Tree

val areas: Areas by inject()
val tasks: TaskManager by inject()

on<ActionFinished>({ type == ActionType.Woodcutting }) { bot: Bot ->
    bot.resume("woodcutting")
}

on<World, Startup> {
    for (area in areas.getTagged("trees")) {
        val spaces: Int = area["spaces", 1]
        val range: IntRange = area["levels", "1-5"].toIntRange()
        val type = area["trees", emptyList<String>()].map { RegularTree.valueOf(it.capitalize()) }.firstOrNull()
        val task = Task(
            name = "cut ${(type ?: RegularTree.Tree).name.plural(2).toLowerCase()} at ${area.name}".toTitleCase(),
            block = {
                while (player.levels.getMax(Skill.Woodcutting) < range.last + 1) {
                    cutTrees(area, type)
                }
            },
            area = area.area,
            spaces = spaces,
            requirements = listOf(
                { player.levels.getMax(Skill.Woodcutting) in range },
                { hasExactGear(Skill.Woodcutting) || hasCoins(2000) }
            )
        )
        tasks.register(task)
    }
}

suspend fun Bot.cutTrees(map: MapArea, type: Tree? = null) {
    setupGear(Skill.Woodcutting)
    goToArea(map)
    while (player.inventory.isNotFull()) {
        val trees = player.viewport.objects
            .filter { isAvailableTree(map, it, type) }
            .map { tree -> tree to tile.distanceTo(tree) }
        val tree = weightedSample(trees, invert = true)
        if (tree == null) {
            await("tick")
            if (player.inventory.spaces < 4) {
                break
            }
            continue
        }
        player.instructions.emit(InteractObject(tree.def.id, tree.tile.x, tree.tile.y, 1))
        await("woodcutting")
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
    if (type != null && type != tree && type != RegularTree.Oak && tree != RegularTree.Tree) {
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
        return Hatchet.get(weapon.id)
    }
    val inventoryHatchet = player.inventory.getItems()
        .mapNotNull { Hatchet.get(it.id) }
        .filter { Hatchet.hasRequirements(player, it) }
        .maxByOrNull { it.index }
    if (inventoryHatchet != null) {
        return inventoryHatchet
    }
    return player.bank.getItems()
        .mapNotNull { Hatchet.get(it.id) }
        .filter { Hatchet.hasRequirements(player, it) }
        .maxByOrNull { it.index }
}

suspend fun Bot.setupInventory() {
    val bestOwned = getBestOwnedUsableHatchet()
    if (bestOwned == null || bestOwned.index < 7) {
        val bestShop = getBestUsableShopHatchet("bobs_brilliant_axes")
        if (bestShop != null && (bestOwned?.index ?: -1) < bestShop.index) {
            buyItem(bestShop.id)
            equip(bestShop.id)
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
            .mapNotNull { Hatchet.get(it.id) }
            .filter { Hatchet.hasRequirements(player, it, false) }
            .maxByOrNull { it.ordinal }!!
        withdraw(bestHatchet.id)
        equip(bestHatchet.id)
    }
    closeBank()
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