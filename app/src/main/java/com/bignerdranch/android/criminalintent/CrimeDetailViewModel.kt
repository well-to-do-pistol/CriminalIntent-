package com.bignerdranch.android.criminalintent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import java.io.File
import java.util.UUID

class CrimeDetailViewModel() : ViewModel() { //`LiveData` 是 Android 架构组件中的一个可观察数据持有者类。
    // 它用于以生命周期敏感的方式保存数据，这意味着它尊重其他应用程序组件（例如活动、片段或服务）的生命周期。

    private val crimeRepository = CrimeRepository.get()
    private val crimeIdLiveData = MutableLiveData<UUID>()

    var crimeLiveData: LiveData<Crime?> = crimeIdLiveData.switchMap { crimeId ->
        crimeRepository.getCrime(crimeId) //每当“crimeIdLiveData”中的“UUID”发生变化时，“switchMap”就会自动使用与新“UUID”对应的“Crime”对象更新
    } //crimeLiveData代表整个crime, crimeIdLiveData代表crimeId

    fun loadCrime(crimeId: UUID) { //保存crimeId
        crimeIdLiveData.value = crimeId
    }

    fun saveCrime(crime: Crime) {
        crimeRepository.updateCrime(crime)
    }

    fun getPhotoFile(crime: Crime): File {
        return crimeRepository.getPhotoFile(crime)
    }
}
