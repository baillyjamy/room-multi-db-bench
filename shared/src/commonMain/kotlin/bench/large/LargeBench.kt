package bench.large

import androidx.room3.RoomDatabase
import androidx.room3.useReaderConnection
import androidx.room3.withWriteTransaction
import bench.bareScenario
import bench.dataDir
import bench.header
import bench.configure
import bench.deleteDir
import bench.fullGc
import bench.makeDir
import bench.memory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.TimeSource

/*
 * "Large" suite: a realistic calendar dataset. Same data, same queries, two layouts:
 *  L1 = one database (contacts + calendars + events + attendees)
 *  L2 = two databases (contacts DB + calendar DB), joined in memory.
 * All data is generated from fixed seeds, so both layouts must return identical results (checksum).
 */

/** Volume of the calendar suite. Same schema, queries and seeds; only the row counts change. */
class Volume(val mode: String, val contacts: Int, val events: Int, val syncEvents: Int, val syncContacts: Int)

/** ~120 MB: a heavy calendar account. */
val LARGE = Volume("large", contacts = 20_000, events = 200_000, syncEvents = 5_000, syncContacts = 2_000)
/** ~12 MB: a modest account, 10x less data (same order of magnitude as the light S1-S10 suite). */
val LIGHT = Volume("light", contacts = 2_000, events = 20_000, syncEvents = 500, syncContacts = 200)

const val CALENDARS = 10
const val SYNC_CALENDAR_ID = 11L
const val BATCH = 5_000
const val IN_CHUNK = 900

private const val DAY = 86_400_000L
private const val T0 = 1_640_995_200_000L // 2022-01-01T00:00:00Z
private const val YEAR_2024 = T0 + 730 * DAY
private val ALL_CALS = (1L..CALENDARS).toList()
private val WEEK_CALS = listOf(1L, 2L, 3L)

private val FIRST = listOf("Alice", "Bruno", "Chloé", "David", "Emma", "Félix", "Gaëlle", "Hugo", "Inès", "Jules", "Karim", "Léa", "Marc", "Nina", "Olivier", "Paula", "Quentin", "Rosa", "Samuel", "Théa", "Ugo", "Vera", "William", "Yasmine", "Zoé")
private val LAST = listOf("Martin", "Bernard", "Dubois", "Durand", "Lefebvre", "Moreau", "Laurent", "Simon", "Michel", "Garcia", "Roux", "Fournier", "Girard", "Bonnet", "Dupont", "Lambert", "Fontaine", "Rousseau", "Vincent", "Muller")
private val WORDS = "réunion point projet revue sprint client déjeuner appel démo atelier planning budget équipe produit design support formation entretien bilan comité".split(' ')

private inline fun timedMs(block: () -> Unit): Double {
    val mark = TimeSource.Monotonic.markNow()
    block()
    return mark.elapsedNow().inWholeMicroseconds / 1000.0
}

private fun sentence(r: Random, words: Int) = List(words) { WORDS[r.nextInt(WORDS.size)] }.joinToString(" ")

/** Deterministic per id: a re-synced contact keeps its name/email, only `notes` changes. */
fun contact(id: Long, generation: Int = 0): ContactEntity {
    val r = Random(id)
    return ContactEntity(
        id = id,
        accountId = 1,
        name = "${FIRST[r.nextInt(FIRST.size)]} ${LAST[r.nextInt(LAST.size)]} $id",
        email = "user$id@example.com",
        phone = "+41 79 ${r.nextInt(100, 999)} ${r.nextInt(10, 99)} ${r.nextInt(10, 99)}",
        organization = "Org ${r.nextInt(500)}",
        notes = sentence(r, 8) + " v$generation",
    )
}

