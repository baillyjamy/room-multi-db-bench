package bench.large

import androidx.room3.RoomDatabase
import androidx.room3.useReaderConnection
import androidx.room3.withWriteTransaction
import bench.PLATFORM
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

const val CONTACTS = 20_000
const val CALENDARS = 10
const val EVENTS = 200_000
const val SYNC_CALENDAR_ID = 11L
const val SYNC_EVENTS = 5_000
const val SYNC_CONTACTS = 2_000
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

private fun attendeesOf(eventId: Long, r: Random): List<AttendeeEntity> = List(r.nextInt(6)) {
    val email = if (r.nextInt(5) == 0) "ext${r.nextInt(50_000)}@other.org" else "user${r.nextLong(1, CONTACTS + 1L)}@example.com"
    AttendeeEntity(eventId = eventId, email = email, status = r.nextInt(4))
}

/** The 5 000 events of the "sync" calendar, regenerated identically every time (stable DB size across runs). */
private fun syncBatch(): Pair<List<EventEntity>, List<AttendeeEntity>> {
    val r = Random(7)
    val events = List(SYNC_EVENTS) { event(900_001L + it, SYNC_CALENDAR_ID, r, 1_826 * DAY) }
    return events to events.flatMap { attendeesOf(it.id, r) }
}

private fun syncContacts(): List<ContactEntity> {
    return List(SYNC_CONTACTS) { contact(it * 10L + 1, generation = 1) }
}

/** Either one database (L1) or two (L2). */
class LargeHandles(val mono: LargeMonoDb?, val contactsDb: LargeContactsDb?, val calendarDb: LargeCalendarDb?) {
    val contacts: ContactDao get() = mono?.contactDao() ?: contactsDb!!.contactDao()
    val calendar: CalendarDao get() = mono?.calendarDao() ?: calendarDb!!.calendarDao()
    val contactsRoom: RoomDatabase get() = mono ?: contactsDb!!
    val calendarRoom: RoomDatabase get() = mono ?: calendarDb!!
    val all: List<RoomDatabase> get() = listOfNotNull(mono, contactsDb, calendarDb)

    suspend fun agenda(cals: List<Long>, from: Long, to: Long): List<AgendaRow> {
        mono?.let { return it.joinDao().agenda(cals, from, to) }
        // Two databases: no SQL JOIN possible -> query calendar DB, then batch lookup in contacts DB, join in memory.
        val raw = calendar.agendaRaw(cals, from, to)
        val names = HashMap<String, String>()
        raw.asSequence().map { it.attendeeEmail }.distinct().chunked(IN_CHUNK).forEach { chunk ->
            contacts.byEmails(chunk).forEach { names[it.email] = it.name }
        }
        return raw.map { AgendaRow(it.eventId, it.title, it.startMs, it.attendeeEmail, names[it.attendeeEmail]) }
    }

    fun close() = all.forEach { it.close() }
}

fun openLarge(scenario: String, dir: String): LargeHandles = when (scenario) {
    "L1" -> LargeHandles(largeMonoDbBuilder("$dir/app.db").configure().build(), null, null)
    "L2" -> LargeHandles(null, largeContactsDbBuilder("$dir/contacts.db").configure().build(), largeCalendarDbBuilder("$dir/calendar.db").configure().build())
    else -> error("unknown large scenario $scenario")
}

private suspend fun sizeBytes(db: RoomDatabase): Long = db.useReaderConnection { c ->
    val pages = c.usePrepared("PRAGMA page_count") { it.step(); it.getLong(0) }
    val size = c.usePrepared("PRAGMA page_size") { it.step(); it.getLong(0) }
    pages * size
}

private suspend fun LargeHandles.sizeMb() = all.sumOf { sizeBytes(it) } / 1_048_576.0

private suspend fun LargeHandles.doSyncContacts(batch: List<ContactEntity>) =
    contactsRoom.withWriteTransaction { contacts.upsert(batch) }

