package edu.nyu.welcomehome.models;

import org.immutables.value.Value;

@Value.Immutable
public interface Role {
    Long roleID();

    String roleDescription();
}
