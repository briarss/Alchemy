package aster.amo.alchemy.config

class AlchemyConfig(
    var debug: Boolean = false
) {
    override fun toString(): String {
        return "AlchemyConfig(debug=$debug)"
    }
}
