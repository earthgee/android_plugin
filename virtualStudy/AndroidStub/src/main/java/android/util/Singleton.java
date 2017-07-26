package android.util;

/**
 * Created by zhaoruixuan on 2017/7/24.
 */
public abstract class Singleton<T> {

    public Singleton() {
        throw new RuntimeException("Stub!");
    }

    protected abstract T create();

    public T get() {
        throw new RuntimeException("Stub!");
    }

}
