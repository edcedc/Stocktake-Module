package com.yyc.stocktake.weight;


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.blankj.utilcode.util.ScreenUtils;
import com.yyc.stocktake.R;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.SimpleCallback;

/**
 * 作者：yc on 2018/8/23.
 * 邮箱：501807647@qq.com
 * 版本：v1.0
 */

public class PopupWindowTool {

    /**
     *  可已复用的弹窗
     * @param act
     */
    public static XPopup.Builder showDialog(Context act){
        return new XPopup.Builder(act)
                .setPopupCallback(new SimpleCallback(){

                });
    }

    /**
     *  列表弹窗
     */
    public static XPopup.Builder showListDialog(Context act) {
        return new XPopup.Builder(act)
                .isDarkTheme(true)
                .hasShadowBg(false)
//                .customHostLifecycle(act)
                .moveUpToKeyboard(false);
//                .borderRadius(XPopupUtils.dp2px(act, 15));
//                        .popupHeight(XPopupUtils.dp2px(getContext(), 397f))
//                        .isViewMode(true)
//                            .hasBlurBg(true)
//                            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
    }


}
