package com.example.tomkeyzhang.retrofit2_demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Build;
import android.support.annotation.NonNull;

import static android.R.style.Theme_Material_Light_Dialog_Alert;

/**
 * Created by tomkeyzhang on 6/2/18.
 */

public class WaitDialog implements ProgressOperation {
    private ProgressDialog progressDialog;

    public static WaitDialog create(Activity activity) {
        return new WaitDialog(activity);
    }

    public WaitDialog(Activity activity) {
        progressDialog = progressNoTitleDialog(activity, "请稍候");
    }

    public WaitDialog(Activity activity, String msg) {
        progressDialog = progressNoTitleDialog(activity, msg);
    }

    private ProgressDialog progressNoTitleDialog(@NonNull Activity activity, String msg) {
        int theme = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            theme = Theme_Material_Light_Dialog_Alert;
        }
        ProgressDialog progressDialog = new ProgressDialog(activity, theme);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }

    @Override
    public void showFailed() {
        try {
            progressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showContent() {
        try {
            progressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showProgress() {
        try {
            progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public void dismiss() {
        try {
            progressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
