package qwirkle.control;

import com.google.common.eventbus.EventBus;

public interface Writer<T> {
    public T get();
    void set(T t);
    public EventBus getBus();
}
