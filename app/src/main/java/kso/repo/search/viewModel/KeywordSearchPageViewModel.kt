package kso.repo.search.viewModel

import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kso.repo.search.model.Keyword
import kso.repo.search.model.Resource
import kso.repo.search.repository.KeywordSearchBaseRepository
import javax.inject.Inject


@HiltViewModel
class KeywordSearchPageViewModel @Inject constructor(
    private val appRepository: KeywordSearchBaseRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val tag: String = "KeywordPageViewModel"
    private val repoName: String = savedStateHandle.get<String>("repo").orEmpty()

    val searchText = MutableLiveData(repoName)

    private val keywordList = MutableLiveData<Resource<List<Keyword>>>()
    val keywords: LiveData<Resource<List<Keyword>>> get() = keywordList


    init {

        Log.e(tag, "init")
        Log.e(tag, "Argument: $repoName")
        Log.e(tag, "SearchText: ${searchText.value}")

        submit()

    }


    private fun submit() {
        Log.e(tag, "fetchAllKeywordList()")

        viewModelScope.launch {
            Log.e(tag, "in ViewModelScope")
            appRepository.getKeywordListNetworkBoundResource(searchText.value!!).collect {
                keywordList.value = it
            }
        }

    }

    fun retry() {
        Log.e(tag, "Retry:")
        submit()
    }

    fun onSearchTextChanged(changedSearchText: String) {

        Log.e(tag, "onSearchTextChanged: keyword ${searchText.value}")
        searchText.value = changedSearchText
        submit()

    }

    fun onSearchBoxClear() {
        Log.e(tag, "onSearchClear: ")
        searchText.value = ""
        submit()
    }

}
