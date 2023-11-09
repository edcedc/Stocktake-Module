package com.yyc.stocktake.ui.frg

import android.os.Bundle
import android.widget.CompoundButton
import com.blankj.utilcode.util.AppUtils
import com.yyc.stocktake.R
import com.yyc.stocktake.api.UIHelper
import com.yyc.stocktake.base.BaseFragment
import com.yyc.stocktake.databinding.FLoginBinding
import com.yyc.stocktake.ext.showMessage
import com.yyc.stocktake.util.CacheUtil
import com.yyc.stocktake.viewmodel.LoginModel
import me.hgj.jetpackmvvm.ext.nav

/**
 * @Author nike
 * @Date 2023/7/5 14:53
 * @Description
 */
class LoginFrg: BaseFragment<LoginModel, FLoginBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        mDatabind.viewmodel = mViewModel
        mDatabind.click = ProxyClick()

        val user = CacheUtil.getUser()
        if (user != null && user.LoginID != null){
            mViewModel.username.set(user.LoginID)
        }
        if (user != null && user.Password != null){
            mViewModel.password.set(user.Password)
        }
        mDatabind.tvVersion.text = "SP INFINITE TECHNOLOGY LTD" + "_V" + AppUtils.getAppVersionName()
    }

    override fun createObserver() {

    }

    inner class ProxyClick() {
        fun clear(){
            mViewModel.username.set("")
        }

        var onCheckedChangeListener =
            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                mViewModel.isShowPwd.set(isChecked)
            }
        fun login(){
            when {
                mViewModel.username.get().isEmpty() -> showMessage(getString(R.string.error_phone))
                mViewModel.password.get().isEmpty() -> showMessage(getString(R.string.error_phone))
                else -> mViewModel.login(
                    mViewModel.username.get(),
                    mViewModel.password.get()
                )
            }
        }

        fun toSet(){
            UIHelper.startSettingFrg(nav())
        }

    }

}
