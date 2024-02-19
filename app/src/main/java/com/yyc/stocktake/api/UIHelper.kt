package com.yyc.stocktake.api

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.blankj.utilcode.util.ActivityUtils
import com.google.gson.Gson
import com.yyc.stocktake.MainActivity
import com.yyc.stocktake.R
import com.yyc.stocktake.bean.DataBean
import com.yyc.stocktake.bean.db.AssetBean
import com.yyc.stocktake.ui.act.LoginAct
import com.yyc.stocktake.ui.act.ZxingAct
import me.hgj.jetpackmvvm.ext.navigateAction


/**
 * Created by Administrator on 2017/2/22.
 */

class UIHelper private constructor() {

    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }


    companion object {

        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)  // 设置进入动画
            .setExitAnim(R.anim.slide_out_left)  // 设置退出动画
            .setPopEnterAnim(R.anim.slide_in_left)  // 设置返回动画
            .setPopExitAnim(R.anim.slide_out_right)  // 设置返回退出动画
            .build()

        fun startMainAct() {
            ActivityUtils.startActivity(MainActivity::class.java)
        }

        /**
         *  二维码
         */
        fun startZxingAct(type: Int) {
            val bundle = Bundle()
            bundle.putInt("type", type)
            ActivityUtils.startActivity(bundle, ZxingAct::class.java)
        }

        /**
         *  登录
         */
        fun startLoginAct() {
            ActivityUtils.startActivity(LoginAct::class.java)
        }

        /**
         *  设置
         */
        fun startSettingFrg(nav: NavController) {
            val bundle = Bundle()
            nav.navigateAction(R.id.action_loginfragment_to_settingFrg, bundle)
        }

        /**
         *  下载
         */
        fun startDownloadFrg(nav: NavController) {
            val bundle = Bundle()
            nav.navigateAction(R.id.action_orderFrg_to_downloadFrg, bundle)
        }

        /**
         *  RFID 列表
         */
        fun startAssetFrg(nav: NavController, orderId: String) {
            val bundle = Bundle()
            bundle.putString("orderId", orderId)
            nav.navigateAction(R.id.action_orderFrg_to_assetFrg, bundle)
        }

        /**
         *  RFID 详情
         */
        fun startAssetDetailsFrg(nav: NavController, bean: AssetBean) {
            val bundle = Bundle()
            bundle.putString("bean", Gson().toJson(bean))
            nav.navigateAction(R.id.action_assetFrg_to_assetDetailsFrg, bundle)
        }

        /**
         *  上传
         */
        fun starUploadFrg(nav: NavController) {
            val bundle = Bundle()
            nav.navigateAction(R.id.action_orderFrg_to_uploadFrg, bundle)
        }

    }
}

