package dev.maharshpatel.atmapp.database

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.maharshpatel.atmapp.database.dao.ATMDao
import dev.maharshpatel.atmapp.models.ATMBalance
import dev.maharshpatel.atmapp.models.SessionInfo
import dev.maharshpatel.atmapp.models.TransactionHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    version = 1,
    exportSchema = false,
    entities = [ATMBalance::class, SessionInfo::class, TransactionHistory::class]
)
abstract class MainDatabase : RoomDatabase() {

    companion object {

        @Volatile
        private var mainDB: MainDatabase? = null

        fun getDatabase(application: Application) =
            synchronized(this) {
                mainDB ?: createDB(application).also { mainDB = it }
            }

        private fun createDB(application: Application): MainDatabase =
            Room.databaseBuilder(
                application,
                MainDatabase::class.java,
                "MainDatabase"
            ).addCallback(DatabaseCallback()).build()
    }

    abstract fun getATMDao(): ATMDao

    class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)


            CoroutineScope(Dispatchers.IO).launch {
                val currencyInATM = buildMap {
                    put(100, 20)
                    put(200, 20)
                    put(500, 12)
                    put(2000, 4)
                }
                val atmDao = mainDB?.getATMDao()

                for ((k, v) in currencyInATM) {
                    atmDao?.loadMoneyToATM(
                        ATMBalance(
                            noteValue = k,
                            notesQuantity = v
                        )
                    )
                }
            }
        }
    }


}