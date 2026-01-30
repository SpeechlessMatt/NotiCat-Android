package com.czy4201b.noticat.features.globalfilters

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.czy4201b.noticat.core.database.GlobalFilterDao
import com.czy4201b.noticat.core.database.entity.GlobalFilterEntity
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(FlowPreview::class)
class GlobalFiltersEditViewViewModel(
    private val globalFilterDao: GlobalFilterDao,
) : ViewModel() {
    private val _state = MutableStateFlow(GlobalFiltersEditViewUiState())
    val state: StateFlow<GlobalFiltersEditViewUiState> = _state.asStateFlow()

    var filterFields = mutableStateListOf<FilterFieldState>()
        private set

    private val _eventFlow = Channel<UiEvent>(capacity = Channel.BUFFERED)
    val eventFlow = _eventFlow.receiveAsFlow()

    init {
        initialize()
    }

    fun initialize() {
        viewModelScope.launch {
            delay(200)
            _state.update { it.copy(isLoading = true) }
            val filters = globalFilterDao.getAllStatic().map { filter ->
                FilterFieldState(
                    pattern = filter.pattern,
                    filterType = filter.type,
                    isIgnoreCase = filter.isIgnoreCase
                )
            }
            filterFields.addAll(filters)
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun addFilter() {
        filterFields.add(
            FilterFieldState(
                pattern = "",
                filterType = "keyword",
                isIgnoreCase = true,
                isUnfold = false
            )
        )
    }

    fun deleteFilter(id: String) {
        filterFields.removeAll { it.id == id }
    }

    fun updateFilterPattern(id: String, pattern: String) {
        val index = filterFields.indexOfFirst { it.id == id }
        if (index != -1) {
            filterFields[index] = filterFields[index].copy(pattern = pattern)
        }
    }

    fun setFilterType(id: String, filterType: String) {
        val index = filterFields.indexOfFirst { it.id == id }
        if (index != -1) {
            filterFields[index] = filterFields[index].copy(filterType = filterType)
        }
    }

    fun setIgnoreCase(id: String, isIgnoreCase: Boolean) {
        val index = filterFields.indexOfFirst { it.id == id }
        if (index != -1) {
            filterFields[index] = filterFields[index].copy(isIgnoreCase = isIgnoreCase)
        }
    }

    fun toggleFilter(id: String) {
        val index = filterFields.indexOfFirst { it.id == id }
        if (index != -1) {
            filterFields[index] = filterFields[index].copy(isUnfold = !filterFields[index].isUnfold)
        }
    }

    private fun validateFilters(): Boolean {
        var hasError = false

        // 2026/1/30 Gemini generate
        // 1. 统计每个 pattern 出现的次数 (排除掉空白，空白由 isBlank 处理)
        val patternCounts = filterFields.asSequence()
            .map { it.pattern.trim() }
            .filter { it.isNotEmpty() }
            .groupingBy { it }
            .eachCount()

        // 2. 遍历并更新状态
        for (i in filterFields.indices) {
            val current = filterFields[i]
            val pattern = current.pattern.trim()

            val isEmpty = pattern.isBlank()
            val isDuplicate = (patternCounts[pattern] ?: 0) > 1

            val shouldShowError = isEmpty || isDuplicate

            if (shouldShowError) hasError = true

            if (current.isError != shouldShowError) {
                filterFields[i] = current.copy(isError = shouldShowError)
            }
        }

        return !hasError
    }

    fun saveFilters() {
        if (!validateFilters()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val filters = filterFields.map { field ->
                GlobalFilterEntity(
                    type = field.filterType,
                    pattern = field.pattern,
                    isIgnoreCase = field.isIgnoreCase
                )
            }
            globalFilterDao.deleteAllFields()
            globalFilterDao.insertAll(filters = filters)
            _state.update { it.copy(isLoading = false) }
            _eventFlow.send(UiEvent.ShowToast("保存成功"))
        }
    }
}

class GlobalFiltersEditViewViewModelFactory(
    private val globalFilterDao: GlobalFilterDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GlobalFiltersEditViewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GlobalFiltersEditViewViewModel(globalFilterDao = globalFilterDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class FilterFieldState(
    val id: String = UUID.randomUUID().toString(),
    val pattern: String,
    val filterType: String,
    val isIgnoreCase: Boolean = false,
    val isUnfold: Boolean = false,
    val isError: Boolean = false
)