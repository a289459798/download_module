package com.ichong.commonmodule.downloadmodule;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;

/**
 * @Description 下载相关工具类
 * @author zzy
 * @date 2014年6月3日 下午6:26:17
 * @version V1.0.0
 */

public class DownloadUtils {
	private static final String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
	public static final String FILE_ROOT = SDCARD_ROOT + "testDM/";

	private static final long LOW_STORAGE_THRESHOLD = 1024 * 1024 * 10;

	public static boolean isSdCardWrittenable() {

		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	public static long getAvailableStorage() {

		String storageDirectory = null;
		storageDirectory = Environment.getExternalStorageDirectory().toString();

		try {
			StatFs stat = new StatFs(storageDirectory);
			long avaliableSize = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
			return avaliableSize;
		} catch (RuntimeException ex) {
			return 0;
		}
	}

	public static boolean checkAvailableStorage() {

		return getAvailableStorage() >= LOW_STORAGE_THRESHOLD;

	}

	public static boolean isSDCardPresent() {

		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED || info[i].getState() == NetworkInfo.State.CONNECTING) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
