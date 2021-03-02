package org.kymjs.chat;

import android.app.ProgressDialog;
import android.content.Context;

public class LoadingDialog {
    ProgressDialog mDefaultDialog;

    Context context;


    public LoadingDialog(Context context){
        this.context = context;
    }
    //显示的加载框
    public void showLoadingDialog(String message) {
        mDefaultDialog = new ProgressDialog(context);
        mDefaultDialog.setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER); //默认就是小圆圈的那种形式
        mDefaultDialog.setMessage(message);
        mDefaultDialog.setCancelable(true);//默认true
        mDefaultDialog.setCanceledOnTouchOutside(true);//默认true
        mDefaultDialog.show();
    }
    public void closeLoadingDialog(){
        this.mDefaultDialog.dismiss();
    }
}
