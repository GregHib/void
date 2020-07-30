package rs.dusk.engine.entity.character.contain

sealed class ContainerResult {
    sealed class Addition : ContainerResult() {
        object Added : Addition()
        sealed class Failure : Addition() {
            object Full : Failure()
            object Overflow : Failure()
            object WrongType : Failure()
            object Unstackable : Failure()
            object Invalid : Failure()

        }
    }
    sealed class Removal : ContainerResult() {
        object Removed : Removal()
        sealed class Failure : Removal() {
            object Deficient : Failure()
            object Underflow : Failure()
            object WrongType : Failure()
            object Unstackable : Failure()
            object Invalid : Failure()
        }
    }
}