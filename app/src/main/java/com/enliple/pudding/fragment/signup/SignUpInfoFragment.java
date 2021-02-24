package com.enliple.pudding.fragment.signup;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.enliple.pudding.AbsBaseFragment;
import com.enliple.pudding.R;
import com.enliple.pudding.activity.AgreementActivity;
import com.enliple.pudding.activity.SignUpActivity;
import com.enliple.pudding.api.AccountPolicy;
import com.enliple.pudding.commons.events.OnSingleClickListener;
import com.enliple.pudding.commons.internal.AppPreferences;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.NetworkHandler;
import com.enliple.pudding.commons.network.vo.BaseAPI;
import com.enliple.pudding.commons.ui_compat.AsteriskPasswordTransformationMethod;
import com.enliple.pudding.commons.widget.toast.AppToast;
import com.enliple.pudding.widget.ProgressLoading;
import com.enliple.pudding.widget.shoptree.AgeSelectDialog;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class SignUpInfoFragment extends AbsBaseFragment {
    private static final int DRAWABLE_RES_INPUT_NORMAL = R.drawable.input_form_normal;
    private static final int DRAWABLE_RES_INPUT_ERROR = R.drawable.input_form_error;
    private static final int AUTH_CODE_MIN_LENGTH = 6;
    private static final long SECONDS = 1000L;
    private static final long MINUTE = SECONDS * 60;
    private static final long AUTH_NUMBER_REQUEST_COUNTDOWN_TIME = MINUTE * 5;           // 인증번호 입력 제한시간
    private static final long COUNT_DOWN_DEFAULT_TICK = SECONDS;
    private AppCompatImageView imageViewStatus2;
    private AppCompatEditText editTextPassword, editTextPasswordConfirm, editTextAccount, editTextEmail, editTextAuthNumber, editTextCellPhone;
    private AppCompatTextView submit;
    private AppCompatCheckBox checkBoxTerms, checkBoxPrivacyUse, checkBoxPrivacyConsignment;
    private LinearLayout layoutTermOfUseDetail, layoutPrivacyUseDetail, layoutPrivacyConsignmentDetail, manLayer, womanLayer, checkBoxAll;
    private AppCompatTextView textViewAccountError, textViewPasswordError, textViewPasswordConfirmError, textViewEmailError, textViewSMSConfirmError, textAge, textViewCountDown;
    private AppCompatImageView manImage, womanImage, allImage;
    private ProgressLoading progressLoading;
    private AppCompatButton buttonSend;
    private boolean isPhoneAuthEnabled = false;
    private CountDownTimer countDownTimer = null;
//    private AppCompatSpinner spinner;

    private View errorFocusView = null;
    private Dialog alertDialog = null;

    private ArrayList<String> spinnerData = new ArrayList<String>();
    private String selectedGender = "M";
    private boolean isAllClicked = false;
//    private String age = "";
    private String selectedAge = "";
    private void setLayout(View v) {
        imageViewStatus2 = v.findViewById(R.id.imageViewStatus2);
        editTextAccount = v.findViewById(R.id.editTextAccount);
        editTextEmail = v.findViewById(R.id.editTextEmail);
        editTextPassword = v.findViewById(R.id.editTextPassword);
        editTextPasswordConfirm = v.findViewById(R.id.editTextPasswordConfirm);
        submit = v.findViewById(R.id.submit);
        checkBoxAll = v.findViewById(R.id.checkBoxAll);
        allImage = v.findViewById(R.id.allImage);
        checkBoxTerms = v.findViewById(R.id.checkBoxTerms);
        checkBoxPrivacyUse = v.findViewById(R.id.checkBoxPrivacyUse);
        checkBoxPrivacyConsignment = v.findViewById(R.id.checkBoxPrivacyConsignment);
        layoutTermOfUseDetail = v.findViewById(R.id.layoutTermOfUseDetail);
        layoutPrivacyUseDetail = v.findViewById(R.id.layoutPrivacyUseDetail);
        layoutPrivacyConsignmentDetail = v.findViewById(R.id.layoutPrivacyConsignmentDetail);
        textViewAccountError = v.findViewById(R.id.textViewAccountError);
        textViewPasswordError = v.findViewById(R.id.textViewPasswordError);
        textViewPasswordConfirmError = v.findViewById(R.id.textViewPasswordConfirmError);
        textViewEmailError = v.findViewById(R.id.textViewEmailError);
        manLayer = v.findViewById(R.id.manLayer);
        womanLayer = v.findViewById(R.id.womanLayer);
        manImage = v.findViewById(R.id.manImage);
        womanImage = v.findViewById(R.id.womanImage);
        progressLoading = v.findViewById(R.id.progressLoading);
        textAge = v.findViewById(R.id.textAge);

        editTextAuthNumber = v.findViewById(R.id.editTextAuthNumber);
        editTextCellPhone = v.findViewById(R.id.editTextCellPhone);
        textViewCountDown = v.findViewById(R.id.textViewCountDown);
        buttonSend = v.findViewById(R.id.buttonSend);
        textViewSMSConfirmError = v.findViewById(R.id.textViewSMSConfirmError);
//        spinner = v.findViewById(R.id.spinner);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.signup_info_fragment, null, false);
        setLayout(v);
        return v;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageViewStatus2.setSelected(true);
        changeGenderUI();
        editTextPassword.setTransformationMethod(new AsteriskPasswordTransformationMethod());
        editTextPasswordConfirm.setTransformationMethod(new AsteriskPasswordTransformationMethod());

        checkBoxTerms.setOnCheckedChangeListener(checkedChangeListener);
        checkBoxPrivacyUse.setOnCheckedChangeListener(checkedChangeListener);
        checkBoxPrivacyConsignment.setOnCheckedChangeListener(checkedChangeListener);
//        toggleOnCheckedChangedListener(true);

        layoutTermOfUseDetail.setOnClickListener(clickListener);
        layoutPrivacyUseDetail.setOnClickListener(clickListener);
        layoutPrivacyConsignmentDetail.setOnClickListener(clickListener);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubmit();
            }
        });

        editTextAccount.addTextChangedListener(accountChangeListener);
        editTextPassword.addTextChangedListener(pwChangeListener);
        editTextPasswordConfirm.addTextChangedListener(pwConfirmListener);
        editTextEmail.addTextChangedListener(mailConfirmListener);
        editTextCellPhone.addTextChangedListener(authRequestChangeListener);
        editTextAuthNumber.addTextChangedListener(authInputChangeListener);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cellPhoneNumber = editTextCellPhone.getText().toString();
                if (!TextUtils.isEmpty(cellPhoneNumber)) {
                    requestAuthNumber(cellPhoneNumber);
                }
            }
        });
        manLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGender = "M";
                changeGenderUI();
            }
        });

        womanLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGender = "F";
                changeGenderUI();
            }
        });

        checkBoxAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( isAllClicked ) {
                    isAllClicked =false;
                    allImage.setBackgroundResource(R.drawable.checkbox_white_off);
                    checkBoxTerms.setChecked(false);
                    checkBoxPrivacyUse.setChecked(false);
                    checkBoxPrivacyConsignment.setChecked(false);
                } else {
                    isAllClicked = true;
                    allImage.setBackgroundResource(R.drawable.checkbox_on);
                    checkBoxTerms.setChecked(true);
                    checkBoxPrivacyUse.setChecked(true);
                    checkBoxPrivacyConsignment.setChecked(true);
                }
            }
        });

        textAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( spinnerData != null && spinnerData.size() > 0 ) {
                    if (!TextUtils.isEmpty(textAge.getText().toString()))
                        selectedAge = textAge.getText().toString();
                    AgeSelectDialog dialog = new AgeSelectDialog(getActivity(), spinnerData, selectedAge, new AgeSelectDialog.Listener() {
                        @Override
                        public void onProductItem(@NotNull String item) {
                            textAge.setTextColor(Color.parseColor("#192028"));
                            textAge.setText(item);
                        }
                    });
                    dialog.show();
                }
