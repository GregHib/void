package rs.dusk.engine.entity.character.contain

enum class ContainerResult {
    /**
     * Transaction successful
     */
    Success,
    /**
     * Container doesn't have enough slots, stacks or would exceed [Int.MAX_VALUE]
     */
    Full,
    /**
     * Not enough items available stacked or otherwise
     */
    Deficient,
    /**
     * Combined values exceed [Int.MAX_VALUE]
     */
    Overflow,
    /**
     * Item can't be added to slot as the item types are different
     */
    WrongType,
    /**
     * Item can't be added to slot as the items cannot be stacked
     */
    Unstackable,
    /**
     * Invalid slot, id or amount entered.
     */
    Invalid;
}