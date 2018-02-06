package com.example.tomkeyzhang.retrofit2_demo;

/**
 * Created by tomkeyzhang on 5/2/18.
 */

public interface ProgressOperation {
    void showFailed();

    void showContent();

    void showProgress();

    ProgressOperation NONE = new ProgressOperation() {
        @Override
        public void showFailed() {

        }

        @Override
        public void showContent() {

        }

        @Override
        public void showProgress() {

        }
    };
}
