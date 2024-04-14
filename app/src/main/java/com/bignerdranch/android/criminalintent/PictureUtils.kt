package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point

class PictureUtils { //使用伴随对象, 这样不用实例就能调用了

    companion object{
        fun getScaledBitmap(path: String, activity: Activity): Bitmap {
            val size = Point()
            activity.windowManager.defaultDisplay.getSize(size) //调用 getSize，将显示尺寸存储在 size 中。 这为您提供了用于缩放图像以适合屏幕的最大尺寸

            return getScaledBitmap(path, size.x, size.y) //
        }

        fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
            // Read in the dimensions of the image on disk
            var options = BitmapFactory.Options() //com/bignerdranch/android/criminalintent/PictureUtils.kt:18
            options.inJustDecodeBounds = true //告诉 BitmapFactory 不要将实际位图加载到内存中，而只是填充尺寸（outWidth 和 outHeight）。(读到内存会导致内存膨胀(原本压缩的图片变成BitMap会一下子变大成几十m))
            BitmapFactory.decodeFile(path, options) //解码 path 指定的图像文件以检索其尺寸。
            val srcWidth = options.outWidth.toFloat()
            val srcHeight = options.outHeight.toFloat()

            // Figure out how much to scale down by
            var inSampleSize = 1 //如果是2, 水平像素比是1:2(原图:缩略图), 像素数为1:4
            if (srcHeight > destHeight || srcWidth > destWidth) { //当需要缩小时
                val heightScale = srcHeight / destHeight
                val widthScale = srcWidth / destWidth

                val sampleScale = if (heightScale > widthScale) { //用大的部分缩放, (用小的当缩小时会溢出)
                    heightScale
                } else {
                    widthScale
                }
                inSampleSize = Math.round(sampleScale) //将浮点数舍入为最接近的整数(四舍五入)
            }

            //创建一个新的Options实例，并设置inSampleSize，指示图像缩小多少。
            options = BitmapFactory.Options()
            options.inSampleSize = inSampleSize

            // Read in and create final bitmap
            return BitmapFactory.decodeFile(path, options)
            //解码缩放后的位图： return BitmapFactory.decodeFile(path, options) 最后将缩放后的图像加载到内存中
        }
    }
}