package org.referix.database;

public interface TableCreateCallback {
    void onSuccess();
    void onError(Exception e);
}
