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

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import java.io.File;

/**
 * @author cenxiaozhong
 * @date 2019/2/9
 * @since 1.0.0
 */
public class ResourceRequest<T extends DownloadTask> {
    private DownloadTask mDownloadTask;

    static ResourceRequest with(Context context) {
        ResourceRequest resourceRequest = new ResourceRequest();
        resourceRequest.mDownloadTask = Runtime.getInstance().getDefaultDownloadTask();
        resourceRequest.mDownloadTask.setContext(context);
        return resourceRequest;
    }

    public ResourceRequest url(@NonNull String url) {
        mDownloadTask.setUrl(url);
        return this;
    }

    public ResourceRequest target(@Nullable File target) {
        mDownloadTask.setFile(target);
        return this;
    }

    public ResourceRequest setUniquePath(boolean uniquePath) {
        mDownloadTask.setUniquePath(uniquePath);
        return this;
    }

    public ResourceRequest target(@NonNull File target, @NonNull String authority) {
        mDownloadTask.setFile(target, authority);
        return this;
    }

    protected ResourceRequest setContentLength(long contentLength) {
        mDownloadTask.mContentLength = contentLength;
        return this;
    }


    public ResourceRequest setDownloadTimeOut(long downloadTimeOut) {
        mDownloadTask.downloadTimeOut = downloadTimeOut;
        return this;
    }

    public ResourceRequest setConnectTimeOut(int connectTimeOut) {
        mDownloadTask.connectTimeOut = connectTimeOut;
        return this;
    }

    public ResourceRequest setOpenBreakPointDownload(boolean openBreakPointDownload) {
        mDownloadTask.mIsBreakPointDownload = openBreakPointDownload;
        return this;
    }

    public ResourceRequest setForceDownload(boolean force) {
        mDownloadTask.mIsForceDownload = force;
        return this;
    }

    public ResourceRequest setEnableIndicator(boolean enableIndicator) {
        mDownloadTask.mEnableIndicator = enableIndicator;
        return this;
    }


    public ResourceRequest setIcon(@DrawableRes int icon) {
        mDownloadTask.mDownloadIcon = icon;
        return this;
    }

    public ResourceRequest setParallelDownload(boolean parallelDownload) {
        mDownloadTask.mIsParallelDownload = parallelDownload;
        return this;
    }

    public ResourceRequest addHeader(String key, String value) {
        if (mDownloadTask.mHeaders == null) {
            mDownloadTask.mHeaders = new ArrayMap<>();
        }
        mDownloadTask.mHeaders.put(key, value);
        return this;
    }

    public ResourceRequest setAutoOpen(boolean autoOpen) {
        mDownloadTask.mAutoOpen = autoOpen;
        return this;
    }

    public File get() {
        return DownloadImpl.getInstance().call(mDownloadTask);
    }

    public ResourceRequest setDownloadListener(DownloadListener downloadListener) {
        mDownloadTask.setDownloadListener(downloadListener);
        return this;
    }

    public ResourceRequest setDownloadingListener(DownloadingListener downloadListener) {
        mDownloadTask.setDownloadingListener(downloadListener);
        return this;
    }

    public ResourceRequest
    setDownloadListenerAdapter(DownloadListenerAdapter downloadListenerAdapter) {
        mDownloadTask.setDownloadListenerAdapter(downloadListenerAdapter);
        return this;
    }

    public DownloadTask getDownloadTask() {
        return this.mDownloadTask;
    }

    public void enqueue() {
        DownloadImpl.getInstance().enqueue(mDownloadTask);
    }

    public void enqueue(DownloadListener downloadListener) {
        mDownloadTask.setDownloadListener(downloadListener);
        DownloadImpl.getInstance().enqueue(mDownloadTask);
    }

    public void enqueue(DownloadingListener downloadingListener) {
        mDownloadTask.setDownloadingListener(downloadingListener);
        DownloadImpl.getInstance().enqueue(mDownloadTask);
    }

    public void enqueue(DownloadListenerAdapter downloadListenerAdapter) {
        setDownloadListenerAdapter(downloadListenerAdapter);
        DownloadImpl.getInstance().enqueue(mDownloadTask);
    }

}
