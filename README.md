CriminalIntent(Kotlin):
	配置(零): 
1.ConstraintLayout, ViewModel, FrameLayout
	   2.ViewModel和保存实例状态防止旋转和内存清理
	   3.context比任何activity都活得久
	   4.LiveData: 生命周期感知, 无内存泄漏, 不会因活动停止而崩溃, 实时数据更新
	   5. MainActivity继承CrimeListFragment的Callbacks, 重写onCrimeSelected(实现了将点击事件(id)从CrimeListFragment传递到了MainActivity)(onAttach的时候将activity的context传给了fragment, fragment将之转成callbacks再在holder传入数据进行调用, 而activity已经重写了方法, 所以能得到数据)-[发送方定义接口, 接受方随便重写方法, 就能得到数据](关键是onAttach传context)__p207
	   6.用Bundle加上上面一点就可以fragment -activity-fragment互传数据了
	   7.在Dao的Query用LiveData接收数据时,会告诉Room在后台查询(查询)(也可用Executors.newSingleThreadExecutor(更新和插入))
	   8.fragment-fragment互传数据, 也是Bundle加回调接口
M:
一.	开发记录应用明细部分(CrimeFragment, MainActivity)
1.	新建fragment_crime.xml(LinearLayout), 一个text, 一个edittext, 一个text, 一个button, 一个checkbox
2.	新建CrimeFragment类, lateinit初始化Crime(OnCreate), 在onCreateView初始化layout, 初始化edittext
3.	OnStart初始化edittext监听器, onTextChanged后赋值crime的title. addListener
4.	onCreateVIew初始化dateButton, 禁用并将其文字设置为crime.date;
5.	onCreateView初始化solvedCheckBox, onStart里setOnCheckListener(将isChecked赋给crime.isSolved)
6.	修改mainactivity(FrameLayout), 检索一下mainactivity的id判空, 然后事务add里加id和new Fragment()
二.	使用RecyclerView显示列表(CrimeListFragment)
1.	App.gradle里加lifecycle-extensions依赖
2.	新建CrimeListViewModel类继承ViewModel, 新建列表, 在Init里for循环新建100个crime加进去(title设I, isSolved设i%2==0)
3.	新建CrimeListFragment, 创CrimeListViewModel对象用ViewModelProviders得到(关键在这里语句的this关联了); 在伴随对象里新建newInstance的fun新建本对象
4.	在MainActivity的onCreate的fragment改为CrimeListFragment的newInstance(已经托管了100个对象)
5.	添加recyclerview依赖, 新建fragment_crime_list.xml指定为RecyclerView, 在CrimeListFragment的onCreateView配置视图. 新建list_item_crime.xml(LinearLayout)加两个text
6.	CrimeListFragment加CrimeHolder(inner)类, 配置两个text视图; 加CrimeAdapter(传list), onCreateHolder里返回CrimeHolder(view, view是list_item_crime.xml(LinearLayout)两text的膨胀), 修改getItemCount, 从bind里添加用position拿到当前crime, 用它设置hoder的两个text
7.	CrimeListFragment里新建adapter对象, updateUI()(用得到的crimeListViewModel拿到列表, 加进adapter, 设置)
8.	CrimeHolder新建crime对象, 两Text属性改私有, 加bind(传入crime)方法(用传入的初始化自己的crime和设置两个text), 删除adapter 的Bind的holder.apply的重复语句改为holder.bind(crime)
9.	CrimeHolder继承viewClickListener, 在Init为itemView(隐藏)设置监听, 重写OnClick
三.	使用布局与部件创建用户(Home)界面, (CrimeListFragment)
1.	打开list_item_crime.xml, 从视图convert LinearLayout to ConstraintLayout, 在属性把两text都改为wrap_content; 往date下面拖个image(ic_solved), 约束布局拖右边(上下右都是8dp);两text都约到Image左边, title上左16右8, date上8左16右8, 改名图片属性为crime_solved
2.	在CrimeListFragment的CrimeHolder加image对象并同句配置, bind里根据crime.isSolved设图片的可见, 改title的大小和颜色
3.	挑战
a)	显示date特定字符串
i.	待搞…
四.	数据库与Room(在SQLite之上)库
1.	在app.gradle添加plugin和两句依赖. 给Crime(data)加上@Entity和id加上@PrimaryKey注释
2.	新建database包, 里面新建CrimeDatabase类(abstract, 继承RoomDatabase)@Database配置视图和版本; 里面新建CrimeTypeConverters类, 加UUID(Crime)和String(Room), Date和String的互转函数@TypeConverter标记, @TypeConverters再加在CrimeDatabase头上(类标为转换类)
3.	Database包里面新建CrimeDao(interface, @Dao标记)类, 加得crime(传Id)和crimes函数, @Query标记后面跟SQL语句, 在CrimeDatabase里登记abstract fun crimeDao(): CrimeDao
4.	新建CrimeRepository(private constructor), 伴随对象:加个自己对象, 加initialize(单例)和get()函数; 创建Application子类CriminalIntentApplication在onCreate调用initialize, Manifest的application的name登记CriminalIntentApplication
5.	在CrimeRepository里新建对象Build CrimeDatabase传类和静态字符串, 新建并new crimeDao对象; 加两个函数调用crimeDao的两函数
6.	在文件浏览器包位置upload测试数据库. 删除CrimeListViewModel的所有代码, 新建CrimeRepository对象(get得到), 调用它的get方法拿到crimes
7.	修改CrimeDao的两个方法把返回数据类型都用LiveData包装, CrimeRepository的两个也是
8.	删除CrimeListFragment的updateUI调用并修改该方法: 要传入List<Crime>, 删除crimes的赋值语句; 修改adapter对象的赋值语句为CrimeAdapter(emptyList()); 在onCreateView加一句crimeRecyclerView.adapter=adapter(与后面方法重复了); 重写onViewCreated(观察crimeListViewModel.crimeListLiveData, crimes?.let{调用updateUI(crimes)})
9.	App.gradle里android{}加一句kapt {
  arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
R:
五.	Fragment Navigation(CrimeFragment代替CrimeListFragment在MainActivity)(点击item显示明细)
1.	在CrimeListFragment(托管的fragment建立回调), 加Callbacks接口(配一个传入UUID的方法),  声明Callbacks对象为null, 重写onAttach(fragment附加到activity调用)(赋值context为callbacks(强制转换)), 重写 onDetach(设callbacks为null)
2.	也是CrimeListFragment, 在CrimeHolder的onClick调用callbacks?.onCrimeSelected(crime.id)
3.	MainActivity继承CrimeListFragment的Callbacks, 重写onCrimeSelected(实现了将点击事件(id)从CrimeListFragment传递到了MainActivity)(onAttach的时候将activity的context传给了fragment, fragment将之转成callbacks再在holder传入数据进行调用, 而activity已经重写了方法, 所以能得到数据)-[发送方定义接口, 接受方随便重写方法, 就能得到数据](关键是onAttach传context)
4.	MainActivity的onCrimeSelected修改打印为new CrimeFragment, 用fragmentManager更换容器中的fragment(添加.addToBackStack(null)返回上一fragment)
5.	在CrimeFragment伴随对象里加个newInstance方法(传入crimeId)(Bundle()要putSerializable, 设置返回的CrimeFragment的arguments为args变量(Bundle))
6.	MainActivity的onCrimeSelected修改new CrimeFragment为.newInstance(crimeId)
7.	在CrimeFragment的onCreate里用arguments(隐藏)?.getSerializable拿到id
8.	使用LiveData数据转换:
a)	新建CrimeDetailViewModel类(继承ViewModel), 加crimeRepository对象(从类中get到), 加cromeIdLiveData对象初始化为MutableLiveData<UUID>, 加crimeLiveData(LiveData<Crime?>)对象(从switchMap(id一变就拿到crime)拿crimeIdLiveData的 (crimeId)调用crimeRepository.getCrime来初始化), 加loadCrime方法(传入crimeId初始化crimeIdLiveData.value=crimId)
b)	修改CrimeFragment, 加CrimeDetailViewModel(by lazy)对象(用ViewModelProviders初始化), 在onCreate调用CrimeDetailViewModel.loadCrime(crimeId)
c)	修改CrimeFragment, 在onViewCreated再观察CrimeDetailViewModel.crimeLiveData变化(设置this.crime=crime, updateUI()), 加updateUI()(titile, date, isChecked都为crime的数据(设isChecked时对solvedCheckBox用apply设.jumpDrawablesToCurrentState()跳过勾选动画))
9.	更新数据库
a)	在CrimeDao新增update和addCrime方法(@Update和@Insert都是传入Crime)(不用加任何SQL)
b)	在CrimeRepository新建一个后台线程Executors.newSingleThreadExecutor, 加update和addCrime方法(都是传入Crime)(在executor.execute{}调用dao函数)(前面是用LiveData在后台)
c)	在CrimeDetailViewModel加saveCrime(传入crime)调用工厂的update(), 在CrimeFragment的onStop调用saveCrime
10.	挑战
a)	实现高效RecyclerView刷新
i.	不搞…
六.	对话框(修改日期)
1.	创建DatePickerFragment继承DialogFragment, 配置一下. 在CrimeFragment的onCreateView删一下dateButton的东西, onStart加dataButton的onClick打开DatePickerFragment
2.	在DatePickerFragment伴随对象加newInstance(date: Date), 里面用Bundle加putSerializable和arguments来实现fragment数据互传. CrimeFragment的dateButton的onClick改DatePickerFragment.newInstance
3.	DatePickerFragment的onCreateDialog获取arguments的date设到calendar.time去
4.	Dialog返回数据给CrimeFragment
a)	CrimeFragment的dateButton的onClick里设置targetFragment
b)	DatePickerFragment加Callbacks接口(fun onDateSelected(date: Date)). CrimeFragment继承接口重写函数赋值date给crime再updaetUI()
c)	DatePickerFragment的onCreateDialog改一下传个listener(配置一下listener)
5.	挑战
a)	时间选择对话框(详细时间选择, 加个time按钮)
i.	有空再搞吧没时间了
七.	应用栏菜单
1.	添加字符串资源, 建fragment_crime_list.xml(menu)(加个item, ifRoom|withText). CrimeListFragment的onCreateOptionsMenu里膨胀menu布局, onCreate里加句setHasOptionsMenu(true)接收菜单函数回调
2.	CrimeListViewModel里加个addCrime方法(输入crime), 调用工厂的addCrime. CrimeListFragment的onOptionsItemSelected当Id是new_crime调用模型的addCrime, 再调用callbacks(activity的context)的onCrimeSelected传id
3.	New Image Assets加个add, 在fragment_crime_list的menu的item里引用它
4.	挑战
a)	空数据加文字和按钮说明
i.	没时间搞了
八.	隐式intent(发送消息)
1.	加字符串. Fragment_crime.xml里加两button(suspect, report). Crime.kt里加一个supect的String变量->数据库有变化->CrimeDatabase改版本, 加一个Migration加个add COLUMN的SQL语句. 在CrimeRepository的build数据库时加个addMigrations
2.	加格式化字符串资源(%1$s或)
3.	在CrimeFragment加个getCrimeReport(判断crime的各种东西返回一个拼接的消息String). 初始化reportButton后在onStart设onClick(新建(ACTION_SEND)隐式intent, putExtra(标题(不显示放主题里)和内容)), 在加个选择器里面加标题再startActivity
4.	获取联系人信息(CrimeFragment)
a)	DATE的常量是0, CONTACT是1. 
b)	初始化suspectButton再在onStart用apply创ACTION_PICK隐式intent配contacts再setOnClick来startActivityForResult(用常量)
c)	在updateUI判空后把suspectButton的text设为crime.suspect
d)	重写onActivityResult折腾一系列后拿到联系人后设置给crime, saveCrime后再设suspectButton.text
e)	在OnStart的suspectButton.apply用packageManager查看是否有联系人应用, 没有isEnabled=false(可用CATEGROY_HOME测试一下)
5.	挑战
a)	拨打电话按钮
i.	没时间了下一个!
九.	使用intent拍照
1.	改fragment_crime在TITLE前加个Linearlayout里又LinearLayout(image和imageButton), TITLE和edittext放在第一个LinearLayout里(再用weight=”1”的LinearLayout把自己包起来). CrimeFragment里初始化(image和imageButton)
2.	照片存在本地文件系统里
a)	Manifests在与activity同级里加provider(配置4属性)
b)	Res里new Android resource file建files.xml, 删所有语句加paths设路径”.”
c)	在manifests的provider里加meta-data设置name和resource(是上面的files.xml)
d)	在Crime里加一个photoFileName属性get()=”…$id…”(由id决定)
e)	CrimeRepository加getPhotoFile(输入crime返回File, 用context获得dir后和crime.photoFileName一起传入File())
f)	CrimeDetailViewModel里加getPhotoFile方法(返回工厂同样方法)
3.	使用相机Intent(CrimeFragment)
a)	CrimeFragment新添photoFile对象, 在onViewCreated的LiveData观察里的crime?.let里用模型里的getPhotoFile获得photoFile(LiveData是从模型再从工厂拿到的, 内容是crime, id(LiveData)改crime也改(实现列表中的项目切换))
b)	新加uri对象, 从photoFile用FileProvider拿到uri
c)	加PHOTO常量=2
d)	photoButton.apply:
i.	加一大段东西打开相机
4.	显示和缩放位图
a)	创建PictureUtils.kt
b)	创建getScaledBitmap(传入path和宽高)返回Bitmap(先知道大小才能调用)
i.	写一大堆东西计算图片应缩放多少, 用一个option缩放, 然后返回缩放后图片
c)	创建getScaledBitmap(传入path和activity)返回Bitmap(主观估算), 用Point()和一句default(调用前面getScaledBitmap)搞定
d)	CrimeFragment加updatePhotoView(), 判空phtotFile存在的话用主观估算缩放在设view的bitmap
e)	在updateUI调用updatePhotoView(), 在onActivityResult判断requestCode是PHOTO的话updatePhotoView()
f)	撤销URI权限
i.	CrimeFragment的onDetach里revoke权限, 在onActivityResult判断requestCode是PHOTO的话updatePhotoView()之前revoke权限
5.	在manifest加<uses-feature android:name=”android.hardware.camera” android:required=”false”/>不需要一定有照相功能
6.	挑战
a)	优化缩略图加载
i.	用ViewTreeObserver没有时间不看了, 详情看书p278

R:
十.	应用本地化
1.	Res/values, new->Values resource file输入:
a)	string.xml
b)	main
c)	选Locale再用>>把它移入窗口, 选中zh-Chinese
d)	添加中文字符串给这个xml 
2.	针对宽屏的字符串资源Res/values, new->Values resource file输入:
a)	string.xml
b)	main
c)	选Available qualifiers再用>>把它移入窗口, 输入宽度600
d)	添加(只加了个edit_hint)宽屏字符串给这个xml 
3.	再按照以上方法创建中文宽屏字符串xml
4.	挑战
a)	日期显示本地化
i.	没时间看了下一个
十一.	辅助功能:
1.	中英文分别添加3句描述字符串 
2.	在fragment_crime.xml的ImageButton设置contentDescription为相应字符串(crime_camera), 再在ImageView(crime_photo)设置contentDescription为相应字符串
3.	在CrimeFragment的updatePhotoView的判空里分别设置photoView的contentDescription
4.	挑战
a)	读手铐和待读列表
b)	p305补全上下文信息
c)	事件主动通知
