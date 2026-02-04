package content.bot.action

import content.bot.fact.*
import world.gregs.voidps.engine.event.Wildcard

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
                        option = resolve(action.references["option"], copy.option),
                        id = resolve(action.references["interface"], copy.id),
                    )
                    is BotAction.InteractNpc -> {
                        val option = resolve(action.references["option"], copy.option)
                        if (option == "Attack") {
                            BotAction.FightNpc(
                                id = resolve(action.references["npc"], copy.id),
                                success = resolveReference(copy.success),
                                delay = resolve(action.references["radius"], copy.delay),
                                radius = resolve(action.references["radius"], copy.radius),
                            )
                        } else {
                            BotAction.InteractNpc(
                                option = option,
                                id = resolve(action.references["npc"], copy.id),
                                success = resolveReference(copy.success),
                                delay = resolve(action.references["delay"], copy.delay),
                                radius = resolve(action.references["radius"], copy.radius),
                            )
                        }
                    }
                    is BotAction.FightNpc -> BotAction.FightNpc(
                        id = resolve(action.references["npc"], copy.id),
                        delay = resolve(action.references["delay"], copy.delay),
                        success = resolveReference(copy.success),
                        healPercentage = resolve(action.references["heal_percent"], copy.healPercentage),
                        lootOverValue = resolve(action.references["loot_over"], copy.lootOverValue),
                        radius = resolve(action.references["radius"], copy.radius),
                    )
                    is BotAction.InteractObject -> BotAction.InteractObject(
                        option = resolve(action.references["option"], copy.option),
                        id = resolve(action.references["object"], copy.id),
                        success = resolveReference(copy.success),
                        delay = resolve(action.references["delay"], copy.delay),
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
            val resolved = resolveReference(req) ?: continue
            requirements.add(resolved)
        }
    }

    private fun BehaviourFragment.resolveReference(req: Condition?): Condition? = when (req) {
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
                    Condition.split(id, min, max, Wildcard.Item) { Fact.InventoryCount(it) }
                }
                "equips" -> {
                    val id = resolve(references[req.type], req.id)
                    val min = resolve(references["amount"], req.min)
                    Condition.split(id, min, max, Wildcard.Item) { Fact.EquipCount(it) }
                }
                "owns" -> {
                    val id = resolve(references[req.type], req.id)
                    val min = resolve(references["amount"], req.min)
                    Condition.split(id, min, max, Wildcard.Item) { Fact.ItemCount(it) }
                }
                "clock" -> {
                    val id = resolve(references[req.type], req.id)
                    Condition.split(id, min, max, Wildcard.Variables) { Fact.ClockRemaining(it) }
                }
                "timer" -> {
                    val id = resolve(references[req.type], req.id)
                    val value = resolve(references["value"], req.value as? Boolean)
                    Condition.Equals(Fact.HasTimer(id), value as? Boolean ?: true)
                }
                "interface" -> {
                    val id = resolve(references[req.type], req.id)
                    val value = resolve(references["value"], req.value as? Boolean)
                    Condition.Equals(Fact.InterfaceOpen(id), value as? Boolean ?: true)
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