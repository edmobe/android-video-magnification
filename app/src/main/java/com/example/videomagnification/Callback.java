package com.example.videomagnification;

public interface Callback<T> {
    void onComplete(Result<T> result);
}
