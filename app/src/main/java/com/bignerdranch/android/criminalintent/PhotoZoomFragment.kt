package com.bignerdranch.android.criminalintent

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class PhotoZoomFragment : DialogFragment() { //- `PhotoZoomFragment` 是一个扩展 `DialogFragment` 的自定义类，使其能够在对话框窗口中显示内容。

    private lateinit var photoView: ImageView //声明一个后期初始化的 `ImageView` 变量。 这将用于显示缩放的照片。 使用后期初始化是因为在调用片段的“onCreateDialog”方法之前视图无法初始化。

    //`onCreateDialog()`方法是Android中`DialogFragment`的生命周期方法。 创建对话框时会调用它。 在此方法中，您可以设置并返回将由“DialogFragment”显示的“Dialog”实例。
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //- `LayoutInflater`：一个类，用于将布局 XML 文件实例化为其相应的 `View` 对象。 简单来说，它将您的布局文件转变为您的代码可以操作的完全交互式 UI 元素
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_photo, null) //这是附加膨胀布局的根视图。 此处传递“null”表示您在此阶段没有将膨胀布局绑定到父视图。 这是膨胀对话框布局时的标准，因为对话框本身将处理将视图附加到屏幕。

        // Retrieve the ImageView where you'll display the photo.
        photoView = view.findViewById(R.id.zoomed_photo)

        // Retrieve the bitmap from the fragment's arguments.
        val photoBitmap = arguments?.getParcelable<Bitmap>(ARG_PHOTO)

        // Set the retrieved bitmap on the ImageView.
        photoView.setImageBitmap(photoBitmap)

        //AlertDialog.Builder(requireContext())`：这会使用当前上下文初始化一个新的 `AlertDialog.Builder` 实例。 对话框需要上下文来了解它属于哪个 UI
        return AlertDialog.Builder(requireContext())
            .setView(view)//此方法设置对话框的自定义视图。 该视图通常是从定义对话框 UI 的 XML 布局文件扩展而来的。 在本例中，布局包含一个用于显示照片的“ImageView”。
            .create() //此方法使用提供给构建器的参数（例如上下文、视图）创建一个 `AlertDialog`。
    }

    companion object {
        private const val ARG_PHOTO = "photo_bitmap"

        fun newInstance(photo: Bitmap): PhotoZoomFragment {
            val fragment = PhotoZoomFragment()
            val args = Bundle()
            args.putParcelable(ARG_PHOTO, photo)
            fragment.arguments = args
            return fragment
        }
    }
}
//您提到的“Bundle”通常用于在 Android 应用程序的不同组件（例如活动、片段和服务）之间传递数据。
// 在“DialogFragment”的上下文中，“Bundle”可用于将数据从创建“DialogFragment”的片段传递到“DialogFragment”本身。
// 这通常是在创建“DialogFragment”时、显示之前完成的。
//作为生命周期方法，“Bundle”与“onCreateDialog()”方法没有直接关系。
// 相反，它是一种传递数据的机制。
// 但是，您通常会在“onCreateDialog()”方法中使用“Bundle”中传递的数据来配置“DialogFragment”将显示的“Dialog”。
