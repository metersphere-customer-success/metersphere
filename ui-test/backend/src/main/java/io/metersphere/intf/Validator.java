package io.metersphere.intf;

@FunctionalInterface
public interface Validator {
    boolean _validate(String param);
}
