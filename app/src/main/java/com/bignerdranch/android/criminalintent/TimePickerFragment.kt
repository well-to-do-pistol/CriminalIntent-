package com.bignerdranch.android.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.UUID

private const val ARG_TIME = "time"
class TimePickerFragment: DialogFragment() {

    interface Callbacks {
        fun onTimeSelected(date: Date)
    } //回调接口, 为了给创建自己实例的人数据

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //dateListener` 是 `DatePickerDialog.OnDateSetListener` 的一个实例，它是 Android SDK 中的一个接口。
        // 该接口需要实现方法“onDateSet(DatePicker view, intyear,intmonth,intday)”，
        val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute -> //第一个参数指确定日期的DatePicker, _指不需要
            val date = arguments?.getSerializable(ARG_TIME) as Date? // Retrieve the passed date
            val calendar = Calendar.getInstance()
            if (date != null) {
                calendar.time = date
            }
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)

            // Now calendar.time has the same year, month, and day as the original date but with the user-selected hour and minute.
            val resultTime = calendar.time

            targetFragment?.let { fragment ->
                (fragment as Callbacks).onTimeSelected(resultTime)
            }
        }
//        val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
//            // Handle the time chosen by the user
//            val resultTime: Date = GregorianCalendar(0, 0, 0, hourOfDay, minute).time //直接设置为零的话会出bug
//
//            targetFragment?.let { fragment ->
//                (fragment as Callbacks).onTimeSelected(resultTime)
//            }
//        }
        val date = arguments?.getSerializable(ARG_TIME) as Date //拿到传来的date
        val calendar = Calendar.getInstance()
        calendar.time = date
        val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = calendar.get(Calendar.MINUTE)
        return TimePickerDialog(
                requireContext(),
                timeListener,
                initialHour,
                initialMinute,
                DateFormat.is24HourFormat(requireContext())
            )
    }

    companion object {
        fun newInstance(date: Date): TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_TIME, date)
            }

            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }
}