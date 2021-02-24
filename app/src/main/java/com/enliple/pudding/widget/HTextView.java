package com.enliple.pudding.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.internal.AppPreferences;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class HTextView extends AppCompatTextView implements Html.ImageGetter {
    private Context context;
    private int space = 0;

    public HTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    private void setMargin(int space) {
        this.space = space;
    }

    /**
     * @param source HTML 형식의 문자열
     * @param space  이미지 좌우 비는 영역 합계, 값은 px값, layout에 설정한 HTextView의 좌우 마진이나 패딩값이 없을 경우 0
     */
    public void setHtmlText(String source, int space) {
        setMargin(space);
        Spanned spanned = Html.fromHtml(source, this, null);    // Html.ImageGetter 를 여기다 구현해놨다.
        this.setText(spanned);

    }

    /**
     * Html.ImageGetter 구현.
     *
     * @param source <img> 태그의 주소가 넘어온다.
     * @return 일단 LevelListDrawable 을 넘겨줘서 placeholder 처럼 보여주고, AsyncTask 를 이용해서 이미지를 다운로드
     */
    @Override
    public Drawable getDrawable(String source) {
        LevelListDrawable d = new LevelListDrawable();

        Drawable empty = ContextCompat.getDrawable(getContext(), R.drawable.empty);
        d.addLevel(0, 0, empty);
        d.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());

        new LoadImage().execute(source, d);

        return d;
    }


    /**
     * 실제 온라인에서 이미지를 다운로드 받을 AsyncTask
     */
    class LoadImage extends AsyncTask<Object, Void, Bitmap> {

        private LevelListDrawable mDrawable;

        @Override
        protected Bitmap doInBackground(Object... params) {
            String source = (String) params[0];
            mDrawable = (LevelListDrawable) params[1];

            try {
                InputStream is = new URL(source).openStream();
                return BitmapFactory.decodeStream(is);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 이미지 다운로드가 완료되면, 처음에 placeholder 처럼 만들어서 사용하던 Drawable 에, 다운로드 받은 이미지를 넣어준다.
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (bitmap != null) {
                int width, height;
                width = bitmap.getWidth();
                height = bitmap.getHeight();
                int screenWidth = AppPreferences.Companion.getScreenWidth(context);
                int baseWidth = screenWidth - space;
                if (baseWidth < bitmap.getWidth()) {
                    width = baseWidth;
                    height = (width * bitmap.getHeight()) / bitmap.getWidth();
                }

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

                BitmapDrawable d = new BitmapDrawable(getContext().getResources(), scaledBitmap);


                mDrawable.addLevel(1, 1, d);
                mDrawable.setBounds(0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
                mDrawable.setLevel(1);

                CharSequence t = getText();
                setText(t);
            }
        }
    }
}