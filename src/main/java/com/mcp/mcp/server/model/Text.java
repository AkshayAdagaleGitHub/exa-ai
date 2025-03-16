package com.mcp.mcp.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Text {
    @JsonProperty("maxCharacters")
    public String maxCharacters;
}
