/*
 * Copyright (C)  Justson(https://github.com/Justson/Downloader)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.download.library;

import android.content.Context;
import android.net.Uri;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author cenxiaozhong
 * @date 2017/5/13
 */
public class DownloadTask extends Extra implements Serializable, Cloneable {

    static final String TAG = Runtime.PREFIX + DownloadTask.class.getSimpleName();
    int mId = Runtime.getInstance().generateGlobalId();
    long mTotalsLength;
    Context mContext;
    File mFile;
    DownloadListener mDownloadListener;
    DownloadingListener mDownloadingListener;
    String authority = "";
    public static final int STATUS_NEW = 1000;
    public static final int STATUS_PENDDING = 1001;
    public static final int STATUS_DOWNLOADING = 1002;
    public static final int STATUS_PAUSED = 1003;
    public static final int STATUS_COMPLETED = 1004;
    long beginTime = 0L;
    long pauseTime = 0L;
    long endTime = 0L;
    long detalTime = 0L;
    boolean isCustomFile = false;
    boolean uniquePath = true;
    int connectTimes = 0;

    @IntDef({STATUS_NEW, STATUS_PENDDING, STATUS_DOWNLOADING, STATUS_PAUSED, STATUS_COMPLETED})
    @interface DownloadTaskStatus {
    }

    private AtomicInteger status = new AtomicInteger(STATUS_NEW);

    public DownloadTask() {
        super();
    }

    public int getStatus() {
        return status.get();
    }

    void setStatus(@DownloadTaskStatus int status) {
        this.status.set(status);
    }

    void resetTime() {
        beginTime = 0L;
        pauseTime = 0L;
        endTime = 0L;
        detalTime = 0L;
    }

    public int getId() {
        return this.mId;
    }

    public Context getContext() {
        return mContext;
    }

    public DownloadTask setContext(Context context) {
        mContext = context.getApplicationContext();
        return this;
    }

    public DownloadTask setEnableIndicator(boolean enableIndicator) {
        if (enableIndicator && mFile != null && TextUtils.isEmpty(authority)) {
            Runtime.getInstance().logError(TAG, " Custom file path, you must specify authority, otherwise the notification should not be turned on");
            this.mEnableIndicator = false;
        } else {
            this.mEnableIndicator = enableIndicator;
        }
        return this;
    }

    public File getFile() {
        return mFile;
    }

    public Uri getFileUri() {
        return Uri.fromFile(this.mFile);
    }

    String getAuthority() {
        return authority;
    }

    public DownloadTask setFile(@NonNull File file) {
        mFile = file;
        this.authority = "";
        checkCustomFilePath(file);
        return this;
    }

    private void checkCustomFilePath(File file) {
        if (file == null || file.getAbsolutePath().startsWith(Runtime.getInstance().getDefaultDir(this.getContext()).getAbsolutePath())) {
            isCustomFile = false;
        } else if (!TextUtils.isEmpty(this.authority)) {
            setEnableIndicator(true);
            isCustomFile = true;
        } else {
            setEnableIndicator(false);
            isCustomFile = true;
        }
    }

    boolean isCustomFile() {
        return isCustomFile;
    }

    public DownloadTask setFile(@NonNull File file, @NonNull String authority) {
        this.mFile = file;
        this.authority = authority;
        checkCustomFilePath(file);
        return this;
    }

    void updateTime(long beginTime) {
        if (this.beginTime == 0L) {
            this.beginTime = beginTime;
            return;
        }
        if (this.beginTime != beginTime) {
            detalTime += Math.abs(beginTime - this.pauseTime);
        }
    }

    public long getUsedTime() {
        if (status.get() == STATUS_DOWNLOADING) {
            return beginTime > 0L ? SystemClock.elapsedRealtime() - beginTime - detalTime : 0L;
        } else if (status.get() == STATUS_COMPLETED) {
            return endTime - beginTime - detalTime;
        } else {
            return 0L;
        }
    }

    public long getBeginTime() {
        return beginTime;
    }

    protected void pause() {
        pauseTime = SystemClock.elapsedRealtime();
    }

