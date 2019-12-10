package com.ichong.commonmodule.downloadmodule;

import java.util.List;

/**
 * @Description 下载回调接口
 * @author zzy
 * @date 2014年6月3日 下午6:26:17
 * @version V1.0.0
 */

public interface DownloadListener {
	void onUpdateProgress(DownloadTaskEntity task);

	void onWait(DownloadTaskEntity task);

	void onSuccess(DownloadTaskEntity task);

	void onError(DownloadTaskEntity task, String error);

	void onStart(DownloadTaskEntity task);

	void onCancel(DownloadTaskEntity task);

	void onTaskList(List<DownloadTaskEntity> tasks);

}
