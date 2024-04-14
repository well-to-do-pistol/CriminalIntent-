package com.bignerdranch.android.criminalintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.android.criminalintent.database.CrimeDatabase
import com.bignerdranch.android.criminalintent.database.migration_1_2
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context) { //访问数据库的仓库, 只传了一个Context

    //启动数据库
    private val database : CrimeDatabase = Room.databaseBuilder(
        context.applicationContext, //传入应用的上下文, 因为它比任何activity都活得久
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_1_2)  //添加迁移再创建, 如果不添加,Room会删除旧的, 再创建新的, 之前的数据就没了
        .build()
    //启动Dao
    private val crimeDao = database.crimeDao() //关联Dao, Dao定义了sql语句
    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir //返回应用程序内部目录的路径，您可以在其中存储应用程序私有的文件

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes() //通过调用Dao拿到数据

    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    fun updateCrime(crime: Crime) {
        executor.execute {
            crimeDao.updateCrime(crime)
        }
    }

    fun addCrime(crime: Crime) {
        executor.execute {
            crimeDao.addCrime(crime)
        }
    }

    fun getPhotoFile(crime: Crime): File = File(filesDir, crime.photoFileName)
    //android:name="android.support.FILE_PROVIDER_PATHS" android:resource="@xml/files"/>`：
    // 指定 `FileProvider` 的元数据。 它告诉“FileProvider”在哪里可以找到描述要公开哪些文件或目录的 XML 文件

    //`files.xml`:
    //此 XML 文件定义允许“FileProvider”共享的路径。 该文件的结构和内容告诉“FileProvider”哪些文件是可共享的以及在哪个 URI 路径段下。

    //<files-path name="crime_photos" path="."/>` 元素指定哪个目录的文件是可共享的。 在这种情况下：
    //- `<files-path name="crime_photos" path="."/>`：定义内部存储中的可共享路径。
    //     - `name="crime_photos"`：路径的任意名称，用于构造 URI。 这不会影响文件系统路径，但会在提供给其他应用程序的 URI 中使用。
    //     - `path="."`：指定此规则适用的 `filesDir` 目录中的路径。 点（“.”）表示“filesDir”目录的根目录，因此它允许共享整个目录中的文件。

    companion object {
        private var INSTANCE: CrimeRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }

        fun get(): CrimeRepository {
            return INSTANCE ?:
            throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}