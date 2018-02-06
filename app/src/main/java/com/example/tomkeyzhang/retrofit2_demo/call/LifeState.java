package com.example.tomkeyzhang.retrofit2_demo.call;

import android.arch.lifecycle.Lifecycle;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tomkeyzhang on 11/1/18.
 * 用于保持组件生命状态
 */

public class LifeState {
    Lifecycle lifecycle;
    AtomicBoolean cancelled = new AtomicBoolean(false);

    public LifeState(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public Lifecycle.State getCurrentState() {
        return lifecycle.getCurrentState();
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void cancel() {
        cancelled.set(true);
    }
}