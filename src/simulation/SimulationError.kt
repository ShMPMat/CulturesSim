package simulation

import java.lang.Error


open class SimulationError(override val message: String): Error(message)


open class DataInitializationError(override val message: String): SimulationError(message)
