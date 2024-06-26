package io.tashtabash.sim.event


class EventLog(private val isPurging: Boolean = true, private val isOblivious: Boolean = true) {
    //Stores some last events, it is not guarantied, how many of them are stored.
    private var _lastEvents = mutableListOf<Event>()
    val lastEvents: List<Event>
        get() = _lastEvents

    private val _newEvents = mutableListOf<Event>()
    val newEvents: List<Event>
        get() = _newEvents

    private val maxEvents = 10000

    // Uncomment this fun and its calls in add if logging is necessary
//    private fun printImportantEvents(events: List<Event>) {
//        for (event in events)
//            if (event.toString().contains("PLACEHOLDER"))
//                println(event)
//    }

    fun add(event: Event) {
        _newEvents.add(event)
        if (!isOblivious)
            _lastEvents.add(event)

//        printImportantEvents(listOf(event))
    }

    fun addAll(events: List<Event>) {
        _newEvents.addAll(events)

        if (!isOblivious)
            _lastEvents.addAll(events)

//        printImportantEvents(events)
    }

    operator fun plusAssign(event: Event) = add(event)
    operator fun plusAssign(events: List<Event>) = addAll(events)

    fun clearNewEvents() = _newEvents.clear()

    fun joinNewEvents(log: EventLog) {
        addAll(log.newEvents)
        log.clearNewEvents()

        if (isPurging && _lastEvents.size >= maxEvents) {
            _lastEvents = _lastEvents
                    .takeLast(maxEvents / 2)
                    .toMutableList()
        }
    }
}