    protected void completed() {
        endTime = SystemClock.elapsedRealtime();
    }

    protected void destroy() {
        this.mId = -1;
        this.mUrl = null;
        this.mContext = null;
        this.mFile = null;
        this.mIsParallelDownload = false;
        mIsForceDownload = false;
        mEnableIndicator = true;
        mDownloadIcon = android.R.drawable.stat_sys_download;
        mDownloadDoneIcon = android.R.drawable.stat_sys_download_done;
        mIsParallelDownload = true;
        mIsBreakPointDownload = true;
        mUserAgent = "";
        mContentDisposition = "";
        mMimetype = "";
        mContentLength = -1L;
        if (mHeaders != null) {
            mHeaders.clear();
            mHeaders = null;
        }
        status.set(STATUS_NEW);
    }

    DownloadingListener getDownloadingListener() {
        return mDownloadingListener;
    }

    public DownloadTask
    setDownloadingListener(DownloadingListener downloadingListener) {
        mDownloadingListener = downloadingListener;
        return this;
    }

    public DownloadListener getDownloadListener() {
        return mDownloadListener;
    }


    public DownloadTask setDownloadListener(DownloadListener downloadListener) {
        mDownloadListener = downloadListener;
        return this;
    }

    public DownloadTask
    setDownloadListenerAdapter(DownloadListenerAdapter downloadListenerAdapter) {
        setDownloadListener(downloadListenerAdapter);
        setDownloadingListener(downloadListenerAdapter);
        return this;
    }

    void setTotalsLength(long totalsLength) {
        mTotalsLength = totalsLength;
    }

    public long getTotalsLength() {
        return mTotalsLength;
    }


    public DownloadTask setBreakPointDownload(boolean breakPointDownload) {
        mIsBreakPointDownload = breakPointDownload;
        return this;
    }

    public DownloadTask setForceDownload(boolean force) {
        mIsForceDownload = force;
        return this;
    }

    public DownloadTask setIcon(@DrawableRes int icon) {
        this.mDownloadIcon = icon;
        return this;
    }

    public DownloadTask setParallelDownload(boolean parallelDownload) {
        mIsParallelDownload = parallelDownload;
        return this;
    }

    public DownloadTask addHeader(String key, String value) {
        if (this.mHeaders == null) {
            this.mHeaders = new ArrayMap<>();
        }
        this.mHeaders.put(key, value);
        return this;
    }

    public DownloadTask setAutoOpen(boolean autoOpen) {
        mAutoOpen = autoOpen;
        return this;
    }

    public DownloadTask setDownloadTimeOut(long downloadTimeOut) {
        this.downloadTimeOut = downloadTimeOut;
        return this;
    }

    public DownloadTask setConnectTimeOut(int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
        return this;
    }

    public DownloadTask setBlockMaxTime(int blockMaxTime) {
        this.blockMaxTime = blockMaxTime;
        return this;
    }

    public DownloadTask setUserAgent(String userAgent) {
        this.mUserAgent = userAgent;
        return this;
    }

    public DownloadTask setContentLength(long contentLength) {
        this.mContentLength = contentLength;
        return this;
    }

    public DownloadTask setMimetype(String mimetype) {
        this.mMimetype = mimetype;
        return this;
    }

    public DownloadTask setContentDisposition(String contentDisposition) {
        this.mContentDisposition = contentDisposition;
        return this;
    }

    public DownloadTask setUrl(String url) {
        this.mUrl = url;
        return this;
    }

    public DownloadTask setDownloadDoneIcon(@DrawableRes int icon) {
        this.mDownloadDoneIcon = icon;
        return this;
    }

    @Override
    public DownloadTask clone() {
        try {
            DownloadTask downloadTask = (DownloadTask) super.clone();
            downloadTask.mId = Runtime.getInstance().generateGlobalId();
            return downloadTask;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return new DownloadTask();
        }
    }

    public boolean isUniquePath() {
        return uniquePath;
    }

    public void setUniquePath(boolean uniquePath) {
        this.uniquePath = uniquePath;
    }

    public void setLength(long length) {
        mTotalsLength = length;
    }
}
