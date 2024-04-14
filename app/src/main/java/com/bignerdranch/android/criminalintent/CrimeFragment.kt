package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bignerdranch.android.criminalintent.PictureUtils.Companion.getScaledBitmap
import java.io.File
import java.util.Date
import java.util.UUID

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0 //请求代码
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHOTO = 2
private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeFragment : Fragment(), DatePickerFragment.Callbacks {
    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) { //托管activity在newInstance时给fragment添加参数, fragment再在onCreate时候拿到
        super.onCreate(savedInstanceState)
        crime = Crime()     //一开始crime所有有默认值的属性就全初始化了(包括图片名)
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId) //保存Id在ViewModel, 并利用联动保存crime
    }

    override fun onCreateView( //FragmentManager会调用
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { //生成fragment视图
        //第一个参数是资源id(fragment_crime.xml)(布局的资源id)
        //第二个参数是父视图(容器)
        //第三个是是否立即把生成的视图添加给父视图
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe( //观察LiveData对象, id一变crimeIdLiveData变, crimeLiveData也变
            viewLifecycleOwner,  //LiveData的观察与当前视图绑定生命周期
            Observer { crime -> //继承Observer接口, 重写了参数为crime的方法
                crime?.let {//以下代码LiveData一变马上执行!
                    this.crime = crime //从crimeLiveData拿到crime
                    photoFile = crimeDetailViewModel.getPhotoFile(crime) //一开启fragment就拿到图像文件了(包含路径)
                    photoUri = FileProvider.getUriForFile(requireActivity(), //把本地文件路径转换为相机能使用的Uri形式
                        "com.bignerdranch.android.criminalintent.fileprovider", //provider授权
                        photoFile) //文件路径
                    updateUI()
                }
            })
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher { //匿名内部类

            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // This space intentionally left blank}
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) { //返回crime标题字符串
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                // This one too
            }
        }

        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            //在 Kotlin 中，当您有一个带有多个参数的 lambda 表达式，但您不需要使用其中一个或多个参数时，
            // 您可以将这些参数替换为下划线 (`_`) 以指示它们未使用。这使您的代码更加简洁，并向读者表明这些参数是故意不使用的。
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked //发生状态变换时调用
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply { //把当前crime的data传给DatePickerFragment
                setTargetFragment(this@CrimeFragment, REQUEST_DATE) //设置DatePickerFragment的目标fragment为CrimeFragment
                //把数据从CrimeFragment传给DatePickerFragment:添加参数在Bundle, 设置arugments, newInstance时候传递, onCreat或其他函数get拿到
                //把数据从DatePickerFragment传给CrimeFragment, newInstance时候给它设置目标fragment为CrimeFragment, 创建回调接口然后在CrimeFragment实现
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            } //this是外部的DatePickerFragment
        }

        reportButton.setOnClickListener { //fragment会调用startActivity(Intent)
            Intent(Intent.ACTION_SEND).apply { //隐式Intent, 向系统指定要做的事, 然后寻找能做这个事的activity
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())  //消息内容
                putExtra(
                    Intent.EXTRA_SUBJECT,                      //主题
                    getString(R.string.crime_report_subject))
            }.also { intent ->
                val chooserIntent = //选择器(加入自己的字符串)
                    Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        suspectButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI) //ContactsContract.Contacts.CONTENT_URI指示要从数据库中找到某个联系人(手机自带的Contacts数据库)
            setOnClickListener { //要数据用startActivityForResult, 加入请求代码
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            //pickContactIntent.addCategory(Intent.CATEGORY_HOME) //用来验证按钮禁用是否成功
            val packageManager: PackageManager = requireActivity().packageManager //安卓PackageManager拥有所有设备activitty和组件信息
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent,
                    PackageManager.MATCH_DEFAULT_ONLY) //只会匹配带MATCH_DEFAULT_ONLY的activity
            if (resolvedActivity == null) { //只要没有默认联系人应用都会禁用按钮
//                isEnabled = false
            }
        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
//                isEnabled = false
            }

            setOnClickListener {//由于严格的 URI 权限，使用“FileProvider”提供的“Uri”对于与 Android Nougat 及更高版本的兼容性非常重要。
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri) //添加文件存储路径(Uri)

                val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(captureImage,
                    PackageManager.MATCH_ALL)
