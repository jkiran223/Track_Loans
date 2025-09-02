package com.trackloan

import com.trackloan.common.Result
import com.trackloan.common.UiState
import org.junit.Assert.assertEquals
import org.junit.Test

class ResultUiStateTest {

    @Test
    fun `Result Success should contain data`() {
        val result = Result.Success("test data")
        assert(result is Result.Success)
        assertEquals("test data", (result as Result.Success).data)
    }

    @Test
    fun `Result Error should contain error`() {
        val result = Result.Error(Exception("test error"))
        assert(result is Result.Error)
        assertEquals("test error", (result as Result.Error).exception.message)
    }

    @Test
    fun `Result Loading should be loading state`() {
        val result = Result.Loading
        assert(result is Result.Loading)
    }

    @Test
    fun `UiState Success should contain data`() {
        val uiState = UiState.Success("test data")
        assert(uiState is UiState.Success)
        assertEquals("test data", (uiState as UiState.Success).data)
    }

    @Test
    fun `UiState Error should contain message`() {
        val uiState = UiState.Error("test error message")
        assert(uiState is UiState.Error)
        assertEquals("test error message", (uiState as UiState.Error).message)
    }

    @Test
    fun `UiState Loading should be loading state`() {
        val uiState = UiState.Loading
        assert(uiState is UiState.Loading)
    }
}
