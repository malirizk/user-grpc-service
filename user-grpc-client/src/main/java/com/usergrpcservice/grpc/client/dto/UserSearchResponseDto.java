package com.usergrpcservice.grpc.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchResponseDto {

    @JsonProperty("total_pages")
    private Integer totalPages;
    @JsonProperty("page_number")
    private Integer pageNumber;
    @JsonProperty("total_result")
    private Integer totalResult;
    private List<UserResponseDto> content;
}
