package edu.nyu.welcomehome.models.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableLoginRequest.class)
@JsonDeserialize(as = ImmutableLoginRequest.class)
public interface LoginRequest {
    String username();

    String password();
}