private fun event(id: Long, calendarId: Long, r: Random, span: Long): EventEntity {
    val start = T0 + (r.nextLong(span) / (15 * 60_000L)) * 15 * 60_000L
    return EventEntity(
        id = id,
        calendarId = calendarId,
        uid = "uid-$id@example.com",
        title = sentence(r, 3),
        location = "Salle ${r.nextInt(1, 40)}",
        description = sentence(r, 20 + r.nextInt(30)),
        startMs = start,
        endMs = start + (1 + r.nextInt(8)) * 15 * 60_000L,
        rrule = if (r.nextInt(10) == 0) "FREQ=WEEKLY;COUNT=10" else null,
        updatedAt = start,
    )
}

private fun attendeesOf(v: Volume, eventId: Long, r: Random): List<AttendeeEntity> = List(r.nextInt(6)) {
    val email = if (r.nextInt(5) == 0) "ext${r.nextInt(50_000)}@other.org" else "user${r.nextLong(1, v.contacts + 1L)}@example.com"
    AttendeeEntity(eventId = eventId, email = email, status = r.nextInt(4))
}

/** The events of the "sync" calendar (5 000 in LARGE), regenerated identically every time (stable DB size across runs). */
private fun syncBatch(v: Volume): Pair<List<EventEntity>, List<AttendeeEntity>> {
    val r = Random(7)
    val events = List(v.syncEvents) { event(900_001L + it, SYNC_CALENDAR_ID, r, 1_826 * DAY) }
    return events to events.flatMap { attendeesOf(v, it.id, r) }
}

private fun syncContacts(v: Volume): List<ContactEntity> {
    return List(v.syncContacts) { contact(it * 10L + 1, generation = 1) }
}

/** Same data, same queries, whatever the library. L1 = one database file, L2 = contacts file + calendar file. */
interface LargeStore {
    val dbCount: Int
    suspend fun countContacts(): Int
    suspend fun countEvents(): Int
    suspend fun countAttendees(): Int
    /** Each write method below runs in one write transaction on the database that owns the data. */
    suspend fun upsertContacts(batch: List<ContactEntity>)
    suspend fun insertCalendars(calendars: List<CalendarEntity>)
    suspend fun insertEvents(events: List<EventEntity>, attendees: List<AttendeeEntity>)
    suspend fun replaceCalendar(calendarId: Long, events: List<EventEntity>, attendees: List<AttendeeEntity>)
    suspend fun eventsInRange(cals: List<Long>, from: Long, to: Long): List<EventEntity>
    suspend fun attendeesInRange(cals: List<Long>, from: Long, to: Long): List<AttendeeEntity>
    suspend fun agenda(cals: List<Long>, from: Long, to: Long): List<AgendaRow>
    suspend fun searchContacts(prefix: String): List<ContactEntity>
    suspend fun sizeMb(): Double
    suspend fun sqliteVersion(): String
    fun close()
}

/** Two databases: no SQL JOIN possible -> calendar query, then batched lookup in the contacts DB, join in memory. */
suspend fun joinInMemory(raw: List<AgendaRawRow>, byEmails: suspend (List<String>) -> List<ContactEntity>): List<AgendaRow> {
    val names = HashMap<String, String>()
    raw.asSequence().map { it.attendeeEmail }.distinct().chunked(IN_CHUNK).forEach { chunk ->
        byEmails(chunk).forEach { names[it.email] = it.name }
    }
    return raw.map { AgendaRow(it.eventId, it.title, it.startMs, it.attendeeEmail, names[it.attendeeEmail]) }
}

class RoomLargeStore(private val mono: LargeMonoDb?, private val contactsDb: LargeContactsDb?, private val calendarDb: LargeCalendarDb?) : LargeStore {
    private val contacts: ContactDao get() = mono?.contactDao() ?: contactsDb!!.contactDao()
    private val calendar: CalendarDao get() = mono?.calendarDao() ?: calendarDb!!.calendarDao()
    private val contactsRoom: RoomDatabase get() = mono ?: contactsDb!!
    private val calendarRoom: RoomDatabase get() = mono ?: calendarDb!!
    private val all: List<RoomDatabase> get() = listOfNotNull(mono, contactsDb, calendarDb)
    override val dbCount get() = all.size

