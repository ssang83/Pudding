/**
 * The MIT License (MIT) Copyright (c) 2015 OriginQiu Permission is hereby
 * granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions: The above copyright notice and this
 * permission notice shall be included in all copies or substantial portions of
 * the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package com.enliple.pudding.shoppingcaster.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.log.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.originqiu.library.FlowLayout;

/**
 * Created by OriginQiu on 4/7/16.
 */
public class EditTag extends FrameLayout
        implements View.OnClickListener, TextView.OnEditorActionListener, View.OnKeyListener {

    private FlowLayout flowLayout;

    private EditText editText;

    private int tagViewLayoutRes;

    private int inputTagLayoutRes;

    private int deleteModeBgRes;

    private Drawable defaultTagBg;

    private boolean isEditableStatus = true;

    private TextView lastSelectTagView;

    private List<String> tagValueList = new ArrayList<>();

    private boolean isDelAction = false;

    private TagAddCallback tagAddCallBack;

    private TagDeletedCallback tagDeletedCallback;
    private CharSequence seq;

    public interface TagAddCallback {
        /*
         * Called when add a tag
         * true: tag would be added
         * false: tag would not be added
         */
        boolean onTagAdd(String tagValue);
    }

    public interface TagDeletedCallback {
        /**
         * Called when tag be deleted
         *
         * @param deletedTagValue
         */
        void onTagDelete(String deletedTagValue);
    }

    public EditTag(Context context) {
        this(context, null);
    }

    public EditTag(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditTag(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.EditTag);
        tagViewLayoutRes =
                mTypedArray.getResourceId(R.styleable.EditTag_tag_layout, R.layout.view_default_tag);
        inputTagLayoutRes = mTypedArray.getResourceId(R.styleable.EditTag_input_layout,
                R.layout.view_default_input_tag);
        deleteModeBgRes =
                mTypedArray.getResourceId(R.styleable.EditTag_delete_mode_bg, R.color.colorAccent);
        mTypedArray.recycle();
        setupView();
    }

    private void setupView() {
        flowLayout = new FlowLayout(getContext());
        LayoutParams layoutParams =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        flowLayout.setLayoutParams(layoutParams);
        addView(flowLayout);
        addInputTagView();
    }

    private void addInputTagView() {
        editText = createInputTag(flowLayout);
        editText.setTag(new Object());
        editText.setOnClickListener(this);
        setupListener();
        flowLayout.addView(editText);
        isEditableStatus = true;
    }

    private void setupListener() {
        editText.setOnEditorActionListener(this);
        editText.setOnKeyListener(this);
        editText.setFilters(new InputFilter[]{filter});
    }

    private InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Logger.e("source :: " + source.toString());
            if (source == null)
                return null;
            seq = source;
            Logger.e("editText str length :: " + editText.getText().toString().length());
            Logger.e("edit str :: " + editText.getText().toString());
            if ((source.toString().equals("#") && editText.getText().toString().replaceAll("#", "").length() >=1) || source.toString().equals(" ") ) {
                if (tagValueList.size() >= 5) {
                    editText.setText("");
                    return null;
                }

                int startSelection = editText.getSelectionStart();
                int endSelection = editText.getSelectionEnd();
                editText.setText(editText.getText().toString().trim());
                editText.setSelection(startSelection, endSelection);

                String tagContent = editText.getText().toString();
                String tempStr = tagContent.replaceAll("#", "");
                tempStr = tempStr.replaceAll(" ", "");
                if (tempStr != null && tempStr.length() <= 0)
                    return null;
                if (!tagContent.startsWith("#"))
                    tagContent = "#" + tagContent;
                editText.setText(tagContent);
                // 기존 저장되어 있는 tag와 동일하다면 tag 추가하지 않음
                for (int i = 0; i < getTagList().size(); i++) {
                    String val = getTagList().get(i).replaceAll("#", "");
                    String content = tagContent.replaceAll("#", "");
                    if (val.equals(content)) {
                        editText.setText("");
                        return null;
                    }
                }
                if (tagAddCallBack == null || (tagAddCallBack != null
                        && tagAddCallBack.onTagAdd(tagContent))) {
                    TextView tagTextView = createTag(flowLayout, tagContent);
                    if (defaultTagBg == null) {
                        defaultTagBg = tagTextView.getBackground();
                    }
                    tagTextView.setOnClickListener(EditTag.this);
                    flowLayout.addView(tagTextView, flowLayout.getChildCount() - 1);
                    tagValueList.add(tagContent);
                    // reset action status
                    editText.getText().clear();
                    editText.performClick();
                    isDelAction = false;

                    if (tagValueList.size() > 0) {
                        editText.setHint("");
                    }
                }
                for ( int i = 0 ; i < tagValueList.size() ; i ++ ) {
                    Logger.e("tagValueList ::::: " + tagValueList.get(i));
                }
            }
            return null;
        }
    };

