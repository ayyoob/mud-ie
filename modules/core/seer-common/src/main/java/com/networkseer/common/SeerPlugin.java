package com.networkseer.common;

import com.networkseer.common.config.SeerConfiguration;

import java.util.List;

public interface SeerPlugin {

    void activate(SeerConfiguration seerConfiguration);

    void deactivate();

    List<String> getModuleDependencies();
}