private suspend fun LargeHandles.doSyncCalendar(batch: Pair<List<EventEntity>, List<AttendeeEntity>>) =
    calendarRoom.withWriteTransaction {
        calendar.deleteAttendeesOf(SYNC_CALENDAR_ID)
        calendar.deleteEventsOf(SYNC_CALENDAR_ID)
        calendar.insertEvents(batch.first)
        calendar.insertAttendees(batch.second)
    }

suspend fun largeSeed(scenario: String, baseDir: String): String {
    val dir = "$baseDir/$scenario"
    deleteDir(dir)
    makeDir(dir)
    val h = openLarge(scenario, dir)
    val ms = timedMs {
        (1L..CONTACTS).chunked(BATCH).forEach { ids ->
            h.contactsRoom.withWriteTransaction { h.contacts.upsert(ids.map { contact(it) }) }
        }
        h.calendarRoom.withWriteTransaction {
            h.calendar.insertCalendars((1L..SYNC_CALENDAR_ID).map { CalendarEntity(it, "Calendar $it", "#${it.toString().padStart(6, '0')}") })
        }
        val re = Random(2)
        (1L..EVENTS).chunked(BATCH).forEach { ids ->
            val events = ids.map { event(it, 1 + (it % CALENDARS), re, 1_826 * DAY) }
            val attendees = events.flatMap { attendeesOf(it.id, re) }
            h.calendarRoom.withWriteTransaction {
                h.calendar.insertEvents(events)
                h.calendar.insertAttendees(attendees)
            }
        }
        h.doSyncCalendar(syncBatch())
    }
    val result = """{"mode":"large-seed","platform":"$PLATFORM","scenario":"$scenario","seedMs":$ms,""" +
        """"contacts":${h.contacts.count()},"events":${h.calendar.countEvents()},"attendees":${h.calendar.countAttendees()},"dbSizeMb":${h.sizeMb()}}"""
    h.close()
    return result
}

suspend fun largeRun(scenario: String, baseDir: String): String = coroutineScope {
    val dir = "$baseDir/$scenario"
    val contactsBatch = syncContacts()
    val calendarBatch = syncBatch()
    fullGc()
    val m0 = memory()
    var checksum = 0L
    fun mix(v: Long) { checksum = checksum * 31 + v }

    lateinit var h: LargeHandles
    val openMs = timedMs {
        h = openLarge(scenario, dir)
        mix(h.contacts.count().toLong())
        mix(h.calendar.countEvents().toLong())
    }
    fullGc()
    val mOpen = memory()

    val weekQueryMs = timedMs {
        repeat(52) { w ->
            val from = YEAR_2024 + w * 7 * DAY
            mix(h.calendar.eventsInRange(WEEK_CALS, from, from + 7 * DAY).sumOf { it.id })
            mix(h.calendar.attendeesInRange(WEEK_CALS, from, from + 7 * DAY).size.toLong())
        }
    }
    val monthQueryMs = timedMs {
        repeat(12) { m ->
            val from = YEAR_2024 + m * 30 * DAY
            mix(h.calendar.eventsInRange(ALL_CALS, from, from + 30 * DAY).size.toLong())
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
        repeat(100) { i -> mix(h.contacts.search(FIRST[i % FIRST.size].take(2 + i % 3)).sumOf { it.id }) }
    }
    val syncContactsMs = timedMs { h.doSyncContacts(contactsBatch) }
    val syncCalendarMs = timedMs { h.doSyncCalendar(calendarBatch) }
    val parallelSyncMs = timedMs {
        listOf(
            async(Dispatchers.Default) { h.doSyncContacts(contactsBatch) },
            async(Dispatchers.Default) { h.doSyncCalendar(calendarBatch) },
        ).awaitAll()
    }

    delay(300)
    fullGc()
    val mEnd = memory()
    val dbSizeMb = h.sizeMb()
    val dbCount = h.all.size
    h.close()

    buildString {
        append("""{"mode":"large-run","platform":"$PLATFORM","scenario":"$scenario","dbCount":$dbCount""")
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
    "large-seed" -> largeSeed(scenario, dir)
    "large-run" -> largeRun(scenario, dir)
    else -> error("unknown mode $mode")
}