//    private InputFilter filter = new InputFilter() {
//        @Override
//        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
//            if (source == null)
//                return null;
//            if (source.toString().equals(" ")) {
//                if (tagValueList.size() >= 5) {
//                    editText.setText("");
//                    return null;
//                }
//
//                int startSelection = editText.getSelectionStart();
//                int endSelection = editText.getSelectionEnd();
//                editText.setText(editText.getText().toString().trim());
//                editText.setSelection(startSelection, endSelection);
//
//                String tagContent = editText.getText().toString();
//                String tempStr = tagContent.replaceAll("#", "");
//                tempStr = tempStr.replaceAll(" ", "");
//                if (tempStr != null && tempStr.length() <= 0)
//                    return null;
//                if (!tagContent.startsWith("#"))
//                    tagContent = "#" + tagContent;
//                editText.setText(tagContent);
//                // 기존 저장되어 있는 tag와 동일하다면 tag 추가하지 않음
//                for (int i = 0; i < getTagList().size(); i++) {
//                    String val = getTagList().get(i).replaceAll("#", "");
//                    String content = tagContent.replaceAll("#", "");
//                    if (val.equals(content)) {
//                        editText.setText("");
//                        return null;
//                    }
//                }
//                if (tagAddCallBack == null || (tagAddCallBack != null
//                        && tagAddCallBack.onTagAdd(tagContent))) {
//                    TextView tagTextView = createTag(flowLayout, tagContent);
//                    if (defaultTagBg == null) {
//                        defaultTagBg = tagTextView.getBackground();
//                    }
//                    tagTextView.setOnClickListener(EditTag.this);
//                    flowLayout.addView(tagTextView, flowLayout.getChildCount() - 1);
//                    tagValueList.add(tagContent);
//                    // reset action status
//                    editText.getText().clear();
//                    editText.performClick();
//                    isDelAction = false;
//
//                    if (tagValueList.size() > 0) {
//                        editText.setHint("");
//                    }
//                }
//            }
//            return null;
//        }
//    };

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        boolean isHandle = false;
        if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
            String tagContent = editText.getText().toString();
            if (TextUtils.isEmpty(tagContent)) {
                int tagCount = flowLayout.getChildCount();
                if (lastSelectTagView == null && tagCount > 1) {
                    if (isDelAction) {
                        flowLayout.removeViewAt(tagCount - 2);
                        if (tagDeletedCallback != null) {
                            tagDeletedCallback.onTagDelete(tagValueList.get(tagCount - 2));
                        }
                        tagValueList.remove(tagCount - 2);
                        isHandle = true;
                        if (tagValueList.size() == 0) {
                            editText.setHint("#을 이용해 태그를 등록하세요.(최대 5개)");
                        }
                    } else {
                        TextView delActionTagView = (TextView) flowLayout.getChildAt(tagCount - 2);
//                        delActionTagView.setBackgroundDrawable(getDrawableByResId(deleteModeBgRes));
                        lastSelectTagView = delActionTagView;
                        isDelAction = true;
                    }
                } else {
                    removeSelectedTag();
                }
            } else {
                int length = tagContent.length();
                editText.getText().delete(length, length);
            }
        }
        return isHandle;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean isHandle = false;
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            try {
                if (editText.getText().toString().replaceAll("#", "").length() >=1 ) {
                    if (tagValueList.size() >= 5) {
                        editText.setText("");
                        return false;
                    }

                    int startSelection = editText.getSelectionStart();
                    int endSelection = editText.getSelectionEnd();
                    editText.setText(editText.getText().toString().trim());
                    editText.setSelection(startSelection, endSelection);

                    String tagContent = editText.getText().toString();
                    String tempStr = tagContent.replaceAll("#", "");
                    tempStr = tempStr.replaceAll(" ", "");
                    if (tempStr != null && tempStr.length() <= 0)
                        return false;
                    if (!tagContent.startsWith("#"))
                        tagContent = "#" + tagContent;
                    editText.setText(tagContent);
                    // 기존 저장되어 있는 tag와 동일하다면 tag 추가하지 않음
                    for (int i = 0; i < getTagList().size(); i++) {
                        String val = getTagList().get(i).replaceAll("#", "");
                        String content = tagContent.replaceAll("#", "");
                        if (val.equals(content)) {
                            editText.setText("");
                            return false;
                        }
                    }
                    if (tagAddCallBack == null || (tagAddCallBack != null
                            && tagAddCallBack.onTagAdd(tagContent))) {
                        TextView tagTextView = createTag(flowLayout, tagContent);
                        if (defaultTagBg == null) {
                            defaultTagBg = tagTextView.getBackground();
                        }
                        tagTextView.setOnClickListener(EditTag.this);
                        flowLayout.addView(tagTextView, flowLayout.getChildCount() - 1);
                        tagValueList.add(tagContent);
                        // reset action status
                        editText.getText().clear();
                        editText.performClick();
                        isDelAction = false;

                        if (tagValueList.size() > 0) {
                            editText.setHint("");
                        }
                        for ( int i = 0 ; i < tagValueList.size() ; i ++ ) {
                            Logger.e("tagValueList ::::: " + tagValueList.get(i));
                        }
                    }
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }


//            String tagContent = editText.getText().toString();
//            if (TextUtils.isEmpty(tagContent)) {
//                // do nothing, or you can tip "can'nt add empty tag"
//            } else {
//                if (tagAddCallBack == null || (tagAddCallBack != null
//                        && tagAddCallBack.onTagAdd(tagContent))) {
//                    TextView tagTextView = createTag(flowLayout, tagContent);
//                    if (defaultTagBg == null) {
//                        defaultTagBg = tagTextView.getBackground();
//                    }
//                    tagTextView.setOnClickListener(EditTag.this);
//                    flowLayout.addView(tagTextView, flowLayout.getChildCount() - 1);
//                    tagValueList.add(tagContent);
//                    // reset action status
//                    editText.getText().clear();
//                    editText.performClick();
//                    isDelAction = false;
//                    isHandle = true;
//                }
//            }
        }

        return true;
    }

    @Override
    public void onClick(View view) {
        if (view.getTag() == null && isEditableStatus) {
            // TextView tag click
            if (lastSelectTagView == null) {
                lastSelectTagView = (TextView) view;
//                view.setBackgroundDrawable(getDrawableByResId(deleteModeBgRes));
            } else {
                if (lastSelectTagView.equals(view)) {
                    lastSelectTagView.setBackgroundDrawable(defaultTagBg);
                    lastSelectTagView = null;
                } else {
                    lastSelectTagView.setBackgroundDrawable(defaultTagBg);
                    lastSelectTagView = (TextView) view;
//                    view.setBackgroundDrawable(getDrawableByResId(deleteModeBgRes));
                }
            }
        } else {
            // EditText tag click
            if (lastSelectTagView != null) {
                lastSelectTagView.setBackgroundDrawable(defaultTagBg);
                lastSelectTagView = null;
            }
        }
    }

    private void removeSelectedTag() {
        int size = tagValueList.size();
        if (size > 0 && lastSelectTagView != null) {
            int index = flowLayout.indexOfChild(lastSelectTagView);
            tagValueList.remove(index);
            flowLayout.removeView(lastSelectTagView);
            if (tagDeletedCallback != null) {
                tagDeletedCallback.onTagDelete(lastSelectTagView.getText().toString());
            }
            lastSelectTagView = null;
            isDelAction = false;

            if (tagValueList.size() == 0) {
                editText.setHint(getContext().getResources().getString(R.string.msg_casting_ready_hashtag));
            }
        }
    }

    private TextView createTag(ViewGroup parent, String s) {
        TextView tagTv =
                (TextView) LayoutInflater.from(getContext()).inflate(tagViewLayoutRes, parent, false);
        tagTv.setText(s);
        return tagTv;
    }

    private EditText createInputTag(ViewGroup parent) {
        editText =
                (EditText) LayoutInflater.from(getContext()).inflate(inputTagLayoutRes, parent, false);
        return editText;
    }

    private void addTagView(List<String> tagList) {
        int size = tagList.size();
        for (int i = 0; i < size; i++) {
            addTag(tagList.get(i));
        }

        if (size > 0) {
            editText.setHint("");
        }
    }

    private Drawable getDrawableByResId(int resId) {
        return getContext().getResources().getDrawable(resId);
    }

    public void setEditable(boolean editable) {
        if (editable) {
            if (!isEditableStatus) {
                flowLayout.addView((editText));
            }
        } else {
            int childCount = flowLayout.getChildCount();
            if (isEditableStatus && childCount > 0) {
                flowLayout.removeViewAt(childCount - 1);
                if (lastSelectTagView != null) {
                    lastSelectTagView.setBackgroundDrawable(defaultTagBg);
                    isDelAction = false;
                    editText.getText().clear();
                }
            }
        }
        this.isEditableStatus = editable;
    }

    public boolean addTag(String tagContent) {
        if (TextUtils.isEmpty(tagContent)) {
            // do nothing, or you can tip "can'amount add empty tag"
            return false;
        } else {
            if (tagAddCallBack == null || (tagAddCallBack != null
                    && tagAddCallBack.onTagAdd(tagContent))) {
                TextView tagTextView = createTag(flowLayout, tagContent);
                if (defaultTagBg == null) {
                    defaultTagBg = tagTextView.getBackground();
                }
                tagTextView.setOnClickListener(EditTag.this);
                if (isEditableStatus) {
                    flowLayout.addView(tagTextView, flowLayout.getChildCount() - 1);
                } else {
                    flowLayout.addView(tagTextView);
                }

                tagValueList.add(tagContent);
                // reset action status
                editText.getText().clear();
                editText.performClick();
                isDelAction = false;
                return true;
            }
        }
        return false;
    }

    public void setTagList(List<String> mTagList) {
        addTagView(mTagList);
    }

    public List<String> getTagList() {
        return tagValueList;
    }

    public void setTagAddCallBack(TagAddCallback tagAddCallBack) {
        this.tagAddCallBack = tagAddCallBack;
    }

    public void setTagDeletedCallback(TagDeletedCallback tagDeletedCallback) {
        this.tagDeletedCallback = tagDeletedCallback;
    }

    /*
     * Remove tag view by value
     * warning: this method will remove tags which has the same value
     */
    public void removeTag(String... tagValue) {
        List<String> tagValues = Arrays.asList(tagValue);
        int childCount = flowLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (tagValues.size() > 0) {
                View view = flowLayout.getChildAt(i);
                try {
                    String value = ((TextView) view).getText().toString();
                    if (tagValues.contains(value)) {
                        tagValueList.remove(value);
                        if (tagDeletedCallback != null) {
                            tagDeletedCallback.onTagDelete(value);
                        }
                        flowLayout.removeView(view);
                        i = 0;
                        childCount = flowLayout.getChildCount();
                        continue;
                    }
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
