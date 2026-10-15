package bench.large

import androidx.room3.*

@Entity(tableName = "contacts", indices = [Index("email"), Index("name")])
data class ContactEntity(
    @PrimaryKey val id: Long,
    val accountId: Long,
    val name: String,
    val email: String,
    val phone: String,
    val organization: String,
    val notes: String,
)

@Entity(tableName = "calendars")
data class CalendarEntity(@PrimaryKey val id: Long, val name: String, val color: String)

@Entity(tableName = "events", indices = [Index("calendarId", "startMs")])
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val calendarId: Long,
    val uid: String,
    val title: String,
    val location: String,
    val description: String,
    val startMs: Long,
    val endMs: Long,
    val rrule: String?,
    val updatedAt: Long,
)

@Entity(tableName = "attendees", indices = [Index("eventId"), Index("email")])
data class AttendeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val email: String,
    val status: Int,
)

data class AgendaRawRow(val eventId: Long, val title: String, val startMs: Long, val attendeeEmail: String)

data class AgendaRow(val eventId: Long, val title: String, val startMs: Long, val attendeeEmail: String, val contactName: String?)

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(contacts: List<ContactEntity>)
    @Query("SELECT COUNT(*) FROM contacts") suspend fun count(): Int
    @Query("SELECT * FROM contacts WHERE email IN (:emails)") suspend fun byEmails(emails: List<String>): List<ContactEntity>
    @Query("SELECT * FROM contacts WHERE name LIKE :prefix || '%' ORDER BY name LIMIT 50") suspend fun search(prefix: String): List<ContactEntity>
}

@Dao
interface CalendarDao {
    @Insert suspend fun insertCalendars(calendars: List<CalendarEntity>)
    @Insert suspend fun insertEvents(events: List<EventEntity>): List<Long>
    @Insert suspend fun insertAttendees(attendees: List<AttendeeEntity>)
    @Query("SELECT COUNT(*) FROM events") suspend fun countEvents(): Int
    @Query("SELECT COUNT(*) FROM attendees") suspend fun countAttendees(): Int

    @Query("SELECT * FROM events WHERE calendarId IN (:cals) AND startMs > :from - 86400000 AND startMs < :to AND endMs > :from ORDER BY startMs")
    suspend fun eventsInRange(cals: List<Long>, from: Long, to: Long): List<EventEntity>

    @Query(
        "SELECT a.* FROM attendees a JOIN events e ON a.eventId = e.id " +
            "WHERE e.calendarId IN (:cals) AND e.startMs > :from - 86400000 AND e.startMs < :to AND e.endMs > :from"
    )
    suspend fun attendeesInRange(cals: List<Long>, from: Long, to: Long): List<AttendeeEntity>

    @Query(
        "SELECT e.id AS eventId, e.title, e.startMs, a.email AS attendeeEmail FROM events e " +
            "JOIN attendees a ON a.eventId = e.id " +
            "WHERE e.calendarId IN (:cals) AND e.startMs > :from - 86400000 AND e.startMs < :to AND e.endMs > :from ORDER BY e.startMs, a.id"
    )
    suspend fun agendaRaw(cals: List<Long>, from: Long, to: Long): List<AgendaRawRow>

    @Query("DELETE FROM attendees WHERE eventId IN (SELECT id FROM events WHERE calendarId = :calendarId)")
    suspend fun deleteAttendeesOf(calendarId: Long)
    @Query("DELETE FROM events WHERE calendarId = :calendarId") suspend fun deleteEventsOf(calendarId: Long)
}

/** Only possible when contacts and events live in the same database. */
@Dao
interface JoinDao {
    @Query(
        "SELECT e.id AS eventId, e.title, e.startMs, a.email AS attendeeEmail, c.name AS contactName FROM events e " +
            "JOIN attendees a ON a.eventId = e.id LEFT JOIN contacts c ON c.email = a.email " +
            "WHERE e.calendarId IN (:cals) AND e.startMs > :from - 86400000 AND e.startMs < :to AND e.endMs > :from ORDER BY e.startMs, a.id"
    )
    suspend fun agenda(cals: List<Long>, from: Long, to: Long): List<AgendaRow>
}

@Database(entities = [ContactEntity::class, CalendarEntity::class, EventEntity::class, AttendeeEntity::class], version = 1, exportSchema = false)
@ConstructedBy(LargeMonoDbCtor::class)
abstract class LargeMonoDb : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun calendarDao(): CalendarDao
    abstract fun joinDao(): JoinDao
}

@Database(entities = [ContactEntity::class], version = 1, exportSchema = false)
@ConstructedBy(LargeContactsDbCtor::class)
abstract class LargeContactsDb : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}

@Database(entities = [CalendarEntity::class, EventEntity::class, AttendeeEntity::class], version = 1, exportSchema = false)
@ConstructedBy(LargeCalendarDbCtor::class)
abstract class LargeCalendarDb : RoomDatabase() {
    abstract fun calendarDao(): CalendarDao
}

@Suppress("KotlinNoActualForExpect")
expect object LargeMonoDbCtor : RoomDatabaseConstructor<LargeMonoDb> { override fun initialize(): LargeMonoDb }
@Suppress("KotlinNoActualForExpect")
expect object LargeContactsDbCtor : RoomDatabaseConstructor<LargeContactsDb> { override fun initialize(): LargeContactsDb }
@Suppress("KotlinNoActualForExpect")
expect object LargeCalendarDbCtor : RoomDatabaseConstructor<LargeCalendarDb> { override fun initialize(): LargeCalendarDb }

expect fun largeMonoDbBuilder(path: String): RoomDatabase.Builder<LargeMonoDb>
expect fun largeContactsDbBuilder(path: String): RoomDatabase.Builder<LargeContactsDb>
expect fun largeCalendarDbBuilder(path: String): RoomDatabase.Builder<LargeCalendarDb>
