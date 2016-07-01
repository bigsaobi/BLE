package com.qing.blemanager.command;

import android.content.Context;

import com.qing.blemanager.ble.BLEManager;


/**
 * Created by liuqing on 16/6/16.
 */
public abstract class DingBoxBaseCommand {
    private BLEManager bleManager;
    private Context ctx;
    private CommandListener commandListener;
    public DingBoxBaseCommand(Context ctx, CommandListener commandListener) {
        this.ctx = ctx.getApplicationContext();
        this.commandListener = commandListener;
        bleManager = BLEManager.getInstance(ctx);
    }

    public BLEManager getBleManager() {
        return bleManager;
    }

    public Context getContext(){
        return ctx;
    }

    public CommandListener getCommandListener(){
        return commandListener;
    }
    public abstract void execute();

    public interface CommandListener<T>{
        void onStart();
        void onFail(int code);
        void onSuccess(T data);
    }
}
