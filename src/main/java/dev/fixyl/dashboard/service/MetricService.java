package dev.fixyl.dashboard.service;

import java.io.IOException;

public interface MetricService<S, U> {

    S getStatic() throws IOException;

    U getUpdate() throws IOException;

}
