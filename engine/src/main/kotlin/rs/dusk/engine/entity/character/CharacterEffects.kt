package rs.dusk.engine.entity.character

class CharacterEffects(private val character: Character) {
    private val effects = mutableMapOf<String, Effect>()

    fun add(effect: Effect): Boolean {
        if (!effect.immune(character)) {
            val current = getOrNull(effect.type)
            if (current == null || remove(current)) {
                effect.onStart(character)
                effects[effect.type] = effect
            }
        }
        return true
    }

    fun remove(effect: Effect): Boolean {
        if (effects.remove(effect.type, effect)) {
            finish(effect)
            return true
        }
        return false
    }

    fun has(type: String): Boolean {
        return effects.containsKey(type)
    }

    fun getOrNull(type: String): Effect? {
        return effects[type]
    }

    fun remove(type: String): Boolean {
        val effect = getOrNull(type) ?: return false
        return remove(effect)
    }

    fun removeAll() {
        val iterator = effects.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            finish(next.value)
            iterator.remove()
        }
    }

    private fun finish(effect: Effect) {
        effect.onFinish(character)
    }
}
