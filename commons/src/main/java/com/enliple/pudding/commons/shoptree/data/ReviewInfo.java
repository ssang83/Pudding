package com.enliple.pudding.commons.shoptree.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Kim Joonsung on 2018-12-11.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewInfo {
    @JsonProperty(value = "is_id")
    public String is_id;

    @JsonProperty(value = "is_type")
    public String is_type;

    @JsonProperty(value = "is_score")
    public String is_score;

    @JsonProperty(value = "is_subject")
    public String is_subject;

    @JsonProperty(value = "is_content")
    public String is_content;

    @JsonProperty(value = "is_photo")
    public List<String> is_photo;
}
