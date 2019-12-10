package com.ichong.commonmodule.downloadmodule;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author zzy
 * @version V1.0.0
 * @Description 类说明
 * @date 2014年6月3日 下午6:26:17
 */

public class DownloadService extends Service {
    private DownloadManage mDownloadManage;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        this.mDownloadManage = new DownloadManage(this.getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();

            if (Download.SERVICE_ACTION.equals(action)) {
                int type = intent.getIntExtra(Download.TYPE, -1);
                String url;

                switch (type) {
                    case Download.ADD:
                        url = intent.getStringExtra(Download.URL);
                        String savePath = intent.getStringExtra(Download.SAVE_PATH);
                        Object ext = intent.getSerializableExtra(Download.EXT);
                        this.mDownloadManage.addTask(url, savePath, ext);
                        break;
                    case Download.TASK_LIST:
                        this.mDownloadManage.getTaskList();
                        break;
                    case Download.START:
                        this.mDownloadManage.startManage();
                        break;
                    case Download.REMOVE:
                        url = intent.getStringExtra(Download.URL);
                        this.mDownloadManage.removeTask(url);
                        break;
                    default:
                        break;
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mDownloadManage.close();
    }

}
