package rs.dusk.engine.entity.character.contain

enum class ContainerResult {
    Success,
    /**
     * Container doesn't have enough slots or stacks
     */
    Full,
    /**
     * Not enough items available
     */
    Deficient,
    /**
     * Combined item total exceeds [Int.MAX_VALUE]
     */
    Overflow,
    /**
     * Combined item total is less than container minimum amount
     */
    Underflow,
    /**
     * Item can't be added at a specific slot as the item types are different
     */
    WrongType,
    /**
     * Item can't be added at a slot as the items cannot be stacked
     */
    Unstackable,
    /**
     * Invalid slot, id or amount entered.
     */
    Invalid;
}