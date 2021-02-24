package com.enliple.pudding.widget

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LevelListDrawable
import android.net.Uri
import android.os.AsyncTask
import android.text.Html
import android.text.Spannable
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.enliple.pudding.R
import com.enliple.pudding.common.AppLinkDelegate
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.widget.toast.AppToast
import kotlinx.android.synthetic.main.dialog_noti.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileNotFoundException
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

/**
 * Created by Kim Joonsung on 2019-06-28.
 */
class NotiAlertDialog(context: Context, title: String, message: String, url: String, no: String, updateDate: String) : AppCompatDialog(context, R.style.NotiDialogTheme) {

    private var mContext: Context

    private var mTitle: String
    private var mMessage: String
    private var mUrl: String
    private var mNo: String
    private var mUpdateDate: String

    init {
        this.mContext = context
        this.mTitle = title
        this.mMessage = message
        this.mUrl = url
        this.mNo = no
        this.mUpdateDate = updateDate

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_noti)

        setCancelable(true)

        if (title.isNotEmpty()) {
            textViewTitle.text = title
        }

        if (mMessage.isNotEmpty()) {
            mMessage = mMessage.replace("<p>", "")
            mMessage = mMessage.replace("</p>", "")
        }

        Logger.d("############# message : $message")
        val span = Html.fromHtml(message, Html.ImageGetter { source ->
            var source = source
            val d = LevelListDrawable()
            val empty = ContextCompat.getDrawable(mContext, R.mipmap.ic_launcher)
            d.addLevel(0, 0, empty)
            d.setBounds(0, 0, 0, 0)
            Logger.e("source :: " + source)
//            LoadImage(textViewMessage).execute(source, d)
//            source = "https://previews.123rf.com/images/farang/farang1201/farang120100042/11875193-%EC%95%84%EC%B9%A8%EC%97%90-%EC%95%88%EA%B0%9C-%EB%82%80-%EB%B0%94%EB%8B%A4%EC%9D%98-%EC%A0%84%EB%A7%9D-%EA%B8%B4-%EB%85%B8%EC%B6%9C-%EC%83%B7-%EC%84%B8%EB%A1%9C-%ED%8C%8C%EB%85%B8%EB%9D%BC%EB%A7%88-%EC%A1%B0%EC%84%B1-.jpg"
            Glide.with(context!!)
                    .asBitmap()
                    .load(source!!)
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            var viewWidth = AppPreferences.getScreenWidth(context!!) - Utils.ConvertDpToPx(context!!, 20)
                            var width = resource.width
                            var height = resource.height

                            var viewHeight = (viewWidth * height) / width
                            Logger.e("width :: " + width + " , height :: " + height)
                            Logger.e("viewWidth :: " + viewWidth + " , viewHeight :: " + viewHeight)
                            var param = textViewMessage.layoutParams
                            param.width = viewWidth
                            param.height = viewHeight
                            textViewMessage.layoutParams = param

                            var baseHeight = AppPreferences.getScreenHeight(context!!) - Utils.ConvertDpToPx(context!!, 89) - Utils.GetStatusBarHeight(context!!)
                            if ( viewHeight >= baseHeight ) {
                                var s_param = scroll.layoutParams
                                s_param.width = viewWidth
                                s_param.height = baseHeight
                                scroll.layoutParams = s_param
                            }

                            textViewMessage.setImageBitmap(resource)
                        }
                    })

            d
        }, null) as Spannable

        for (u in span.getSpans(0, span.length, URLSpan::class.java)) {
            span.setSpan(object : UnderlineSpan() {
                override fun updateDrawState(tp: TextPaint) {
                    tp.isUnderlineText = false
                }
            }, span.getSpanStart(u), span.getSpanEnd(u), 0)
        }

        textViewMessage.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (mUrl.isNotEmpty()) {
                    Logger.d("URL : $mUrl")

                    if (mUrl.startsWith("http")
                            || mUrl.startsWith("https")
                            || mUrl.startsWith("HTTP")
                            || mUrl.startsWith("HTTPS")) {
                        try {
                            mContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mUrl)))
                        } catch (e:ActivityNotFoundException) {
                            AppToast(mContext).showToastMessage("No activity found...",
                                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                                    AppToast.GRAVITY_BOTTOM)
                        }
                    } else {
                        AppLinkDelegate.getInstance(mContext).process(AppConstants.APP_LINK_SCHEME, mUrl)
                    }
                }
            }

        })

        /**
        textViewMessage.text = span

        textViewMessage.setMovementMethod(object:LinkMovementMethod() {
        override fun onTouchEvent(widget: TextView?, buffer: Spannable?, event: MotionEvent?): Boolean {
        val action = event?.action

        if (action == MotionEvent.ACTION_UP) {
        if(mUrl.isNotEmpty()) {
        Logger.d("URL : $mUrl")

        if(mUrl.startsWith("http")
        || mUrl.startsWith("https")
        || mUrl.startsWith("HTTP")
        || mUrl.startsWith("HTTPS")) {
        mContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mUrl)))
        } else {
        AppLinkDelegate.getInstance(mContext).process(AppConstants.APP_LINK_SCHEME, mUrl)
        }

        return true
        }
        }
        return super.onTouchEvent(widget, buffer, event)
        }
        })
         **/
        checkBoxNotShowToday.isChecked = false

        checkBoxNotShowToday.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                closeButtonClicked()
            }
        }

        buttonClose.setOnClickListener { closeButtonClicked() }
    }

    private fun closeButtonClicked() {
        if (checkBoxNotShowToday.isChecked) {
            val notShow = AppPreferences.getNotificationNotShowList(mContext)
            var jArray: JSONArray?

            try {
                jArray = JSONArray(notShow)
            } catch (e: Exception) {
                Logger.p(e)
                jArray = JSONArray()
            }

            try {
                JSONObject().apply {
                    put("title", mTitle)
                    put("content", mMessage)
                    put("link", mUrl)
                    put("no", mNo)
                    put("reserve_day", mUpdateDate)
                    put("not_show", System.currentTimeMillis())
                }.let {
                    jArray!!.put(it)
                }

                AppPreferences.setNotificationNotShowList(mContext, jArray.toString())
            } catch (e: Exception) {
                Logger.p(e)
            }
        }

        dismiss()
    }

    inner class LoadImage(private val mTv: AppCompatTextView) : AsyncTask<Any, Void, Bitmap>() {

        private var mDrawable: LevelListDrawable? = null

        override fun doInBackground(vararg params: Any): Bitmap? {
            val source = params[0] as String
            mDrawable = params[1] as LevelListDrawable
            Logger.d("doInBackground $source")

            try {
                val inputStream = URL(source).openStream()
                return BitmapFactory.decodeStream(inputStream)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            Logger.d("mDrawable : $mDrawable")
            Logger.d("onPostExecute bitmap : $bitmap!!")
            if (bitmap != null) {
                val newBitmap = resizeBitmapImageFn(bitmap, mTv.width)
                if (newBitmap != null) {
                    val d = BitmapDrawable(mContext.resources, newBitmap)
                    mDrawable!!.addLevel(1, 1, d)
                    mDrawable!!.setBounds(0, 0, newBitmap.width, newBitmap.height)
                    mDrawable!!.level = 1
                }

                val t = mTv.text
                mTv.text = t

                mTv.requestLayout()
                mTv.invalidate()
            }
        }


        fun resizeBitmapImageFn(bmpSource: Bitmap, maxResolution: Int): Bitmap? {
            val iWidth = bmpSource.width      //비트맵이미지의 넓이
            val iHeight = bmpSource.height     //비트맵이미지의 높이
            var newWidth = iWidth
            var newHeight = iHeight
            var rate = 0.0f

            try {
                rate = maxResolution.toFloat() / iWidth.toFloat()

                newHeight = (iHeight * rate).toInt()
                newWidth = maxResolution

                return if (newHeight > 0 && newWidth > 0) {
                    Bitmap.createScaledBitmap(bmpSource, newWidth, newHeight, true)
                } else {
                    null
                }
            } catch (e: Exception) {
                Logger.p(e)
                return null
            }
        }
    }
}