//                val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(captureImage,
//                    PackageManager.MATCH_DEFAULT_ONLY) //查询包管理器以获取可以处理 `captureImage` 意图的所有活动。 这是授予这些活动写入“photoUri”权限所必需的。

                for (cameraActivity in cameraActivities) { //循环授予对指定“Uri”（“photoUri”）的临时写入访问权限，这是捕获的图像的存储位置。
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName, //指的是可以处理捕获图像意图的相机应用程序（或多个应用程序）的包名称（“MediaStore.ACTION_IMAGE_CAPTURE”）。 此包名称标识 Android 设备上将被调用来拍照的特定应用程序或组件。 它与相机应用程序存储图像的位置无关； 相反，它标识应用程序本身。
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION) //该标志用于授予内容`Uri`临时写入权限。 该权限适用于特定的“Uri”，允许接收应用程序（在本例中为相机应用程序）对其进行写入。 此权限授予将持续到接收应用程序的任务完成为止（直到用户返回到您的应用程序）
                        //销毁时或完成拍照时取消权限的文件访问也是用这个标志
                } //此代码确保每个被识别为能够处理图像捕获意图的相机应用程序都被授予必要的权限，以将结果图像写入指定的“Uri”

                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime) //只有title会变
    }

    override fun onDetach() { //在访问结束或无效访问后, 调用权限关闭文件访问
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    } //把拿到的date保存(在DatePickerDialog强制转换CrimeFragment为Callbacks接口并调用函数传递数据就行了), 需要监听器

    ////托管activity(Callbacks)在newInstance时给fragment添加参数, fragment再在onCreate时候拿到, 保存Id在ViewModel, 并利用联动保存crime, 在fragment添加了观察者, 一联动就更新当前crime, 并通过它刷新UI
    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString() //时间也是需要按时更新的
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState() //跳过动画
        }
        if (crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect //按钮的名字保存下来
        }
        updatePhotoView()
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
            photoView.contentDescription = getString(R.string.crime_photo_image_description)
            photoView.setOnClickListener {
                PhotoZoomFragment.newInstance(bitmap).show(requireFragmentManager(), "PHOTO_DIALOG")
            }//1.此方法应该创建“PhotoZoomFragment”的新实例并将“Bitmap”图像作为参数传递。 但是，如前所述，缺少将位图添加到片段参数的代码。 为了使其正常工作，您通常会捆绑位图或对其的引用（例如文件路径或 URI），并将该捆绑设置为片段的参数。
            //2.`show` 方法需要一个 `FragmentManager` 和一个标签 (`"PHOTO_DIALOG"`)。 `FragmentManager` 负责管理活动内的片段，标签是对话框片段的唯一字符串标识符。
        } else {
            photoView.setImageDrawable(null)
            photoView.setOnClickListener(null)
            photoView.contentDescription = getString(R.string.crime_photo_no_image_description)
        }
    }

    //onActivityResult在onViewCreated之前被调用, 防止onViewCreated从数据库检索的信息覆盖嫌疑人, 先把嫌疑人保存
    //接受数据(数据会附加在Intent), 使用 `startActivityForResult` 启动的activity退出时，就会调用该函数
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //(返回包含在intent的URI数据时, 会添加一个标志授权给应用可以使用联系人数据一次)
        when {
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data //此行从数据中获取所选联系人的“Uri”。 这个“Uri”用于查询联系人详细信息。
                //此行定义查询应返回哪些字段。 在本例中，我们只对联系人的显示名称感兴趣。
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

                //使用 `requireActivity().contentResolver` 来使用 `Uri` 查询联系人数据。
                // “query”函数参数是“Uri”、要返回的字段（只是显示名称）以及用于选择、选择参数和排序顺序的三个“null”值
                //用contentResolver那contentProvider的数据库(安卓深度与联系人数据绑定的数据库api)
                val cursor = contactUri?.let { //防止contactUri为null
                    requireActivity().contentResolver
                        .query(it, queryFields, null, null, null)
                }
                cursor?.use { //cursor只会包含一个记录
                    // Verify cursor contains at least one result
                    if (it.count == 0) {
                        return
                    }

                    // Pull out the first column of the first row of data -
                    // that is your suspect's name
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime) //保存有嫌疑人的crime
                    suspectButton.text = suspect
                }
            }

            requestCode == REQUEST_PHOTO -> { //拍了照马上更新视图
                requireActivity().revokeUriPermission(photoUri, //在访问结束或无效访问后, 调用权限关闭文件访问
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView()
            }
        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        var suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(R.string.crime_report,
            crime.title, dateString, solvedString, suspect)
    }

    companion object {

        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply { //Bundle相当于arguments, put参数到Bundle, 在设置fragment的arguments
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
}