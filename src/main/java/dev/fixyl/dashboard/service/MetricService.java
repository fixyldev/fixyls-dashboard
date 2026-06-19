package dev.fixyl.dashboard.service;

public interface MetricService<S, U> {

    S getStatic();

    U getUpdate();

}