    override suspend fun countContacts() = contacts.count()
    override suspend fun countEvents() = calendar.countEvents()
    override suspend fun countAttendees() = calendar.countAttendees()
    override suspend fun upsertContacts(batch: List<ContactEntity>) = contactsRoom.withWriteTransaction { contacts.upsert(batch) }
    override suspend fun insertCalendars(calendars: List<CalendarEntity>) = calendarRoom.withWriteTransaction { calendar.insertCalendars(calendars) }
    override suspend fun insertEvents(events: List<EventEntity>, attendees: List<AttendeeEntity>) = calendarRoom.withWriteTransaction {
        calendar.insertEvents(events)
        calendar.insertAttendees(attendees)
    }
    override suspend fun replaceCalendar(calendarId: Long, events: List<EventEntity>, attendees: List<AttendeeEntity>) =
        calendarRoom.withWriteTransaction {
            calendar.deleteAttendeesOf(calendarId)
            calendar.deleteEventsOf(calendarId)
            calendar.insertEvents(events)
            calendar.insertAttendees(attendees)
        }
    override suspend fun eventsInRange(cals: List<Long>, from: Long, to: Long) = calendar.eventsInRange(cals, from, to)
    override suspend fun attendeesInRange(cals: List<Long>, from: Long, to: Long) = calendar.attendeesInRange(cals, from, to)
    override suspend fun agenda(cals: List<Long>, from: Long, to: Long): List<AgendaRow> =
        mono?.joinDao()?.agenda(cals, from, to) ?: joinInMemory(calendar.agendaRaw(cals, from, to)) { contacts.byEmails(it) }
    override suspend fun searchContacts(prefix: String) = contacts.search(prefix)
    override suspend fun sizeMb() = all.sumOf { sizeBytes(it) } / 1_048_576.0
    override suspend fun sqliteVersion() = all.first().useReaderConnection { c -> c.usePrepared("SELECT sqlite_version()") { it.step(); it.getText(0) } }
    override fun close() = all.forEach { it.close() }
}

fun openLarge(scenario: String, dir: String): LargeStore = when (scenario) {
    "L1" -> RoomLargeStore(largeMonoDbBuilder("$dir/app.db").configure().build(), null, null)
    "L2" -> RoomLargeStore(null, largeContactsDbBuilder("$dir/contacts.db").configure().build(), largeCalendarDbBuilder("$dir/calendar.db").configure().build())
    "sqldelight:L1", "sqldelight:L2" -> bench.sqldelight.openSqLarge(bareScenario(scenario), dir)
    else -> error("unknown large scenario $scenario")
}

private suspend fun sizeBytes(db: RoomDatabase): Long = db.useReaderConnection { c ->
    val pages = c.usePrepared("PRAGMA page_count") { it.step(); it.getLong(0) }
    val size = c.usePrepared("PRAGMA page_size") { it.step(); it.getLong(0) }
    pages * size
}

suspend fun largeSeed(v: Volume, scenario: String, baseDir: String): String {
    val dir = dataDir("$baseDir/${v.mode}", scenario)
    deleteDir(dir)
    makeDir(dir)
    val h = openLarge(scenario, dir)
    val ms = timedMs {
        (1L..v.contacts).chunked(BATCH).forEach { ids -> h.upsertContacts(ids.map { contact(it) }) }
        h.insertCalendars((1L..SYNC_CALENDAR_ID).map { CalendarEntity(it, "Calendar $it", "#${it.toString().padStart(6, '0')}") })
        val re = Random(2)
        (1L..v.events).chunked(BATCH).forEach { ids ->
            val events = ids.map { event(it, 1 + (it % CALENDARS), re, 1_826 * DAY) }
            val attendees = events.flatMap { attendeesOf(v, it.id, re) }
            h.insertEvents(events, attendees)
        }
        syncBatch(v).let { (e, a) -> h.replaceCalendar(SYNC_CALENDAR_ID, e, a) }
    }
    val result = header("${v.mode}-seed", scenario) + ""","seedMs":$ms,""" +
        """"contacts":${h.countContacts()},"events":${h.countEvents()},"attendees":${h.countAttendees()},"dbSizeMb":${h.sizeMb()}}"""
    h.close()
    return result
}

