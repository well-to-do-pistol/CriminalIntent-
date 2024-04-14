package com.bignerdranch.android.criminalintent

import android.app.Application
//没有复杂生命周期, 应用销毁就销毁
//需要登记
class CriminalIntentApplication : Application() { //应用启动类, 启动了仓库类

    override fun onCreate() {
        super.onCreate()
        CrimeRepository.initialize(this)
    }
}