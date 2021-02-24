package com.enliple.pudding.activity

import android.os.Bundle
import androidx.appcompat.widget.AppCompatCheckBox
import android.text.TextUtils
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.CategoryModel
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppBroadcastSender
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkDBKey
import com.enliple.pudding.commons.network.vo.API81
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_main_category.*
import org.greenrobot.eventbus.EventBus

/**
 * Created by Kim Joonsung on 2018-09-28.
 */
class CategoryActivity : AbsBaseActivity() {

    companion object {
        private const val AGE_ALL = "all"
        private const val AGE_10 = "10"
        private const val AGE_20 = "20"
        private const val AGE_30 = "30"

        private const val GENDER_ALL = "all"
        private const val GENDER_MALE = "male"
        private const val GENDER_FEMALE = "female"
    }

    private lateinit var categoryTexts: Array<String>
    private val selectedCategory: MutableList<String> = ArrayList()
    private val selectedGender: MutableList<String> = ArrayList()
    private val selectedAge: MutableList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_main_category)

        categoryTexts = arrayOf(
                getString(R.string.category_all),
                getString(R.string.category_fashion),
                getString(R.string.category_beauty),
                getString(R.string.category_children),
                getString(R.string.category_food),
                getString(R.string.category_life_necessities),
                getString(R.string.category_home_deco),
                getString(R.string.category_health),
                getString(R.string.category_hobby),
                getString(R.string.category_sport),
                getString(R.string.category_car),
                getString(R.string.category_electronic),
                getString(R.string.category_digital),
                getString(R.string.category_travel_book),
                getString(R.string.category_game)
        )

        initCategory()
        initAge()
        initGender()

        close.setOnClickListener(clickListener)
        buttonNext.setOnClickListener(clickListener)
        buttonReset.setOnClickListener(clickListener)
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    private fun initCategory() {
        for (i in 0 until categoryTexts.size) {
            var checkBox = findViewById<AppCompatCheckBox>(resources.getIdentifier("checkboxCategory${i + 1}", "id", packageName))
            if (checkBox != null) {
                checkBox.isChecked = false
                checkBox.setOnCheckedChangeListener(checkedChangeListener)
            }
        }

        var ctg = AppPreferences.getHomeVideoCategory(this)
        Logger.e("initCategory: $ctg")
        if (!TextUtils.isEmpty(ctg)) {
            var category = ctg.split(",")
            category.forEach {
                if (!TextUtils.isEmpty(it)) {
                    var int = getCategoryIndex(it)
                    var checkBox = findViewById<CheckBox>(resources.getIdentifier("checkboxCategory$int", "id", packageName))
                    if (checkBox != null) {
                        checkBox.isChecked = true
                    }
                }
            }
        }
    }

    private fun initAge() {
        buttonAgeAll.setOnCheckedChangeListener(ageCheckedListener)
        buttonAge10.setOnCheckedChangeListener(ageCheckedListener)
        buttonAge20.setOnCheckedChangeListener(ageCheckedListener)
        buttonAge30.setOnCheckedChangeListener(ageCheckedListener)

        var age = AppPreferences.getCategoryAge(this)
        if (!TextUtils.isEmpty(age)) {
            var category = age.split(",")
            category.forEach {
                when (it) {
                    AGE_ALL -> buttonAgeAll.isChecked = true
                    AGE_10 -> buttonAge10.isChecked = true
                    AGE_20 -> buttonAge20.isChecked = true
                    AGE_30 -> buttonAge30.isChecked = true
                }
            }
        }
    }

    private fun initGender() {
        buttonAllGender.setOnCheckedChangeListener(genderCheckedListener)
        buttonMale.setOnCheckedChangeListener(genderCheckedListener)
        buttonFemale.setOnCheckedChangeListener(genderCheckedListener)

        var gender = AppPreferences.getCategoryGender(this)
        if (!TextUtils.isEmpty(gender)) {
            var category = gender.split(",")
            category.forEach {
                when (it) {
                    GENDER_ALL -> buttonAllGender.isChecked = true
                    GENDER_MALE -> buttonMale.isChecked = true
                    GENDER_FEMALE -> buttonFemale.isChecked = true
                }
            }
        }
    }

    /**
     * 변경된 카테고리 내용을 저장하고 현재 상태를 유지
     */
    private fun saveCategory() {
        var name = StringBuilder()
        var code = StringBuilder()

        selectedCategory.forEach {
            if ("전체" == it) {
                AppPreferences.setHomeVideoCategory(this, it)
                AppPreferences.setHomeVideoCategoryCode(this, "")
                return
            }

            name.append(",")
            name.append(it)

            code.append(",")
            code.append(getCategoryCode(it))
        }

        if (name.isEmpty()) {
            AppPreferences.setHomeVideoCategory(this, "전체")
            AppPreferences.setHomeVideoCategoryCode(this, "")
            return
        }

        var categoryName = name.toString().substring(1, name.length) // ,제거
        AppPreferences.setHomeVideoCategory(this, categoryName)
        Logger.e("saveCategory: $categoryName")

        var categoryCode = code.toString().substring(1, code.length) // ,제거
        AppPreferences.setHomeVideoCategoryCode(this, categoryCode)
        Logger.e("saveCategory: $categoryCode")
    }

    private fun saveAge() {
        var name = StringBuilder()
        var code = StringBuilder()

        selectedAge.forEach {
            if (AGE_ALL == it) {
                Logger.e("saveAge [전체 선택]")
                AppPreferences.setCategoryAge(this, AGE_ALL)
                //AppPreferences.setHomeVideoCategoryCode(this, "")
                return
            }

            name.append(",")
            name.append(it)
            //code.append(",")
            //code.append(getCategoryCode(it))
        }

        if (name.isEmpty()) {
            AppPreferences.setCategoryAge(this, AGE_ALL)
            //AppPreferences.setHomeVideoCategoryCode(this, "")
            return
        }

        var ageName = name.toString().substring(1, name.length) // ,제거
        AppPreferences.setCategoryAge(this, ageName)
        Logger.e("saveAge: $ageName")

//        var categoryCode = code.toString().substring(1, code.length)
//        AppPreferences.setHomeVideoCategoryCode(this, categoryCode)
//        Logger.e("saveCategory: $categoryCode")
    }

    private fun saveGender() {
        var name = StringBuilder()
        var code = StringBuilder()

        selectedGender.forEach {
            if (GENDER_ALL == it) {
                Logger.e("saveGender [전체 선택]")
                AppPreferences.setCategoryGender(this, GENDER_ALL)
                //AppPreferences.setHomeVideoCategoryCode(this, "")
                return
            }

            name.append(",")
            name.append(it)
            //code.append(",")
            //code.append(getCategoryCode(it))
        }

        if (name.isEmpty()) {
            AppPreferences.setCategoryGender(this, GENDER_ALL)
            //AppPreferences.setHomeVideoCategoryCode(this, "")
            return
        }

        var genderName = name.toString().substring(1, name.length) // ,제거
        AppPreferences.setCategoryGender(this, genderName)
        Logger.e("saveGender: $genderName")

//        var categoryCode = code.toString().substring(1, code.length)
//        AppPreferences.setHomeVideoCategoryCode(this, categoryCode)
//        Logger.e("saveCategory: $categoryCode")
    }

    private fun getCategoryCode(code: String): String {
        val str = DBManager.getInstance(this).get(NetworkDBKey.getAPI81Key(this, "all"))
        if (!TextUtils.isEmpty(str)) {
            val response = Gson().fromJson(str, API81::class.java)
            response.data.forEach {
                if (it.categoryName.startsWith(code)) {
                    return it.categoryId
                }
            }
        }

        return ""
    }

    private fun resetCategory() {
        AppPreferences.setHomeVideoCategory(this, "전체")
        AppPreferences.setCategoryAge(this, "all")
        AppPreferences.setCategoryGender(this, "all")

        initCategory()
        initAge()
        initGender()
    }

    /**
     * 매개변수 카테고리명의 리스트상 인덱스를 반환
     * @param category
     * @return
     */
    private fun getCategoryIndex(category: String): Int {
        for (i in 0 until categoryTexts.size) {
            if (category == categoryTexts[i]) {
                return i + 1
            }
        }

        return -1
    }

    private fun handleCategoryButton(button: CompoundButton, isChecked: Boolean) {
        if (isChecked) {
            selectedCategory.add(button.text.toString())
        } else {
            selectedCategory.remove(button.text.toString())
        }
    }

    private fun toggleTotalButton(isChecked: Boolean) {
        checkboxCategory1.setOnCheckedChangeListener(null)

        checkboxCategory1.isChecked = isChecked

        if (isChecked) {
            selectedCategory.add(checkboxCategory1.text.toString())
        } else {
            selectedCategory.remove(checkboxCategory1.text.toString())
        }

        checkboxCategory1.setOnCheckedChangeListener(checkedChangeListener)
    }

    private fun toggleTotalAgeButton(isChecked: Boolean) {
        buttonAgeAll.setOnCheckedChangeListener(null)
        buttonAgeAll.isChecked = isChecked
        if (isChecked) {
            selectedAge.add(buttonAgeAll.tag.toString())
        } else {
            selectedAge.remove(buttonAgeAll.tag.toString())
        }
        buttonAgeAll.setOnCheckedChangeListener(ageCheckedListener)
    }

    private fun toggleTotalGenderButton(isChecked: Boolean) {
        buttonAllGender.setOnCheckedChangeListener(null)
        buttonAllGender.isChecked = isChecked
        if (isChecked) {
            selectedGender.add(buttonAllGender.tag.toString())
        } else {
            selectedGender.remove(buttonAllGender.tag.toString())
        }
        buttonAllGender.setOnCheckedChangeListener(genderCheckedListener)
    }

    private val checkedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        Logger.d("buttonView.text: ${buttonView.text}")
        handleCategoryButton(buttonView, isChecked)

        if ("전체" == buttonView.text) {
            for (i in 2 until categoryTexts.size + 1) {
                var id = resources.getIdentifier("checkboxCategory$i", "id", "com.enliple.pudding")
                var checkBox = findViewById<CheckBox>(id)
                if (checkBox != null) {
                    checkBox.isChecked = isChecked
                }
            }
        } else {
            if (isChecked) {
                if (selectedCategory.size == categoryTexts.size - 1) {
                    toggleTotalButton(true)
                }
            } else {
                toggleTotalButton(isChecked)
            }
        }
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonNext -> {
                saveCategory()
                saveGender()
                saveAge()

                AppBroadcastSender.notifyRefresh(this, AppConstants.BROADCAST_EXTRA_VALUE_REFRESH_CATEGORY)

                finish()
            }

            R.id.buttonReset -> resetCategory()
            R.id.close -> finish()
        }
    }

    private val ageCheckedListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (R.id.buttonAgeAll == buttonView.id) {
            if (isChecked) {
                buttonAge10.isChecked = true
                buttonAge20.isChecked = true
                buttonAge30.isChecked = true
            } else {
                buttonAge10.isChecked = false
                buttonAge20.isChecked = false
                buttonAge30.isChecked = false
            }
        } else {
            if (isChecked) {
                selectedAge.add(buttonView.tag.toString())

                if (selectedAge.size == 3) {
                    toggleTotalAgeButton(true)
                }
            } else {
                selectedAge.remove(buttonView.tag.toString())

                toggleTotalAgeButton(false)
            }
        }
    }

    private val genderCheckedListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (R.id.buttonAllGender == buttonView.id) {
            if (isChecked) {
                buttonMale.isChecked = true
                buttonFemale.isChecked = true
            } else {
                buttonMale.isChecked = false
                buttonFemale.isChecked = false
            }
        } else {
            if (isChecked) {
                selectedGender.add(buttonView.tag.toString())

                if (selectedGender.size == 2) {
                    toggleTotalGenderButton(true)
                }
            } else {
                selectedGender.remove(buttonView.tag.toString())

                toggleTotalGenderButton(false)
            }
        }
    }
}