//                if (orderList != null && orderList.isNotEmpty()) {
//                    val dialog = ProductCheckDialog(this@CenterAskingActivity, orderList).apply {
//                        setListener(this@CenterAskingActivity)
//                    }
//                    dialog.show()
            }
        });

//        spinner.setOnItemSelectedListener(spinnerItemListener);
        setAgeData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        String API76 = NetworkHandler.Companion.getInstance(getActivity()).getKey(NetworkApi.API76.toString(), "", "") + editTextAccount.getText().toString();
        Logger.e("onMessageEvent API76 :: $API76");
        Logger.e("onMessageEvent data.arg1 :: " + data.arg1);
        Logger.e("NetworkApi.API154.toString() :: " + NetworkApi.API154.toString());
        if( data.arg1.equals(API76) ) {
            handleNetworkResult(data);
        } else if ( data.arg1.equals(NetworkApi.API154.toString()) ) {
            Logger.e("data.arg2 :: " + data.arg2 + ", data.arg4 :: " + data.arg4);
            if("ok".equals(data.arg2)) {
                editTextAuthNumber.setEnabled(true);
                startAuthNumberCountDown();
            } else {
                try {
                    editTextAuthNumber.setEnabled(false);
                    JSONObject response = new JSONObject(data.arg4);
                    String message = response.optString("message");
                    new AppToast(getActivity()).showToastMessage(message, AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_BOTTOM);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void handleNetworkResult(NetworkBusResponse data) {
        Logger.e("handleNetworkResult data.arg2 :: " + data.arg2);
        if ("ok".equals(data.arg2)) {
            AppPreferences.Companion.setUserId(getActivity(), editTextAccount.getText().toString());
            AppPreferences.Companion.setUserPw(getActivity(), editTextPassword.getText().toString());
            AppPreferences.Companion.setUserLoginType(getActivity(), AppPreferences.LOGIN_TYPE_AUTO);
            AppPreferences.Companion.setUserAccountType(getActivity(), AppPreferences.USER_ACCOUNT_TYPE_ACCOUNT);

            progressLoading.setVisibility(View.INVISIBLE);
            ((SignUpActivity) getActivity()).replaceCompleteFragment();
        } else {
            BaseAPI errorResult = new Gson().fromJson(data.arg4, BaseAPI.class);
            Logger.e("$errorResult");

            progressLoading.setVisibility(View.INVISIBLE);

            if ( alertDialog != null )
                alertDialog.dismiss();
            alertDialog = new AlertDialog.Builder(getActivity())
                    .setMessage(errorResult.message)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        // 여기서 이미 등록된 사용자입니다 동작 해야할듯
                        dialog.dismiss();
                    }).create();
            alertDialog.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if ( buttonView.getId() == R.id.checkBoxTerms ) {
                if ( isChecked ) {
                    if ( checkBoxPrivacyUse.isChecked() && checkBoxPrivacyConsignment.isChecked() ) {
                        isAllClicked = true;
                        allImage.setBackgroundResource(R.drawable.checkbox_on);
                    } else {
                        isAllClicked = false;
                        allImage.setBackgroundResource(R.drawable.checkbox_white_off);
                    }
                } else {
                    isAllClicked = false;
                    allImage.setBackgroundResource(R.drawable.checkbox_white_off);
                }
            } else if ( buttonView.getId() == R.id.checkBoxPrivacyUse ) {
                if ( isChecked ) {
                    if ( checkBoxTerms.isChecked() && checkBoxPrivacyConsignment.isChecked() ) {
                        isAllClicked = true;
                        allImage.setBackgroundResource(R.drawable.checkbox_on);
                    } else {
                        isAllClicked = false;
                        allImage.setBackgroundResource(R.drawable.checkbox_white_off);
                    }
                } else {
                    isAllClicked = false;
                    allImage.setBackgroundResource(R.drawable.checkbox_white_off);
                }
            } else if ( buttonView.getId() == R.id.checkBoxPrivacyConsignment ) {
                if ( isChecked ) {
                    if ( checkBoxTerms.isChecked() && checkBoxPrivacyUse.isChecked() ) {
                        isAllClicked = true;
                        allImage.setBackgroundResource(R.drawable.checkbox_on);
                    } else {
                        isAllClicked = false;
                        allImage.setBackgroundResource(R.drawable.checkbox_white_off);
                    }
                } else {
                    isAllClicked = false;
                    allImage.setBackgroundResource(R.drawable.checkbox_white_off);
                }
            }
            submit.setSelected(isAllInputInformation());
        }
    };

    private OnSingleClickListener clickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch ( v.getId() ) {
//                case R.id.buttonSubmit:
//                    handleSubmit();
//                    break;
                case R.id.layoutTermOfUseDetail:
                    Intent intent = new Intent(getActivity(), AgreementActivity.class);
                    intent.putExtra(AgreementActivity.INTENT_EXTRA_KEY_MODE,
                            AgreementActivity.INTENT_EXTRA_VALUE_TERM);
                    startActivity(intent);
                    break;
                case R.id.layoutPrivacyUseDetail:
                    Intent intent1 = new Intent(getActivity(), AgreementActivity.class);
                    intent1.putExtra(AgreementActivity.INTENT_EXTRA_KEY_MODE,
                            AgreementActivity.INTENT_EXTRA_VALUE_PRIVACY_USE);
                    startActivity(intent1);
                    break;
                case R.id.layoutPrivacyConsignmentDetail:
                    Intent intent2 = new Intent(getActivity(), AgreementActivity.class);
                    intent2.putExtra(AgreementActivity.INTENT_EXTRA_KEY_MODE,
                            AgreementActivity.INTENT_EXTRA_VALUE_PRIVACY_CONSIGNMENT);
                    startActivity(intent2);
                    break;
            }
        }
    };

    private TextWatcher accountChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            submit.setEnabled(isAllInputInformation());
            String inputAccount = editTextAccount.getText().toString();
            if ( !AccountPolicy.Companion.IsIdMatched(inputAccount) ) {
                Logger.e("id not matched");
                textViewAccountError.setText(R.string.msg_signup_error_bad_type_account);
                textViewAccountError.setVisibility(View.VISIBLE);
                editTextAccount.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR);
            } else {
                textViewAccountError.setVisibility(View.GONE);
                editTextAccount.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher pwChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            submit.setEnabled(isAllInputInformation());
            String pw = editTextPassword.getText().toString();
            if ( !AccountPolicy.Companion.IsPasswordMatched(pw) ) {
                Logger.e("pw not matched");
                textViewPasswordError.setText(R.string.msg_signup_error_bad_type_password);
                textViewPasswordError.setVisibility(View.VISIBLE);
                editTextPassword.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR);
            } else {
                textViewPasswordError.setVisibility(View.GONE);
                editTextPassword.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher pwConfirmListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            submit.setEnabled(isAllInputInformation());
            String pw = editTextPassword.getText().toString();
            String pwConfirm = editTextPasswordConfirm.getText().toString();
            Logger.e("pw :: " + pw);
            Logger.e("pwc:: " + pwConfirm);
            if (!pw.equals(pwConfirm)) {
                Logger.e("pw not same");
                textViewPasswordConfirmError.setText(R.string.msg_signup_error_mismatch_password);
                textViewPasswordConfirmError.setVisibility(View.VISIBLE);
                editTextPasswordConfirm.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR);
            } else {
                textViewPasswordConfirmError.setVisibility(View.GONE);
                editTextPasswordConfirm.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher mailConfirmListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            submit.setEnabled(isAllInputInformation());
            String email = editTextEmail.getText().toString();
//            if (!AccountPolicy.emailPattern.matcher(email).matches()) {
            if ( !AccountPolicy.Companion.IsEMailMatched(email) ) {
                Logger.e("email no not matched");
                textViewEmailError.setText(R.string.msg_signup_error_bad_type_email);
                textViewEmailError.setVisibility(View.VISIBLE);
                editTextEmail.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR);
            } else {
                textViewEmailError.setVisibility(View.GONE);
                editTextEmail.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher authRequestChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            boolean cellPhoneNotNotNull = false;
            if ( TextUtils.isEmpty(editTextCellPhone.getText().toString()))
                cellPhoneNotNotNull = false;
            else
                cellPhoneNotNotNull = true;

            buttonSend.setEnabled(cellPhoneNotNotNull);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher authInputChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            isPhoneAuthEnabled = false;
            if ( editTextAuthNumber.getText().toString().length() >= AUTH_CODE_MIN_LENGTH )
                isPhoneAuthEnabled = true;
            else
                isPhoneAuthEnabled = false;
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    /**
     * 회원가입 정보가 모두 입력된 상태인지 확인
     * @return
     */
    private boolean isAllInputInformation() {
        boolean result = false;
        boolean accountInput = !TextUtils.isEmpty(editTextAccount.getText().toString());
        boolean password1 = !TextUtils.isEmpty(editTextPassword.getText().toString());
        boolean password2 = !TextUtils.isEmpty(editTextPasswordConfirm.getText().toString());
        boolean email = !TextUtils.isEmpty(editTextEmail.getText().toString());
        boolean availableAge =  !TextUtils.isEmpty(textAge.getText().toString());
        boolean isCheckedAgreement = checkBoxTerms.isChecked() && checkBoxPrivacyUse.isChecked() && checkBoxPrivacyConsignment.isChecked();
        boolean isCellPhoneAuth = editTextAuthNumber.isEnabled();
        Logger.e("accountInput :: " + accountInput);
        Logger.e("password1 :: " + password1);
        Logger.e("password2 :: " + password2);
        Logger.e("email :: " + email);
        Logger.e("availableAge :: " + availableAge);
        Logger.e("checkBoxTerms :: " + checkBoxTerms.isChecked());
        Logger.e("checkBoxPrivacyUse :: " + checkBoxPrivacyUse.isChecked());
        Logger.e("checkBoxPrivacyConsignment :: " + checkBoxPrivacyConsignment.isChecked());
        Logger.e("isCheckedAgreement :: " + isCheckedAgreement);
        result = accountInput && password1 && password2 && email && availableAge && isCheckedAgreement && isCellPhoneAuth;
        if (result) {
            submit.setBackgroundColor(Color.parseColor("#9f56f2"));
        } else {
            submit.setBackgroundColor(Color.parseColor("#d9e1eb"));
        }
        Logger.e("result ;: " + result);
        return result;
    }

    /**
     * 회원가입 처리 핸들링 및 입력 양식 검사, 요청
     */
    private void handleSubmit() {
        Logger.e("handleSubmit");
        if  ( !isAllInputInformation() )
            return;
        resetErrorText();

        String inputAccount = editTextAccount.getText().toString();
        String pw = editTextPassword.getText().toString();
        String pwConfirm = editTextPasswordConfirm.getText().toString();
        String email = editTextEmail.getText().toString();
        String phoneNo = editTextCellPhone.getText().toString();
        // 아이디 형식검증
//        if (inputAccount!!.length < AccountPolicy.ACCOUNT_MINIMUM_LENGTH
//                || inputAccount!!.length > AccountPolicy.ACCOUNT_MAXIMUM_LENGTH
//                || AccountPolicy.accountSpecialPattern.matcher(inputAccount).matches()
//                || !AccountPolicy.numberPattern.matcher(inputAccount).matches()
//                || !(AccountPolicy.lowerCasePattern.matcher(inputAccount).matches()
//                        || AccountPolicy.upperCasePattern.matcher(inputAccount).matches())) {
        if ( !AccountPolicy.Companion.IsIdMatched(inputAccount) ) {
            Logger.e("id not matched");
            textViewAccountError.setText(R.string.msg_signup_error_bad_type_account);
            textViewAccountError.setVisibility(View.VISIBLE);
            editTextAccount.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR);
            editTextAccount.requestFocus();
            errorFocusView = editTextAccount;
        }
        else if ( !AccountPolicy.Companion.IsPasswordMatched(pw) ) {
            Logger.e("pw not matched");
            textViewPasswordError.setText(R.string.msg_signup_error_bad_type_password);
            textViewPasswordError.setVisibility(View.VISIBLE);
            editTextPassword.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR);
            editTextPassword.requestFocus();
            errorFocusView = editTextPassword;
        }

        // 비밀번호 - 비밀번호 확인 비교
        else if (!pw.equals(pwConfirm)) {
            Logger.e("pw not same");
            textViewPasswordConfirmError.setText(R.string.msg_signup_error_mismatch_password);
            textViewPasswordConfirmError.setVisibility(View.VISIBLE);;
            editTextPasswordConfirm.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR);
            editTextPasswordConfirm.requestFocus();
            errorFocusView = editTextPasswordConfirm;
        }

        // 이메일 확인
