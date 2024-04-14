package com.bignerdranch.android.criminalintent

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.UUID

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //FragmentManager能有效防止设备旋转和回收内存的情况
        val currentFragment = //获取当前Fragment, 注意:容器视图id拿的就是CrimeFragment, 现在就是null
            supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currentFragment == null) { //FragmentManager.beginTransaction()创建并返回FragmentTransaction
            val fragment = CrimeListFragment.newInstance()
            supportFragmentManager
                .beginTransaction() //用事务添加CrimeFragment到容器视图中
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    //crimeId怎么传过来的?(CrimeListFragment的CrimeHolder的crime在onClick把.id传过来)
    override fun onCrimeSelected(crimeId: UUID) { //重写CrimeListFragment中Callbacks接口的方法
        val fragment = CrimeFragment.newInstance(crimeId)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment) //替换容器栈里的fragment
            .addToBackStack(null)      //按返回键能回退到CrimeListFragment, null是回退栈名字
            .commit()
    }//callbacks是用来切换fragment的
    //数据从哪来?从CrimeFragment的生命周期函数来, 而且有绑定了crimeDetailViewModel的监听器, id一变(点击不同Id的crime)
    //crime就变, 就刷新CrimeFragment视图
}