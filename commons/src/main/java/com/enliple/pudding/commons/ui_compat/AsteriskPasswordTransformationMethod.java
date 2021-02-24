package com.enliple.pudding.commons.ui_compat;

import android.text.method.PasswordTransformationMethod;
import android.view.View;

/**
 * Created by hkcha on 2018-01-26.
 * EditText 에 입력된 Text 를 Password 표시로 출력시키는 InputMethod
 */
public class AsteriskPasswordTransformationMethod extends PasswordTransformationMethod {
    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        return new PasswordCharSequence(source);
    }

    private class PasswordCharSequence implements CharSequence {
        private CharSequence source;

        public PasswordCharSequence(CharSequence source) {
            this.source = source;
        }

        public char charAt(int index) {
            return '*';
        }

        public int length() {
            return source.length();
        }

        public CharSequence subSequence(int start, int end) {
            return source.subSequence(start, end);
        }
    }
}
