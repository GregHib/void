import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNpcClick
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Died
import world.gregs.voidps.engine.entity.character.move.cantReach
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.path.strat.CombatTargetStrategy
import world.gregs.voidps.engine.path.strat.CombatTargetStrategy.Companion.isWithinAttackDistance
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.combat.*

on<NPCClick>({ option == "Attack" }) { player: Player ->
    cancel = true
    player.attack(npc)
}

val definitions: InterfaceDefinitions by inject()
val itemDefs: ItemDefinitions by inject()

on<InterfaceOnNpcClick>({ name.endsWith("_spellbook") }) { player: Player ->
    cancel = true
    player.spell = component
    player["attack_range"] = 8
    player["attack_speed"] = 5
    player.weapon = Item.EMPTY
    /*val component = definitions.get(name).components?.get(componentId) ?: return@on
    val array = component.anObjectArray4758 ?: return@on
    val magicLevel = array[5] as Int

    if (!player.has(Skill.Magic, magicLevel, message = true)) {
        return@on
    }
    val requiredItems = mutableListOf<Item>()

    val item1 = Item(itemDefs.getName(array[8] as Int), array[9] as Int)
    player.inventory.indexOf(item1.name)
    if(player.inventory.remove(item1.name, item1.amount)) {

    }
    val item2 = Item(itemDefs.getName(array[10] as Int), array[11] as Int)
    val item3 = Item(itemDefs.getName(array[12] as Int), array[13] as Int)
    val item4 = Item(itemDefs.getName(array[14] as Int), array[15] as Int)*/
    player.attack(npc)
}

on<CombatSwing> { character: Character ->
    target.start("in_combat", 16, restart = true)
    if (target.inSingleCombat) {
        target.attackers.clear()
    }
    target.attackers.add(character)
}

on<CombatHit>({ it is Player && it.getVar("auto_retaliate", false) || it is NPC }) { character: Character ->
    if (character.levels.get(Skill.Constitution) <= 0) {
        return@on
    }
    delay(character, 1) {
        character.attack(source)
    }
}

fun Character.attack(target: Character) {
    val source = this
    action(ActionType.Combat) {
        source["target"] = target
        val handler = target.events.on<Character, Died> {
            source.stop("in_combat")
            cancel(ActionType.Combat)
        }
        try {
            watch(target)
            while (isActive && (source is NPC || source is Player && source.awaitDialogues())) {
                if (!withinRange(source, target)) {
                    delay()
                    continue
                }
                if (source.remaining("skilling_delay") > 0L) {
                    delay()
                    continue
                }
                if (!canAttack(source, target)) {
                    break
                }
                val swing = CombatSwing(target)
                face(target)
                events.emit(swing)
                val nextDelay = swing.delay
                if (nextDelay == null || nextDelay < 0) {
                    break
                }
                start("skilling_delay", nextDelay, quiet = true)
            }
        } finally {
            target.events.remove(handler)
            clear("target")
            watch(null)
        }
    }
}

fun withinRange(source: Character, target: Character): Boolean {
    val range = if (source is NPC) source.def["attack_range", 1] else source["attack_range", 1]
    val maxDistance = (range + if (source.attackStyle == "long_range") 2 else 0).coerceAtMost(10)
    val closeCombat = maxDistance == 1
    if (!isWithinAttackDistance(source, target, maxDistance + if (source.movement.moving) 1 else 0, closeCombat)) {
        if (source.movement.steps.isNotEmpty()) {
            return false
        }
        val strategy = CombatTargetStrategy(target, maxDistance, closeCombat)
        if (source is Player) {
            source.dialogues.clear()
        }
        source.movement.strategy = strategy
        source.movement.action = {
            if (source is Player && (source.cantReach(strategy) || source.movement.result == null)) {
                source.message("You can't reach that.")
            }
        }
        return false
    }
    return true
}