suspend fun largeRun(v: Volume, scenario: String, baseDir: String): String = coroutineScope {
    val dir = dataDir("$baseDir/${v.mode}", scenario)
    val contactsBatch = syncContacts(v)
    val calendarBatch = syncBatch(v)
    fullGc()
    val m0 = memory()
    var checksum = 0L
    fun mix(v: Long) { checksum = checksum * 31 + v }

    lateinit var h: LargeStore
    val openMs = timedMs {
        h = openLarge(scenario, dir)
        mix(h.countContacts().toLong())
        mix(h.countEvents().toLong())
    }
    fullGc()
    val mOpen = memory()

    val weekQueryMs = timedMs {
        repeat(52) { w ->
            val from = YEAR_2024 + w * 7 * DAY
            mix(h.eventsInRange(WEEK_CALS, from, from + 7 * DAY).sumOf { it.id })
            mix(h.attendeesInRange(WEEK_CALS, from, from + 7 * DAY).size.toLong())
        }
    }
    val monthQueryMs = timedMs {
        repeat(12) { m ->
            val from = YEAR_2024 + m * 30 * DAY
            mix(h.eventsInRange(ALL_CALS, from, from + 30 * DAY).size.toLong())
        }
    }
    val agendaJoinMs = timedMs {
        repeat(12) { m ->
            val from = YEAR_2024 + m * 30 * DAY
            val rows = h.agenda(ALL_CALS, from, from + 30 * DAY)
            mix(rows.size.toLong())
            mix(rows.sumOf { (it.contactName?.length ?: 0).toLong() + it.eventId })
        }
    }
    val searchContactsMs = timedMs {
        repeat(100) { i -> mix(h.searchContacts(FIRST[i % FIRST.size].take(2 + i % 3)).sumOf { it.id }) }
    }
    val syncContactsMs = timedMs { h.upsertContacts(contactsBatch) }
    val syncCalendarMs = timedMs { h.replaceCalendar(SYNC_CALENDAR_ID, calendarBatch.first, calendarBatch.second) }
    val parallelSyncMs = timedMs {
        listOf(
            async(Dispatchers.Default) { h.upsertContacts(contactsBatch) },
            async(Dispatchers.Default) { h.replaceCalendar(SYNC_CALENDAR_ID, calendarBatch.first, calendarBatch.second) },
        ).awaitAll()
    }

    delay(300)
    fullGc()
    val mEnd = memory()
    val dbSizeMb = h.sizeMb()
    val dbCount = h.dbCount
    val sqlite = h.sqliteVersion()
    h.close()

    buildString {
        append(header("${v.mode}-run", scenario) + ""","dbCount":$dbCount,"sqlite":"$sqlite"""")
        append(""","openMs":$openMs,"weekQueryMs":$weekQueryMs,"monthQueryMs":$monthQueryMs,"agendaJoinMs":$agendaJoinMs""")
        append(""","searchContactsMs":$searchContactsMs,"syncContactsMs":$syncContactsMs,"syncCalendarMs":$syncCalendarMs,"parallelSyncMs":$parallelSyncMs""")
        append(""","checksum":$checksum,"dbSizeMb":$dbSizeMb""")
        m0.keys.forEach { k ->
            append(""","${k}OpenKb":${(mOpen.getValue(k) - m0.getValue(k)) / 1024}""")
            append(""","${k}EndKb":${(mEnd.getValue(k) - m0.getValue(k)) / 1024}""")
        }
        append("}")
    }
}

suspend fun dispatch(mode: String, scenario: String, dir: String): String = when (mode) {
    "seed" -> bench.seed(scenario, dir)
    "run" -> bench.run(scenario, dir)
    "large-seed" -> largeSeed(LARGE, scenario, dir)
    "large-run" -> largeRun(LARGE, scenario, dir)
    "light-seed" -> largeSeed(LIGHT, scenario, dir)
    "light-run" -> largeRun(LIGHT, scenario, dir)
    else -> error("unknown mode $mode")
}
