## 一.Room

1. #### 配置

   - 定义实体
   - 创建数据库类(放DAO)
   - 添加TypeConverter函数
   - 定义数据库访问对象(DAO)(增删改查)
   - 定义仓库类(调用数据库类中DAO的函数)

2. #### 要在后台线程进行数据库访问

   - DAO中的函数设置返回类型为livedata(就可以自动进行后台线程访问)

3. #### 设置数据库导出目录

4. #### 单例可以换成依赖注入, 方便测试

## 二.Fragment

数据传递逻辑: 数据库拿到所有crimes对象, 传入adapter, 通过position分配给各个holder, holder再分配id给单个明细fragment, 显示数据

UI更新逻辑: LiveData和Room的集成使得只要crimelistfragment观察了livedata, 数据库一改变它就能从livedata得到新的crimes数据, 来更新UI

1. #### 为维护fragment的独立性(不能在fragment里发起fragment事务), 创建fragment回调接口

   - fragment定义接口, activity继承, 并在onAttach传递接口对象(onDetach删除)
   - fragment里可以用对象调用activity的重写函数来替换fragment

2. #### 数据传递

   - 回调接口
   - 使用Bundle和argument在activity和fragment之间传递数据
   - crimelistfragment的holder里用接口函数传递id, activity的切换事务里传入id

3. #### LiveData数据转换

   - livedata的swithMap实现livedata的id以变, 马上访问数据库得到crime对象修改当前livedata的crime对象
   - 观察livedata的crime对象来更新ui

4. #### 使用Executor(需要引用线程的对象)来实现数据库更新和插入

   - 通常它用于执行后台任务, 放在仓库类, 不要放在dao(Room会识别错误)

5. #### ListAdapter实现高效RecyclerView刷新

   - 添加DiffUtil.ItemCallback<Crime>
   - 观察到数据库内数据变化时, 重新构建adapter添加到recyclerview, adapter?.submitList(crimes)更新ui

## 三.DatePickerFragment

1. #### 使用DialogFragment旋转后可重建恢复

2. #### crimefragment传数据给datePickerfragment

   - 在datePickerfragment设置Bundle和arguments, 在crimefragment的按钮处创建实例时传入

3. #### datePickerfragment传数据给crimefragment

   - datePickerfragment定义接口, crimefragment继承, 并在setTargetFragment传递接口对象
   - 监听日期按钮, 调用接口对象的重写函数, 完成传递

## 四.隐式intent

1. #### 发送消息

2. #### 获取联系人

   - 以ACTION_PICK启动activity
   - onActivityResult接收intent并拿出里面的Uri
   - onActivityResult在onViewCreated之前执行(在这保存在数据库), 注意--获取联系人全程fragment的视图都没有被摧毁
   - 如果只使用联系人数据一次, 则自动授权

3. #### 询问联系人权限

   - 检查是否有权限, 没有权限再检查是否已关闭(点击联系人按钮)
   - 如果已关闭弹出警告窗去设置, 没关闭会请求权限(实际上一进来fragment就会先请求)

4. #### 拨打电话

   - onActivityResult里得到联系人id后再拿电话, 保存数据库
   - 点击拨打按钮构建电话Uri, 启动隐式intent

## 五.intent拍照

1. #### ContentProvider暴露Uri

   - manifest添加FileProvider
   - 并关联路径描述(Provider)(".")
   - $id.jpg为文件名
   - 仓库类内通过context.applicationContext.filesDir和文件名拿到File

2. #### 触发拍照

   - 使用Provider授权和File通过FileProvider获取到Uri(相机要用的)
   - 过滤的时候要用PackageManager.MATCH_ALL
   - 启动cameraActivity(startActivityForResult)

3. #### 缩放和显示位图

   - BitmapFactory.Options()计算出缩放值
   - 用Uri和缩放函数来显示位图
   - 估算:activity.windowManager.defaultDisplay.getSize(Point())
   - onActivityResult完成后要显示位图和关闭暴露Uri的写权限(onDetach也要)
   - manifest设置<uses-feature>以支持没有相机的设备

4. #### 点击缩略图放大

   - 新建Dialogfragment(需要传bitmap), 用.show()
   - 用 AlertDialog

5. #### 优化缩略图加载

   - 用ViewTreeObserver观察imageView, 然后用已有的imageView的大小来缩放(取代估算)
   - 等到有布局切换时再显示缩略图
   - 不用观察直接更新也可以, 因为视图从来没消失过

## 六.外国语言设置

1. #### 新建string.xml

   - 找到Locale, 选中美国
   - 右击string.xml选择Open Translations Editor可查看所有语言版本

2. #### 宽屏也是同样的方法设置

3. #### 在xml里设置contentDescription配置视力障碍辅助功能
