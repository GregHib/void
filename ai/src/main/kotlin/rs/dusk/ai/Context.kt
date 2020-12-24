package rs.dusk.ai

/**
 * Context for the [DecisionMaker]
 * Note: Should include the acting entity where applicable
 */
interface Context {
    var last: Decision?
}