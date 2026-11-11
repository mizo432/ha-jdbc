package com.example.sample.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseUnitTest {

    @Test
    void ok_WithData_ShouldCreateSuccessResponse() {
        ApiResponse<String> response = ApiResponse.ok("テストデータ");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Success");
        assertThat(response.getData()).isEqualTo("テストデータ");
    }

    @Test
    void ok_WithDataAndMessage_ShouldCreateSuccessResponse() {
        ApiResponse<String> response = ApiResponse.ok("テストデータ", "カスタムメッセージ");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("カスタムメッセージ");
        assertThat(response.getData()).isEqualTo("テストデータ");
    }

    @Test
    void error_ShouldCreateErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error("エラーが発生しました");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("エラーが発生しました");
        assertThat(response.getData()).isNull();
    }

    @Test
    void constructor_ShouldSetAllFields() {
        ApiResponse<String> response = new ApiResponse<>(true, "メッセージ", "データ");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("メッセージ");
        assertThat(response.getData()).isEqualTo("データ");
    }

    @Test
    void defaultConstructor_ShouldInitializeFields() {
        ApiResponse<String> response = new ApiResponse<>();

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getData()).isNull();
    }

    @Test
    void setters_ShouldUpdateFields() {
        ApiResponse<String> response = new ApiResponse<>();

        response.setSuccess(true);
        response.setMessage("テストメッセージ");
        response.setData("テストデータ");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("テストメッセージ");
        assertThat(response.getData()).isEqualTo("テストデータ");
    }

    @Test
    void ok_WithListData_ShouldHandleCollection() {
        List<String> items = List.of("item1", "item2", "item3");
        ApiResponse<List<String>> response = ApiResponse.ok(items);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).hasSize(3);
        assertThat(response.getData()).containsExactly("item1", "item2", "item3");
    }

    @Test
    void ok_WithNullData_ShouldHandleNull() {
        ApiResponse<Void> response = ApiResponse.ok((Void) null);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Success");
        assertThat(response.getData()).isNull();
    }

    @Test
    void ok_WithEmptyList_ShouldHandleEmptyCollection() {
        ApiResponse<List<String>> response = ApiResponse.ok(List.of());

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEmpty();
    }

    @Test
    void getters_ShouldReturnCorrectValues() {
        ApiResponse<Integer> response = new ApiResponse<>(true, "テスト", 42);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("テスト");
        assertThat(response.getData()).isEqualTo(42);
    }
}
