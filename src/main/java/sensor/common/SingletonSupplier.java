package sensor.common;

import java.util.function.Supplier;

public class SingletonSupplier<T> implements Supplier<T> {

    private final Supplier<T> supplier;
    private T instance;

    SingletonSupplier(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (instance == null) {
            instance = supplier.get();
        }
        return instance;
    }
}
