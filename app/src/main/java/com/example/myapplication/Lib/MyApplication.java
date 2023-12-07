package com.example.myapplication.Lib;

import android.app.Application;

import java.io.Serializable;
import java.net.Socket;

public class MyApplication extends Application implements Serializable {
    private Socket sSocket;

    public Socket getSocket() {
        return sSocket;
    }

    public void setSocket(Socket socket) {
        this.sSocket = socket;
    }
}
