package com.hqumath.tcp.ui.main;

import com.hqumath.tcp.app.AppExecutors;
import com.hqumath.tcp.utils.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * ****************************************************************
 * 作    者: Created by gyd
 * 创建时间: 2024/12/9 17:07
 * 文件描述:
 * 注意事项:
 * ****************************************************************
 */
public class TCPServer {
    private final String TAG = "TCPServer";
    public static final int ServerPort = 10200;
    private final int REC_LENGTH = 2048;//每次接收数据长度2k

    private ServerSocket serverSocket;//服务端
    private Socket clientSocket;//当前客户端
    private OutputStream out;//当前客户端输出流
    private InputStream in;//当前客户端输入流
    private ScheduledFuture scheduledFuture;//定时任务
    private OnStatusChangeListener onStatusChangeListener;

    private boolean timerWorking;//定时器工作状态
    private boolean isReceive = false;//接收线程状态
    private final int TimerPeriod = 500;//定时器周期ms
    private long timerLength;//定时器工作时间ms

    //单例模式-静态内部类
    private static class TCPServerHolder {
        private static final TCPServer instance = new TCPServer();
    }

    public static TCPServer getInstance() {
        return TCPServer.TCPServerHolder.instance;
    }

    //连接状态变化
    public interface OnStatusChangeListener {
        void onStatusChange(boolean isConnected);

        void onEvent(String msg);
    }

    public void setOnStatusChangeListener(OnStatusChangeListener listener) {
        this.onStatusChangeListener = listener;
    }

    public void start() {
        //定时任务，检查服务端运行状态、设置方向盘力效应
        timerWorking = true;
        if (scheduledFuture == null) {
            scheduledFuture = AppExecutors.getInstance().scheduledWork().scheduleWithFixedDelay(() -> {
                if (timerWorking) {
                    if (timerLength % 3000 == 0) {//每3秒
                        checkServerSocket();//检查服务端运行状态
                    }
                    timerLength += TimerPeriod;//计时更新
                }
            }, 0, TimerPeriod, TimeUnit.MILLISECONDS);
        }
    }

    public void stop() {
        //取消定时任务
        timerWorking = false;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        //断开连接
        isReceive = false;
        onConnectError();
        try {
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送数据
     *
     * @param msg
     */
    public void sendData(String msg) {
        if (out == null) {
            LogUtil.d(TAG, "客户端未连接");
            return;
        }
        try {
            byte[] data = msg.getBytes(Charset.forName("GBK"));
            out.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查服务端运行状态
     */
    private void checkServerSocket() {
        try {
            //开启服务、指定端口号
            if (serverSocket == null) {
                serverSocket = new ServerSocket(ServerPort);
                //serverSocket.setPerformancePreferences(1,0,0);//性能首选项 短连接时间、低延迟和高带宽
                //serverSocket.setReceiveBufferSize(cache.length);//内部套接字接收缓冲区的大小，又用于设置通告给远程对等方的 TCP 接收窗口的大小
                //serverSocket.setReuseAddress(true);//关闭Socket时等待一会
                //serverSocket.setSoTimeout(5*1000);//超时默认为0，无限等待
            }
            //开始接收数据
            startReceive();
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "开启服务异常");
            onConnectError();
        }
    }

    //接收客户端数据
    private void startReceive() {
        if (isReceive)
            return;
        isReceive = true;
        AppExecutors.getInstance().workThread().execute(() -> {
            try {
                //等待客户端的连接，Accept会阻塞，直到建立连接，
                clientSocket = serverSocket.accept();
                //获取输入流
                in = clientSocket.getInputStream();
                //获取输出流
                out = clientSocket.getOutputStream();
                LogUtil.d(TAG, "客户端已连接");
                if (onStatusChangeListener != null) {
                    onStatusChangeListener.onStatusChange(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                isReceive = false;
                onConnectError();
                return;
            }
            byte[] container = new byte[REC_LENGTH];
            while (isReceive) {
                try {
                    int len = in.read(container);
                    if (len > 0) {
                        //LogUtil.d("收到数据：" + ByteUtil.bytesToHexWithSpace(container));
                        dealData(container, len);
                    } else {
                        isReceive = false;//释放 读取线程
                        onConnectError();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    isReceive = false;//释放 读取线程
                    onConnectError();
                }
            }
            LogUtil.d(TAG, "Receive 线程结束");
        });
    }

    //TCP通讯异常时，断开连接
    private void onConnectError() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
                clientSocket = null;
            }
            if (in != null) {
                in.close();
                in = null;
            }
            if (out != null) {
                out.close();
                out = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtil.d(TAG, "TCP通讯断开");
        if (onStatusChangeListener != null) {
            onStatusChangeListener.onStatusChange(false);
        }
    }

    /**
     * 处理接收到的数据
     *
     * @param receiveData 收到的数据
     * @param length      数据长度
     */
    private void dealData(byte[] receiveData, int length) {
        String msg = new String(receiveData, 0, length, Charset.forName("GBK"));
        if (onStatusChangeListener != null) {//键位配置界面，更新UI
            onStatusChangeListener.onEvent(msg);
        }
    }
}
