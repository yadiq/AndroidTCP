package com.hqumath.tcp.ui.main;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;

import com.hqumath.tcp.app.AppExecutors;
import com.hqumath.tcp.base.BaseActivity;
import com.hqumath.tcp.databinding.ActivityMainBinding;
import com.hqumath.tcp.utils.CommonUtil;
import com.hqumath.tcp.utils.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ****************************************************************
 * 作    者: Created by gyd
 * 创建时间: 2023/10/25 9:35
 * 文件描述: 主界面
 * 注意事项:
 * ****************************************************************
 */
public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    private TCPServer tcpServer;

    @Override
    protected View initContentView(Bundle savedInstanceState) {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initListener() {
        binding.btnStartServer.setOnClickListener(v -> {
            startServer();
        });
        binding.btnCloseServer.setOnClickListener(v -> {
            closeServer();
        });
        binding.btnSend.setOnClickListener(v -> {
            AppExecutors.getInstance().workThread().execute(() -> {
                tcpServer.sendData(binding.edtSend.getText().toString());
            });
        });
        binding.tvReceive.setMovementMethod(new ScrollingMovementMethod());//可滚动

    }

    @Override
    protected void initData() {
        String ip = CommonUtil.getIPAddress();
        binding.tvIp.setText("本地主机地址：" + ip);
        binding.tvPort.setText("本地主机端口：" + TCPServer.ServerPort);

        tcpServer = TCPServer.getInstance();
        tcpServer.setOnStatusChangeListener(new TCPServer.OnStatusChangeListener() {
            @Override
            public void onStatusChange(boolean isConnected) {
                printLog(isConnected ? "客户端已连接" : "客户端已断开");
            }

            @Override
            public void onEvent(String msg) {
                printLog(msg);
            }
        });
        //开启服务
        binding.btnStartServer.performClick();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tcpServer != null) {
            tcpServer.stop();
            tcpServer = null;
        }
        CommonUtil.killProgress();
    }


    /////////////////Server端////////////////

    //开启服务
    private void startServer() {
        if (tcpServer != null) {
            tcpServer.start();
        }
        binding.tvStatus.setText("服务状态：已开启");
        printLog("服务端已开启");
    }

    //关闭服务
    private void closeServer() {
        if (tcpServer != null) {
            tcpServer.stop();
        }
        binding.tvStatus.setText("服务状态：未开启");
        printLog("服务端已关闭");
    }

    private void printLog(String msg) {
        LogUtil.d(msg);
        binding.getRoot().post(() -> {
            //当前时间
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = dateFormat.format(new Date());
            binding.tvReceive.setText(date + "\n" + msg);
        });
    }
}
