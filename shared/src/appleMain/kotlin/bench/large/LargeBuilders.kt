package bench.large

import androidx.room3.Room
import androidx.room3.RoomDatabase

actual fun largeMonoDbBuilder(path: String): RoomDatabase.Builder<LargeMonoDb> = Room.databaseBuilder<LargeMonoDb>(name = path)
actual fun largeContactsDbBuilder(path: String): RoomDatabase.Builder<LargeContactsDb> = Room.databaseBuilder<LargeContactsDb>(name = path)
actual fun largeCalendarDbBuilder(path: String): RoomDatabase.Builder<LargeCalendarDb> = Room.databaseBuilder<LargeCalendarDb>(name = path)
