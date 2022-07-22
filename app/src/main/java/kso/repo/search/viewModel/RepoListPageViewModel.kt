package kso.repo.search.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import kso.repo.search.app.CurrentNetworkStatus
import kso.repo.search.app.NetworkStatusDetector
import kso.repo.search.app.map
import kso.repo.search.dataSource.preference.PreferenceProvider
import kso.repo.search.model.Repo
import kso.repo.search.model.Resource
import kso.repo.search.repository.RepoSearchBaseRepository
import kso.repo.search.ui.state.NetworkConnectionState
import javax.inject.Inject

@HiltViewModel
class RepoListPageViewModel @Inject constructor(

    savedStateHandle: SavedStateHandle,
    private val repository: RepoSearchBaseRepository,
    networkStatusDetector: NetworkStatusDetector,
    private val preferenceProvider: PreferenceProvider,
    private val application: Application

) :
    ViewModel() {

    private val tag: String = "RepoListPageViewModel"
    private val repoName: String = savedStateHandle.get<String>("repo_name").orEmpty()
    val searchText = MutableLiveData(repoName)
    private val repoList = MutableLiveData<Resource<List<Repo>>>()
    val repos: LiveData<Resource<List<Repo>>> get() = repoList

    @OptIn(FlowPreview::class)
    val networkState =
        networkStatusDetector.networkStatus
            .map (
                onAvailable = { NetworkConnectionState.Fetched },
                onUnavailable = { NetworkConnectionState.Error },
            )

    val isRefreshing = MutableLiveData<Boolean>()
    val showSearchTextEmptyToast = MutableLiveData<Boolean>()
    val isConnected = MutableLiveData<Boolean>()

    init {

        Log.e(tag, "init")
        Log.e(tag, "Argument: $repoName")
        Log.e(tag, "SearchText: ${searchText.value}")

        submit()

    }


    @OptIn(FlowPreview::class)
    fun submit() {
        Log.e(tag, "fetch RepoList")

        viewModelScope.launch {
            Log.e(tag, "in ViewModelScope")
            Log.e(tag, "preferenceKeyword: ${preferenceProvider.getSearchKeyword()}")
            if(searchText.value?.isEmpty() == true){
                showSearchTextEmptyToast.value = true
            }else {
                showSearchTextEmptyToast.value = false
                if (preferenceProvider.getSearchKeyword() == searchText.value) {
                    Log.e(tag, "Not Need connection")
                    repository.getRepoListNetworkBoundResource(searchText.value!!).collect {
                        repoList.value = it
                    }
                } else {
                    if (CurrentNetworkStatus.getNetwork(application.applicationContext)) {
                        repository.getRepoListNetworkBoundResource(searchText.value!!).collect {
                            repoList.value = it
                        }
                    } else {
                        Log.e(tag, "Need connection")
                    }
                }
            }

            networkState.collect{
                isConnected.value = when (it) {
                    NetworkConnectionState.Fetched  -> {
                        Log.e(tag, "Network Status: Fetched")
                        true
                    }
                    else -> {
                        Log.e(tag, "Network Status: Error")
                        false
                    }
                }
            }

        }

    }

    fun retry() {
        Log.e(tag, "Retry:")
        Log.e(tag, "searchText ${searchText.value}")

        submit()
    }

    fun refresh(){
        Log.e(tag, "Refresh:")
        Log.e(tag, "searchText ${searchText.value}")

        isRefreshing.value = true
        submit()
    }

    fun onDoneCollectResource(){
        Log.e(tag, "onDoneCollectResource()")
        isRefreshing.value = false
    }

    fun showSearchTextEmptyToastCollected(){
        Log.e(tag, "showSearchTextEmptyToastCollected()")
        showSearchTextEmptyToast.value = false
    }

    fun onSearchTextChanged(changedSearchText: String) {

        Log.e(tag, "onSearchTextChanged: keyword ${searchText.value}")
        searchText.value = changedSearchText

    }
    fun onKeyboardSearchClick(query: String) {

        Log.e(tag, "onSearchTextChanged: keyword ${searchText.value}")
        searchText.value = query
        submit()

    }

    fun onSearchBoxClear() {
        Log.e(tag, "onSearchClear: ")
        searchText.value = ""
    }

}