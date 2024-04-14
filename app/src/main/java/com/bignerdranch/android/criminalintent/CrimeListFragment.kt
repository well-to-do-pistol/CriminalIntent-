package com.bignerdranch.android.criminalintent
//显示列表项需要RecyclerView, layoutManager, ViewHolder, Adapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat
import java.util.Locale
import java.util.UUID

private const val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment() {
    /**
     * Required interface for hosting activities
     */
    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks? = null

    private val DIFF_CALLBACK: DiffUtil.ItemCallback<Crime> = object : DiffUtil.ItemCallback<Crime>() {
        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            // 如果两个项代表同一个对象，则返回 true。.
            Log.i(TAG, "compare id")
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            // // 如果项目的内容没有改变，则返回 true。
            Log.i(TAG, "compare crime")
            return oldItem == newItem
        }
    } //“ListAdapter”将使用该实例来确定犯罪列表中发生的更改。

    //- **初始化控制**：`lateinit` 允许变量在声明之后和使用之前的任何时候进行初始化。 “by lazy”在第一次访问变量时初始化该变量。
    //- **可变性**：`lateinit` 用于可变属性（`var`），而 `bylazy` 用于只读属性（`val`）。
    //- **安全**：尝试在初始化之前访问“lateinit”变量会导致运行时异常。 “惰性”属性可确保在首次访问时进行初始化，从而消除未初始化访问的风险。
    //- **线程安全**：`bylazy`具有内置的线程安全选项，而`lateinit`没有任何线程安全保证，由开发人员管理。
    //在“lateinit”和“by Lazy”之间进行选择取决于该属性是否可变、您计划何时初始化它以及您是否需要线程安全。
    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this).get(CrimeListViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks? //强制转换了, 所以托管activity需要继承Callbacks接口
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {//生成fragment视图
        //第一个参数是资源id(fragment_crime.xml)(布局的资源id)
        //第二个参数是父视图(容器)
        //第三个是是否立即把生成的视图添加给父视图
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        crimeRecyclerView = //从view中拿到RecyclerView属性
            view.findViewById(R.id.crime_recycler_view) as RecyclerView
        //需要用layoutManager安排列表项出现的位置(这里是线性, 竖直)
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter //启动crimeRecyclerView的时候先把传入了空集合的adapter绑定上
        //之后再调用updateUI在onViewCreated
        return view
    }

    //这个函数在onCreateView后执行, 在这里放观察者确保视图(而不是实例)完全生成
    //当您进入“Fragment”的“onViewCreated”方法时，“viewLifecycleOwner”已经初始化并可供使用。
    // 它由 Fragment 类本身提供，因此您不需要显式传递它。
    // 视图的生命周期从片段的“onCreateView”方法返回非空视图时开始，到调用“onDestroyView”时结束。
    // 因此，“viewLifecycleOwner”是有效的，可以在这两个回调之间使用。
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addButton = view.findViewById<Button>(R.id.add_crime_button)
        addButton.setOnClickListener {
            val crime = Crime()
            crimeListViewModel.addCrime(crime)
            callbacks?.onCrimeSelected(crime.id)
        }

        crimeListViewModel.crimeListLiveData.observe( //除非应用退出, 否则观察者会死死盯着然后更新
            viewLifecycleOwner, //防止CrimeListFragment销毁还观察导致报错, 让其和观察者周期同步
            //同步的是fragment视图的生命周期, 而不是fragment实例的, 本质上其实不同
            Observer { crimes -> //就是crimeListLiveData的类型
                crimes?.let {
                    Log.i(TAG, "Got crimes ${crimes.size}")
                    updateUI(crimes)
                }//这里，“Observer”是“Observer”接口的实现，它定义了一个“onChanged()”方法。
            // 您看到的代码是一个 lambda 表达式，它充当此接口的简洁实现，
            // 其中“crimes”是传递给“onChanged()”的参数。
            // `crimes?` 部分使用 Kotlin 的安全调用运算符来确保在继续之前 `crimes` 不为 null。
            // 如果“crimes”不为空，则 lambda 会记录收到的犯罪数量，并使用“crimes”列表调用“updateUI()”来更新用户界面。
            })
    }

    override fun onDetach() { //销毁
        super.onDetach()
        callbacks = null
    }

    //当activity收到操作系统的onCreateOptionsMenu回调请求
    //必须通知FragmentManager(使用setHasOptionsMenu)其管理的fragment应接受onCreateOptionsMenu的调用指令
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { //当菜单项被点击时, fragment会收到这个函数的回调请求
        return when (item.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id) //调用callbacks里函数切换视图并刷新
                true //返回true表示任务已完成, false则调用托管activity的函数继续, 如果它还托管了其他fragment那么也会调用
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI(crimes: List<Crime>) {
//        adapter = CrimeAdapter(crimes) //只接受传入的crimes了
//        crimeRecyclerView.adapter = adapter //在onCreateView的同时创建并添加adapter

        val emptyView = view?.findViewById<TextView>(R.id.empty_view)
        val addButton = view?.findViewById<Button>(R.id.add_crime_button)

        if (crimes.isEmpty()) {
            crimeRecyclerView.visibility = View.GONE
            emptyView?.visibility = View.VISIBLE
            addButton?.visibility = View.VISIBLE
        } else {
            crimeRecyclerView.visibility = View.VISIBLE
            emptyView?.visibility = View.GONE
            addButton?.visibility = View.GONE
//            adapter = CrimeAdapter(crimes)
//            crimeRecyclerView.adapter = adapter
            adapter = CrimeAdapter(crimes)
            crimeRecyclerView.adapter = adapter
            Log.i(TAG, "before submit: Got crimes ${crimes.size}")
            adapter?.submitList(crimes)
            Log.d(TAG, "updateUI: Submitted ${crimes.size} crimes to the adapter.")
        }
    }


    //CrimeHolder是CrimeListFragment的内部类
    private inner class CrimeHolder(view: View) //管理列表项(一个ViewHolder对应一个列表项)
        : RecyclerView.ViewHolder(view) , View.OnClickListener{ //1.itemView藏在每一个ViewHolder内; 2.实现点击监听器接口

        private lateinit var crime: Crime
        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this) //为它自己的itemView设置监听器, 同时它也要继承点击监听器接口并重写onClick
        }

        fun bind(crime: Crime) { //绑定数据, 显示真实视图数据
            Log.d(TAG, "Binding crime with ID: ${crime.id}")
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.DEFAULT, Locale.getDefault()).format(this.crime.date)
            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
            //给盲人
            val solvedOrNot = if (crime.isSolved){
                getString(R.string.crime_solved)
            }else{
                getString(R.string.crime_not_solved)
            }
            solvedImageView.contentDescription = solvedOrNot
            itemView.contentDescription = getString(R.string.crime_summary, titleTextView.text, dateTextView.text, solvedOrNot)
        }

        override fun onClick(v: View) {
            callbacks?.onCrimeSelected(crime.id) //一点击就调用callbacks里函数切换视图并刷新
        } //这个函数会传id导致数据更新和fragment的UI刷新
    }//ViewHolder引用列表项视图

    private inner class CrimeAdapter(var crimes: List<Crime>)
        : ListAdapter<Crime, CrimeHolder>(DIFF_CALLBACK) { //创建ViewHolder, 绑定其和模型层数据

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : CrimeHolder {
            //创建子视图
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        override fun getItemCount() = crimes.size

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }
    }

    companion object { //用newInstance可能是因为在初始化之前要配置
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }
}