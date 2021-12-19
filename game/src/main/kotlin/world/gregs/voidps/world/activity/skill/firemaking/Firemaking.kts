import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnInterface
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.obj.spawnObject
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.activity.skill.firemaking.fire.ColouredFire
import world.gregs.voidps.world.activity.skill.firemaking.fire.Fire

val items: FloorItems by inject()
val objects: Objects by inject()

on<InterfaceOnInterface>({ (fromItem.fireStarter && toItem.burnable) || (fromItem.burnable && toItem.burnable) }) { player: Player ->
    if (player.hasEffect("skilling_delay")) {
        return@on
    }
    val lighter = if (fromItem.fireStarter) fromItem else toItem
    val log = if (toItem.burnable) toItem else fromItem
    val logSlot = if (toItem.burnable) toSlot else fromSlot
    player.action(ActionType.FireMaking) {
        if (objects.getType(player.tile, 10) != null) {
            player.message("You can't light a fire here.")
            return@action
        }
        val fire = Fire.get(log)
        if (fire == null || !player.has(Skill.Firemaking, fire.level, true)) {
            return@action
        }

        player.message("You attempt to light to logs.", ChatType.GameFilter)
        player.setAnimation("light_fire")
        player.inventory.remove(logSlot, log.id)
        val delay = if (player.hasEffect("last_fire")) 1 else 2
        player.start("skilling_delay", delay)
        val floorItem = items.add(log.id, 1, player.tile, 60, 60, player)
        delay(delay)

        items.remove(floorItem)
        val fireId = if (fire is ColouredFire) "fire_${fire.id}" else "fire"
        val obj = spawnObject(fireId, player.tile, type = 10, rotation = 0, ticks = fire.life)
        obj.events.on<GameObject, Unregistered> {
            items.add("ashes${if (lighter.id.endsWith("dungeoneering")) "_dungeoneering" else ""}", 1, obj.tile, 60, 60, player)
        }
        player.movement.set(obj.interactTarget, true) {
            player.face(obj)
        }

        player.exp(Skill.Firemaking, fire.xp)
        player.start("last_fire", 3)
        await<Unit>(Suspension.Movement)
    }
}

val Item.fireStarter: Boolean
    get() = id.startsWith("tinderbox")

val Item.burnable: Boolean
    get() = id.endsWith("logs") || id.endsWith("branches")