package com.ichong.commonmodule.downloadmodule;

/**
 * @Description 类说明
 * @author zzy
 * @date 2014年6月3日 下午6:26:17
 * @version V1.0.0
 */

public interface DownloadStatus {
	enum TaskStatus {
		Error, Success, Wait, Running, Cancel, Start
	}

}
