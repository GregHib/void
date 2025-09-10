package content.bot.skill.combat

import content.bot.*
import content.bot.Bot
import content.bot.interact.item.pickup
import content.bot.interact.navigation.await
import content.bot.interact.navigation.cancel
import content.bot.interact.navigation.goToArea
import content.bot.interact.navigation.resume
import content.entity.combat.attackers
import content.entity.combat.inCombat
import content.entity.death.playerDeath
import content.entity.death.weightedSample
import content.skill.magic.spell.removeSpellItems
import content.skill.magic.spell.spell
import content.skill.magic.spell.spellBook
import content.skill.slayer.categories
import net.pearx.kasechange.toLowerSpaceCase
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.data.definition.AmmoDefinitions
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.equip.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirements
import world.gregs.voidps.engine.entity.character.player.skill.level.levelChange
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.InteractInterface
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

suspend fun Bot.setAttackStyle(skill: Skill) {
    setAttackStyle(
        when (skill) {
            Skill.Strength -> 1
            Skill.Defence -> 3
            else -> 0
        },
    )
}

suspend fun Bot.setAutoCast(spell: String) {
    val definitions = get<InterfaceDefinitions>()
    val def = definitions.get(player.spellBook)
    player.instructions.send(InteractInterface(def.id, definitions.getComponentId(player.spellBook, spell) ?: return, -1, -1, 0))
}

suspend fun Bot.setAttackStyle(style: Int) {
    player.instructions.send(InteractInterface(interfaceId = 884, componentId = style + 11, itemId = -1, itemSlot = -1, option = 0))
}

@Script
class CombatBot {

    val areas: AreaDefinitions by inject()
    val tasks: TaskManager by inject()
    val floorItems: FloorItems by inject()

    init {
        variableSet("in_combat", to = 1) { player ->
            if (player.isBot) {
                player.bot.resume("combat")
            }
        }

        levelChange(Skill.Constitution) { player ->
            if (player.isBot && player.levels.getPercent(Skill.Constitution) < 50.0) {
                val food = player.inventory.items.firstOrNull { it.def.contains("heals") } ?: return@levelChange
                player.bot.inventoryOption(food.id, "Eat")
            }
        }

        playerDeath { player ->
            if (player.isBot) {
                player.clear("area")
                player.bot.cancel()
            }
        }

        worldSpawn {
            for (area in areas.getTagged("combat_training")) {
                val spaces: Int = area["spaces", 1]
                val types = area["npcs", emptyList<String>()].toSet()
                val range = area["levels", "1-5"].toIntRange()
                val skills = listOf(Skill.Attack, Skill.Strength, Skill.Defence, Skill.Ranged, Skill.Magic).shuffled().take(spaces)
                for (skill in skills) {
                    val task = Task(
                        name = "train ${skill.name} killing ${types.joinToString(", ")} at ${area.name}".toLowerSpaceCase(),
                        block = {
                            while (levels.getMax(skill) < range.last + 1) {
                                bot.fight(area, skill, types)
                            }
                        },
                        area = area.area,
                        spaces = 1,
                        requirements = listOf(
                            { levels.getMax(skill) in range },
                            { bot.hasExactGear(skill) || bot.hasCoins(2000) },
                        ),
                    )
                    tasks.register(task)
                }
            }
        }
    }

    suspend fun Bot.fight(map: AreaDefinition, skill: Skill, races: Set<String>) {
        setupGear(skill)
        goToArea(map)
        setAttackStyle(skill)
        while (player.inventory.spaces > 0 && player.isRangedNotOutOfAmmo(skill) && player.isMagicNotOutOfRunes(skill)) {
            val targets = get<NPCs>()
                .filter { isAvailableTarget(map, it, races) }
                .map { it to tile.distanceTo(it) }
            val target = weightedSample(targets, invert = true)
            if (target == null) {
                await("tick")
                if (player.inventory.spaces < 4) {
                    break
                }
                continue
            }
            npcOption(target, "Attack")
            await("combat", timeout = 30)
            target.get<Tile>("death_tile")?.let {
                pickupItems(it, 4)
            }
            equipAmmo(skill)
            // TODO on death run back and pickup stuff
        }
    }

    fun Player.isRangedNotOutOfAmmo(skill: Skill): Boolean {
        if (skill != Skill.Ranged) {
            return true
        }
        return has(EquipSlot.Ammo)
    }

    fun Player.isMagicNotOutOfRunes(skill: Skill): Boolean {
        if (skill != Skill.Magic) {
            return true
        }
        val spell = spell
        return removeSpellItems(spell)
    }

    suspend fun Bot.pickupItems(tile: Tile, amount: Int) {
        repeat(random.nextInt(2, 8)) {
            if (player.inventory.contains("bones")) {
                inventoryOption("bones", "Bury")
                await("tick")
            }
            await("tick")
        }
        repeat(amount) {
            val item = floorItems[tile].firstOrNull() ?: return@repeat
            pickup(item)
        }
    }

    fun Bot.isAvailableTarget(map: AreaDefinition, npc: NPC, races: Set<String>): Boolean {
        if (!npc.tile.within(player.tile, Viewport.VIEW_RADIUS)) {
            return false
        }
        if (player.attackers.isNotEmpty()) {
            return player.attackers.contains(npc)
        }
        if (npc.inCombat) {
            return false
        }
        if (!npc.def.options.contains("Attack")) {
            return false
        }
        if (!races.contains(npc.def.name.toSnakeCase()) && npc.categories.none { races.contains(it) }) {
            return false
        }
        if (!map.area.contains(npc.tile)) {
            return false
        }
        val difference = npc.def.combat - player.combatLevel
        return difference < 5
    }

    fun Bot.equipAmmo(skill: Skill) {
        if (skill == Skill.Ranged) {
            val ammo = player.equipped(EquipSlot.Ammo)
            if (ammo.isEmpty()) {
                val weapon = player.equipped(EquipSlot.Weapon)
                val ammoDefinitions: AmmoDefinitions = get()
                player.inventory.items
                    .firstOrNull { player.hasRequirements(it) && ammoDefinitions.get(weapon.def["ammo_group", ""]).items.contains(it.id) }
                    ?.let {
                        equip(it.id)
                    }
            } else if (player.inventory.contains(ammo.id)) {
                equip(ammo.id)
            }
        }
    }
}
