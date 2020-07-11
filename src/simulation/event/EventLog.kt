package simulation.event

class EventLog {
    private val _allEvents = mutableListOf<Event>()
    val allEvents: List<Event>
        get() = _allEvents

    private val _newEvents = mutableListOf<Event>()
    val newEvents: List<Event>
        get() = _newEvents

    fun addEvent(event: Event) {
        _newEvents.add(event)
        _allEvents.add(event)
    }

    fun addEvents(events: List<Event>) {
        _newEvents.addAll(events)
        _allEvents.addAll(events)
    }

    fun clearNewEvents() = _newEvents.clear()

    fun joinNewEvents(log: EventLog) {
        addEvents(log.newEvents)
        log.clearNewEvents()
    }
}
