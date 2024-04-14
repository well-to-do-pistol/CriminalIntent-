package com.bignerdranch.android.criminalintent

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

//然而，Room 在从数据库重建对象时需要一种方法来设置此字段，如果它被标记为“final”，
// Room 无法为其提供值，因为这意味着需要一个不支持的 setter 方法。 Final 字段不存在。
@Entity
data class Crime(@PrimaryKey var id: UUID = UUID.randomUUID(), //只能改成var, 我也不知道为什么报错
                 var title: String = "",
                 var date: Date = Date(),
                 var isSolved: Boolean = false,
                 var suspect: String = "",
                 var suspectPhoneNumber: String = ""){ //需要增加版本号

    val photoFileName
        get() = "IMG_$id.jpg"
}

//@Entity
//class Crime(@PrimaryKey @ColumnInfo(name = "id") private var _id: UUID = UUID.randomUUID(),
//            var title: String = "",
//            var date: Date = Date(),
//            var isSolved: Boolean = false) {
//    var id: UUID
//        get() = _id
//        set(value) {
//            _id = value
//        }
//}
