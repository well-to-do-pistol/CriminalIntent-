package com.bignerdranch.android.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

private const val ARG_DATE = "date"

//DialogFragment用来放置日期
class DatePickerFragment : DialogFragment() {

    interface Callbacks {
        fun onDateSelected(date: Date)
    } //回调接口, 为了给创建自己实例的人数据

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //dateListener` 是 `DatePickerDialog.OnDateSetListener` 的一个实例，它是 Android SDK 中的一个接口。
        // 该接口需要实现方法“onDateSet(DatePicker view, intyear,intmonth,intday)”，
        val dateListener = DatePickerDialog.OnDateSetListener { //给DatePickerDialog设置监听器
                _: DatePicker, year: Int, month: Int, day: Int -> //第一个参数指确定日期的DatePicker, _指不需要

            val resultDate : Date = GregorianCalendar(year, month, day).time //用.time转换成Date

            targetFragment?.let { fragment ->
                (fragment as Callbacks).onDateSelected(resultDate)
            }
        }
        val date = arguments?.getSerializable(ARG_DATE) as Date //拿到传来的date
        val calendar = Calendar.getInstance()
        calendar.time = date
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(
            requireContext(), //用来获取视图相关必需资源的context对象
            dateListener,
            initialYear,
            initialMonth,
            initialDay
        )
    }

    companion object {
        fun newInstance(date: Date): DatePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }

            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }
}