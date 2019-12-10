package com.ichong.commonmodule.downloadmodule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

/**
 * @Description 类说明
 * @author zzy
 * @date 2014年6月3日 下午6:26:17
 * @version V1.0.0
 */

public class DownloadReceiver extends BroadcastReceiver {

	private final DownloadListener mDownloadListener;

	public DownloadReceiver(DownloadListener downloadListener) {
		this.mDownloadListener = downloadListener;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Download.RECEIVER_ACTION)) {
			int type = intent.getIntExtra(Download.TYPE, -1);

			if (this.mDownloadListener == null) {
				return;
			}

			DownloadTaskEntity taskEntity = new DownloadTaskEntity();
			try {
				taskEntity = (DownloadTaskEntity) intent.getSerializableExtra(Download.RECEIVER_KEY_TASK_ENTITY);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			switch (type) {
			case Download.START:
				this.mDownloadListener.onStart(taskEntity);
				break;

			case Download.SUCCESS:
				this.mDownloadListener.onSuccess(taskEntity);
				break;

			case Download.ERROR:
				this.mDownloadListener.onError(taskEntity, intent.getStringExtra(Download.RECEIVER_KEY_ERROR));
				break;

			case Download.WAIT:
				this.mDownloadListener.onWait(taskEntity);
				break;

			case Download.PROGRESS:

				this.mDownloadListener.onUpdateProgress(taskEntity);
				break;
			case Download.TASK_LIST:

				ArrayList<DownloadTaskEntity> taskEntitys = (ArrayList<DownloadTaskEntity>) intent.getSerializableExtra(Download.RECEIVER_KEY_TASK_ENTITYS);

				this.mDownloadListener.onTaskList(taskEntitys);
				break;
			case Download.CANCEL:
				this.mDownloadListener.onCancel(taskEntity);
				break;

			default:
				break;
			}

		}
	}
}
