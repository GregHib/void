package content.bot.action

import content.bot.fact.*
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards

data class BehaviourFragment(
    override val id: String,
    val type: String,
    val capacity: Int,
    val weight: Int,
    var template: String,
    override val requires: List<Condition> = emptyList(),
    override val resolve: List<Condition> = emptyList(),
    override val actions: List<BotAction> = emptyList(),
    override val produces: Set<Condition> = emptySet(),
    val fields: Map<String, Any> = emptyMap(),
) : Behaviour {
    fun resolveActions(template: BotActivity, actions: MutableList<BotAction>) {
        for (action in template.actions) {
            val resolved = when (action) {
                is BotAction.Reference -> when (val copy = action.action) {
                    is BotAction.GoTo -> BotAction.GoTo(resolve(action.references["go_to"], copy.target))
                    is BotAction.GoToNearest -> BotAction.GoToNearest(resolve(action.references["go_to_nearest"], copy.tag))
                    is BotAction.InterfaceOption -> BotAction.InterfaceOption(
                        id = resolve(action.references["interface"], copy.id),
                        option = resolve(action.references["option"], copy.option),
                    )
                    is BotAction.InteractNpc -> BotAction.InteractNpc(
                        option = resolve(action.references["option"], copy.option),
                        id = resolve(action.references["npc"], copy.id),
                        retryTicks = resolve(action.references["retry_ticks"], copy.retryTicks),
                        retryMax = resolve(action.references["retry_max"], copy.retryMax),
                        radius = resolve(action.references["radius"], copy.radius),
                    )
                    is BotAction.InteractObject -> BotAction.InteractObject(
                        option = resolve(action.references["option"], copy.option),
                        id = resolve(action.references["object"], copy.id),
                        retryTicks = resolve(action.references["retry_ticks"], copy.retryTicks),
                        retryMax = resolve(action.references["retry_max"], copy.retryMax),
                        radius = resolve(action.references["radius"], copy.radius),
                    )
                    is BotAction.WalkTo -> BotAction.WalkTo(
                        x = resolve(action.references["x"], copy.x),
                        y = resolve(action.references["y"], copy.y),
                    )
                    is BotAction.StringEntry -> BotAction.StringEntry(
                        value = resolve(action.references["value"], copy.value),
                    )
                    is BotAction.IntEntry -> BotAction.IntEntry(
                        value = resolve(action.references["value"], copy.value),
                    )
                    is BotAction.Wait -> BotAction.Wait(resolve(action.references["wait"], copy.ticks))
                    is BotAction.WaitFullInventory -> BotAction.WaitFullInventory(resolve(action.references["timeout"], copy.timeout))
                    is BotAction.Clone, is BotAction.Reference -> throw IllegalArgumentException("Invalid reference action type: ${action.action::class.simpleName}.")
                }
                is BotAction.Clone -> throw IllegalArgumentException("Unresolved clone action in template ${id}.")
                else -> action
            }
            actions.add(resolved)
        }
    }

    fun resolveRequirements(requirements: MutableList<Condition>, facts: List<Condition>) {
        for (req in facts) {
            val resolved = when (req) {
                is Condition.Reference -> {
                    val references = req.references
                    val min = resolve(references["min"], req.min)
                    val max = resolve(references["max"], req.max)
                    when (req.type) {
                        "skill" -> {
                            val id = resolve(references[req.type], req.id)
                            Condition.range(Fact.SkillLevel.of(id), min, max)
                        }
                        "carries" -> {
                            val id = resolve(references[req.type], req.id)
                            val min = resolve(references["amount"], req.min)
                            if (id.contains(",")) {
                                Condition.Any(id.split(",").map { Condition.range(Fact.InventoryCount(it), min, max) })
                            } else if (id.any { it == '*' || it == '#' }) {
                                Condition.Any(Wildcards.get(id, Wildcard.Item).map { Condition.range(Fact.InventoryCount(it), min, max) })
                            } else {
                                Condition.range(Fact.InventoryCount(id), min, max)
                            }
                        }
                        "equips" -> {
                            val id = resolve(references[req.type], req.id)
                            val min = resolve(references["amount"], req.min)
                            if (id.contains(",")) {
                                Condition.Any(id.split(",").map { Condition.range(Fact.EquipCount(id), min, max) })
                            } else if (id.any { it == '*' || it == '#' }) {
                                Condition.Any(Wildcards.get(id, Wildcard.Item).map { Condition.range(Fact.EquipCount(id), min, max) })
                            } else {
                                Condition.range(Fact.EquipCount(id), min, max)
                            }
                        }
                        "owns" -> {
                            val id = resolve(references[req.type], req.id)
                            val min = resolve(references["amount"], req.min)
                            if (id.contains(",")) {
                                Condition.Any(id.split(",").map { Condition.range(Fact.ItemCount(id), min, max) })
                            } else if (id.any { it == '*' || it == '#' }) {
                                Condition.Any(Wildcards.get(id, Wildcard.Item).map { Condition.range(Fact.ItemCount(id), min, max) })
                            } else {
                                Condition.range(Fact.ItemCount(id), min, max)
                            }
                        }
                        "variable" -> {
                            val id = resolve(references[req.type], req.id)
                            val default = resolve(references["default"], req.default)
                            when (val value = resolve(references["value"], req.value)) {
                                is Int -> Condition.Equals(Fact.IntVariable(id, default as? Int), value)
                                is String -> Condition.Equals(Fact.StringVariable(id, default as? String), value)
                                is Double -> Condition.Equals(Fact.DoubleVariable(id, default as? Double), value)
                                is Boolean -> Condition.Equals(Fact.BoolVariable(id, default as? Boolean), value)
                                else -> null
                            }
                        }
                        "inventory_space" -> {
                            val min = resolve(references["inventory_space"], req.min)
                            Condition.range(Fact.InventorySpace, min, null)
                        }
                        "location" -> {
                            val id = resolve(references["location"], req.id)
                            Condition.Area(Fact.PlayerTile, id)
                        }
                        else -> null
                    }
                }
                is Condition.Clone -> throw IllegalArgumentException("Unresolved clone requirement in template ${id}.")
                else -> req
            } ?: continue
            requirements.add(resolved)
        }
    }

    private fun String.key(): String {
        if (startsWith('$')) {
            return substring(1)
        }
        val index = indexOf($$"${")
        if (index == -1) {
            return substringAfter('$')
        }
        val end = indexOf('}', index + 2)
        return substring(index + 2, end)
    }

    private fun String.reference(): String {
        if (startsWith('$')) {
            return this
        }
        val index = indexOf($$"${")
        if (index == -1) {
            return "\$${substringAfter('$')}"
        }
        val end = indexOf('}', index) + 1
        return substring(index, end)
    }

    private fun resolve(reference: String?, default: Int): Int {
        return if (reference != null) {
            fields[reference.key()] as? Int ?: throw IllegalArgumentException("Unable to find field '$reference' in ${id}.")
        } else {
            default
        }
    }

    private fun resolve(reference: String?, default: Int?): Int? {
        return if (reference != null) {
            fields[reference.key()] as? Int ?: throw IllegalArgumentException("Unable to find field '$reference' in ${id}.")
        } else {
            default
        }
    }

    private fun resolve(reference: String?, default: String): String {
        return if (reference != null) {
            val key = reference.key()
            val value = fields[key] as? String ?: throw IllegalArgumentException("Unable to find field '$reference' in ${id}.")
            reference.replace(reference.reference(), value)
        } else {
            default
        }
    }

    private fun resolve(reference: String?, default: Any?): Any? {
        return if (reference != null) {
            fields[reference.key()] ?: throw IllegalArgumentException("Unable to find field '$reference' in ${id}.")
        } else {
            default
        }
    }
}