//        else if (!AccountPolicy.emailPattern.matcher(email).matches()) {
        else if ( !AccountPolicy.Companion.IsEMailMatched(email) ) {
            Logger.e("email no not matched");
            textViewEmailError.setText(R.string.msg_signup_error_bad_type_email);
            textViewEmailError.setVisibility(View.VISIBLE);
            editTextEmail.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR);
            editTextEmail.requestFocus();
            errorFocusView = editTextEmail;
        }

        // 약관동의 상태 확인
        else if (!(checkBoxTerms.isChecked() && checkBoxPrivacyUse.isChecked() && checkBoxPrivacyConsignment.isChecked())) {
            Logger.e("agree not checked");
            if ( alertDialog != null )
                alertDialog.dismiss();
            alertDialog = null;
            alertDialog = new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.msg_signup_error_not_agree_mandatory)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        dialog.dismiss();
                    }).create();
            alertDialog.show();
        } else if ( phoneNo.length() < 10 ) {
            if ( alertDialog != null )
                alertDialog.dismiss();
            alertDialog = null;
            alertDialog = new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.msg_signup_error_bad_type_cellphone1)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        dialog.dismiss();
                    }).create();
            alertDialog.show();
        } else if (!AccountPolicy.Companion.IsPhoneMatched(phoneNo) ) {
            if ( alertDialog != null )
                alertDialog.dismiss();
            alertDialog = null;
            alertDialog = new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.msg_signup_error_bad_type_cellphone)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        dialog.dismiss();
                    }).create();
            alertDialog.show();
        } else if ( !isPhoneAuthEnabled ) {
            if ( alertDialog != null )
                alertDialog.dismiss();
            alertDialog = null;
            alertDialog = new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.msg_signup_error_bad_type_auth)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        dialog.dismiss();
                    }).create();
            alertDialog.show();
        } else {
            requestSignUp();
        }
    }

    /**
     * 현재 표시되고 있는 Error Text 문구를 모두 초기화
     */
    private void resetErrorText() {
        textViewAccountError.setText("");
        textViewEmailError.setText("");
        textViewPasswordError.setText("");
        textViewPasswordConfirmError.setText("");
        textViewSMSConfirmError.setText("");
        textViewAccountError.setVisibility(View.GONE);
        textViewEmailError.setVisibility(View.GONE);
        textViewPasswordError.setVisibility(View.GONE);
        textViewPasswordConfirmError.setVisibility(View.GONE);

        editTextAccount.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL);
        editTextEmail.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL);
        editTextPasswordConfirm.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL);
        editTextPassword.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL);
        if ( errorFocusView != null )
            errorFocusView.clearFocus();
        errorFocusView = null;
    }

    /**
     * 양식내 입력된 데이터를 이용한 회원가입 요청
     */
    private void requestSignUp() {
        progressLoading.setVisibility(View.VISIBLE);
        try {
            JSONObject requestObj = new JSONObject();
            requestObj.put("userId", editTextAccount.getText().toString());
            requestObj.put("passw", editTextPassword.getText().toString());
            requestObj.put("userEmail", editTextEmail.getText().toString());
            requestObj.put("userSex", selectedGender);
            requestObj.put("userAge", textAge.getText().toString());
            requestObj.put("userHp", editTextCellPhone.getText().toString());
            requestObj.put("userCert", editTextAuthNumber.getText().toString());
//            requestObj.put("certify", if (certType == BUNDLE_EXTRA_VALUE_CERTIFICATION_TYPE_CELLPHONE) "hp" else "")
//            requestObj.put("userName", certName)
//            requestObj.put("nAdult", nAdult)
//            requestObj.put("strBirth", strBirth)
//            requestObj.put("strSex", strSex)
//            requestObj.put("strCheckHash", strCheckHash)
//            requestObj.put("strAuthKey", dupInfo)

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObj.toString());
            NetworkBus bus = new NetworkBus(NetworkApi.API76.name(), editTextAccount.getText().toString(), body);
            EventBus.getDefault().post(bus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeGenderUI() {
        if ( "M".equals(selectedGender) ) {
            manImage.setBackgroundResource(R.drawable.radio_btn_on);
            womanImage.setBackgroundResource(R.drawable.radio_btn_off);
        } else {
            manImage.setBackgroundResource(R.drawable.radio_btn_off);
            womanImage.setBackgroundResource(R.drawable.radio_btn_on);
        }
    }

    private void setAgeData() {
        for ( int i = 14 ; i < 71 ; i ++ ) {
            spinnerData.add(i+"");
        }
    }

    private void requestAuthNumber(String cellPhoneNumber) {
        try {


            JSONObject object = new JSONObject();
            object.put("cert_val", cellPhoneNumber);
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), object.toString());
            NetworkBus bus = new NetworkBus(NetworkApi.API154.name(), body);
            EventBus.getDefault().post(bus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 카운트 다운을 취소
     */
    private void cancelCountDown() {
        if ( countDownTimer != null )
            countDownTimer.cancel();
        countDownTimer = null;
        textViewCountDown.setText("");
    }

    /**
     * 인증번호를 요청하여 서버에서 응답이후 CountDown 을 수행하고 CountDown 이 종료되면 UI 상태를 변경
     */
    private void startAuthNumberCountDown() {
        cancelCountDown();
        textViewSMSConfirmError.setVisibility(View.GONE);
        long countDownMinute = AUTH_NUMBER_REQUEST_COUNTDOWN_TIME / MINUTE;
        long countDownSeconds =  0;
        if (AUTH_NUMBER_REQUEST_COUNTDOWN_TIME - countDownMinute == 0L) {
            countDownSeconds = 0;
        } else {
            countDownSeconds = (AUTH_NUMBER_REQUEST_COUNTDOWN_TIME - countDownMinute) / SECONDS;
        }
        textViewCountDown.setText(String.format("%02d", countDownMinute) + ":" + String.format("%02d", countDownSeconds));
        buttonSend.setEnabled(false);
        countDownTimer = new CountDownTimer(AUTH_NUMBER_REQUEST_COUNTDOWN_TIME, COUNT_DOWN_DEFAULT_TICK) {
            long timeRemain = 0L;
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = (millisUntilFinished / SECONDS);
                long minute = seconds / 60;
                seconds -= (minute * 60);
                String remain = String.format("%02d", minute) + ":" + String.format("%02d", seconds);
                textViewCountDown.setText(remain);
                timeRemain = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                buttonSend.setText(R.string.msg_find_account_retry_auth_number);
                buttonSend.setEnabled(true);
                editTextAuthNumber.setText("");
                editTextAuthNumber.setEnabled(false);
                textViewSMSConfirmError.setVisibility(View.VISIBLE);
            }
        }.start();
    }
}
