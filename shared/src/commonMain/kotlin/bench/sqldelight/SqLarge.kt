package bench.sqldelight

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.SqlDriver
import bench.large.AgendaRawRow
import bench.large.AgendaRow
import bench.large.AttendeeEntity
import bench.large.CalendarEntity
import bench.large.ContactEntity
import bench.large.EventEntity
import bench.large.LargeStore
import bench.large.joinInMemory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/** Queries of Contacts.sq, independent of the generated package (L1 and L2 generate distinct classes). */
class SqContacts(
    val upsert: (ContactEntity) -> Unit,
    val count: () -> Long,
    val byEmails: (Collection<String>) -> List<ContactEntity>,
    val search: (String) -> List<ContactEntity>,
)

/** Queries of Calendar.sq. */
class SqCalendar(
    val insertCalendar: (CalendarEntity) -> Unit,
    val insertEvent: (EventEntity) -> Unit,
    val insertAttendee: (AttendeeEntity) -> Unit,
    val countEvents: () -> Long,
    val countAttendees: () -> Long,
    val eventsInRange: (Collection<Long>, Long, Long) -> List<EventEntity>,
    val attendeesInRange: (Collection<Long>, Long, Long) -> List<AttendeeEntity>,
    val agendaRaw: (Collection<Long>, Long, Long) -> List<AgendaRawRow>,
    val deleteAttendeesOf: (Long) -> Unit,
    val deleteEventsOf: (Long) -> Unit,
)

class SqLargeStore(
    private val drivers: List<SqlDriver>,
    private val contactsDb: Transacter,
    private val calendarDb: Transacter,
    private val contacts: SqContacts,
    private val calendar: SqCalendar,
    /** L1 only: the SQL JOIN with contacts. */
    private val joinAgenda: ((Collection<Long>, Long, Long) -> List<AgendaRow>)?,
) : LargeStore {
    override val dbCount get() = drivers.size
    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    override suspend fun countContacts() = io { contacts.count().toInt() }
    override suspend fun countEvents() = io { calendar.countEvents().toInt() }
    override suspend fun countAttendees() = io { calendar.countAttendees().toInt() }
    override suspend fun upsertContacts(batch: List<ContactEntity>) = contactsDb.writeTx { batch.forEach(contacts.upsert) }
    override suspend fun insertCalendars(calendars: List<CalendarEntity>) = calendarDb.writeTx { calendars.forEach(calendar.insertCalendar) }
    override suspend fun insertEvents(events: List<EventEntity>, attendees: List<AttendeeEntity>) = calendarDb.writeTx {
        events.forEach(calendar.insertEvent)
        attendees.forEach(calendar.insertAttendee)
    }
    override suspend fun replaceCalendar(calendarId: Long, events: List<EventEntity>, attendees: List<AttendeeEntity>) = calendarDb.writeTx {
        calendar.deleteAttendeesOf(calendarId)
        calendar.deleteEventsOf(calendarId)
        events.forEach(calendar.insertEvent)
        attendees.forEach(calendar.insertAttendee)
    }
    override suspend fun eventsInRange(cals: List<Long>, from: Long, to: Long) = io { calendar.eventsInRange(cals, from, to) }
    override suspend fun attendeesInRange(cals: List<Long>, from: Long, to: Long) = io { calendar.attendeesInRange(cals, from, to) }
    override suspend fun agenda(cals: List<Long>, from: Long, to: Long): List<AgendaRow> =
        joinAgenda?.let { io { it(cals, from, to) } }
            ?: joinInMemory(io { calendar.agendaRaw(cals, from, to) }) { chunk -> io { contacts.byEmails(chunk) } }
    override suspend fun searchContacts(prefix: String) = io { contacts.search(prefix) }
    override suspend fun sizeMb() = io { drivers.sumOf { it.sizeBytes() } / 1_048_576.0 }
    override suspend fun sqliteVersion() = io { drivers.first().sqliteVersion() }
    override fun close() = drivers.forEach { it.close() }
}

fun openSqLarge(scenario: String, dir: String): LargeStore = when (scenario) {
    "L1" -> {
        val d = createDriver(bench.sq.largemono.SqLargeMono.Schema, "$dir/app.db")
        val db = bench.sq.largemono.SqLargeMono(d)
        SqLargeStore(listOf(d), db, db, db.contactsQueries.adapt(), db.calendarQueries.adapt()) { cals, from, to ->
            db.joinQueries.agenda(cals, from, to, ::AgendaRow).executeAsList()
        }
    }
    "L2" -> {
        val dc = createDriver(bench.sq.largecontacts.SqLargeContacts.Schema, "$dir/contacts.db")
        val dk = createDriver(bench.sq.largecalendar.SqLargeCalendar.Schema, "$dir/calendar.db")
        val contacts = bench.sq.largecontacts.SqLargeContacts(dc)
        val calendar = bench.sq.largecalendar.SqLargeCalendar(dk)
        SqLargeStore(listOf(dc, dk), contacts, calendar, contacts.contactsQueries.adapt(), calendar.calendarQueries.adapt(), null)
    }
    else -> error("unknown large scenario $scenario")
}
