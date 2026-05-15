package world.gregs.voidps.engine.queue

import world.gregs.voidps.engine.entity.character.Character

class ActionList<C : Character> {
    var head: Action<C>? = null
    var tail: Action<C>? = null

    fun isEmpty() = head == null

    fun peek() = head

    fun add(action: Action<C>): Boolean {
        if (head == null) {
            head = action
            tail = action
            return true
        } else if (tail != null) {
            tail!!.next = action
            action.previous = tail
            tail = action
            return true
        }
        return false
    }

    fun remove(action: Action<C>) {
        if (action == tail) {
            tail = action.previous
        }
        if (action == head) {
            head = action.next
        }
        action.previous?.next = action.next
        action.next?.previous = action.previous
        action.next = null
        action.previous = null
    }

    private fun any(filter: (Action<C>) -> Boolean): Boolean {
        var next = head
        while (next != null) {
            if (filter(next)) {
                return true
            }
            next = next.next
        }
        return false
    }

    fun contains(name: String): Boolean = any { it.name == name }

    fun contains(priority: ActionPriority): Boolean = any { it.priority == priority }

    fun clear(name: String): Boolean {
        var next = head
        var found = false
        while (next != null) {
            if (next.name == name) {
                val skip = next.next
                remove(next)
                found = true
                next = skip
                continue
            }
            next = next.next
        }
        return found
    }

    fun clear() {
        head = null
        tail = null
    }

}
