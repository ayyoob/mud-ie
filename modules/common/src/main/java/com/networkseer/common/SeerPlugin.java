package com.networkseer.common;

import java.util.List;

public interface SeerPlugin {

    void activate();

    void deactivate();

    List<String> getModuleDependencies();
}
