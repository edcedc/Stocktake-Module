package com.yyc.stocktake.ui.act

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.yyc.stocktake.R
import com.yyc.stocktake.databinding.ASplashBinding
import com.yyc.stocktake.weight.permission.RuntimeRationale
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission
import com.yyc.stocktake.api.ApiService
import com.yyc.stocktake.api.UIHelper
import com.yyc.stocktake.base.BaseActivity
import com.yyc.stocktake.util.CacheUtil
import me.hgj.jetpackmvvm.base.viewmodel.BaseViewModel

/**
 * Created by Android Studio.
 * User: ${edison}
 * Date: 2019/9/22
 * Time: 18:11 启动页
 */
class SplashAct : BaseActivity<BaseViewModel, ASplashBinding>(){

    var REQUEST_CODE_SETTING: Int = 1

    override fun initView(savedInstanceState: Bundle?) {
        setHasPermission()
    }

    /**
     * 设置权限
     */
    fun setHasPermission() {
        AndPermission.with(this)
            .runtime()
            .permission(
                Permission.READ_EXTERNAL_STORAGE,//写入外部存储, 允许程序写入外部存储，如SD卡
                Permission.WRITE_EXTERNAL_STORAGE,
                Permission.CAMERA
            )
            .rationale(RuntimeRationale())
            .onGranted { setPermissionOk() }
            .onDenied { permissions ->
                if (AndPermission.hasAlwaysDeniedPermission(this, permissions)) {
                    showSettingDialog(this, permissions)
                } else {
                    setPermissionCancel()
                }
            }
            .start()
    }

    /**
     * 权限都成功
     */
    fun setPermissionOk() {
        val user = CacheUtil.getUser()
        if (user?.LoginID != null && user?.Password != null){
            UIHelper.startMainAct()
        }else{
            UIHelper.startLoginAct()
        }
        finish()
    }

    /**
     * Display setting dialog.
     */
    fun showSettingDialog(context: Context, permissions: List<String>) {
        val permissionNames = Permission.transformText(context, permissions)
        val message =
            context.getString(
                R.string.message_permission_always_failed,
                TextUtils.join("\n", permissionNames)
            )

        AlertDialog.Builder(context)
            .setCancelable(false)
            .setTitle(R.string.app_name)
            .setMessage(message)
            .setPositiveButton(R.string.setting,
                { dialog, which -> setPermission() })
            .setNegativeButton(R.string.cancel,
                { dialog, which -> setPermissionCancel() })
            .show()
    }

    /**
     * Set permissions.
     */
    fun setPermission() {
        AndPermission.with(this)
            .runtime()
            .setting()
            .start(REQUEST_CODE_SETTING)
    }

    /**
     * 权限有任何一个失败都会走的方法
     */
    fun setPermissionCancel() {
//        act?.finish()
        setHasPermission()
//        showToast("请给需要的权限，以免出现异常")
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_SETTING -> {
//                showToast("The user comes back from the settings page.")
            }
        }
    }

}