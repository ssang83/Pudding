package com.enliple.pudding.commons.network

import android.content.Context
import com.enliple.pudding.commons.internal.AppPreferences

/**
 * Created by Kim Joonsung on 2018-11-30.
 */
object NetworkDBKey {

    fun getAPI27Key(context: Context): String {
        return "GET/search?user=${AppPreferences.getUserId(context)}"
    }

    fun getAPI28Key(context: Context, tagId: String, order: String): String {
        return "GET/search/tagid?t_idx=$tagId&order=$order&user=${AppPreferences.getUserId(context)}"
    }

    fun getAPI29Key(context: Context, keyword: String, order: String, pageCount:Int): String {
        // GET/search/vod?keyword=자동차&order=0&user=android4&category=&age=all&sex=all&page=1
        return "GET/search/vod?keyword=$keyword&order=$order&user=${AppPreferences.getUserId(context)}" +
                "&category=${AppPreferences.getHomeVideoCategoryCode(context)}&age=${AppPreferences.getCategoryAge(context)}" +
                "&sex=${AppPreferences.getCategoryGender(context)}&page=${pageCount}"
    }

    fun getAPI0Key(context: Context): String {
        return "GET/vod/upload/${AppPreferences.getUserId(context)}"
    }

    fun getAPI32Key(userId: String): String {
        return "GET/vod/upload/$userId/ALL"
    }

    fun getAPI23Key(context: Context): String {
        return "GET/user/${AppPreferences.getUserId(context)}/scrap"
    }

    fun getAPI37Key(context: Context): String {
        return "GET/user/${AppPreferences.getUserId(context)}/lately_vod"
    }

    fun getAPI34Key(context: Context, streamKey: String, order: String): String {
        return "GET/mui/hash?streamKey=$streamKey&order=$order&user=${AppPreferences.getUserId(context)}"
    }

    fun getAPI81Key(context: Context, category: String): String {
        return "GET/mui/category/$category"
    }

    fun getAPI49Key(link: String): String {
        //return "GET/share/getData?url=${Uri.encode(link, "UTF-8").toString()}"
        return "GET/share/getData?url=$link"
    }

    fun getAPI51Key(context: Context, gf_type: String): String {
        return "GET/user/${AppPreferences.getUserId(context)}/cookie_history?gf_type=$gf_type"
    }

    fun getAPI52Key(context: Context): String {
        return "GET/user/${AppPreferences.getUserId(context)}/cookie"
    }

    fun getAPI116Key(eventId:String): String {
        return "GET/event/$eventId/vod"
    }

    fun getVOD0Key(context: Context, keyword:String, pageCount:Int): String {
        // GET/mui/home/follow?page=1&category=&age=all&sex=all&search=&user=android4
        return "GET/mui/home/follow?page=${pageCount}&category=${AppPreferences.getHomeVideoCategoryCode(context)}&age=${AppPreferences.getCategoryAge(context)}" +
                "&sex=${AppPreferences.getCategoryGender(context)}&search=${keyword}&user=${AppPreferences.getUserId(context)}"
    }

    fun getAPI24Key(context:Context): String {
        return "DELETE/user/${AppPreferences.getUserId(context)!!}/scrap"
    }
}