package com.davismiyashiro.weathermapapp.presentation

import com.airbnb.mvrx.*
import com.davismiyashiro.weathermapapp.data.Place
import com.davismiyashiro.weathermapapp.domain.RepositoryInterface
import com.davismiyashiro.weathermapapp.injection.AssistedViewModelFactory
import com.davismiyashiro.weathermapapp.injection.hiltMavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.disposables.Disposable.disposed
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.ArrayList

/**
 *  TODO: move Mavericks RxJava3 when supported
 */
@InternalMavericksApi
class ForecastListViewModel @AssistedInject constructor(
    @Assisted initialState: ForecastListState,
    private val repo: RepositoryInterface
) : MavericksViewModel<ForecastListState>(initialState) {

    private val disposables: CompositeDisposable = CompositeDisposable()

    init {
        setState {
            copy(forecast = Loading())
        }

        repo.loadWeatherData()
            .map { mapPlaceToForecastListItem(it) }
            .execute {
               copy(forecast = it)
            }
    }

    @AssistedFactory
    interface Factory : AssistedViewModelFactory<ForecastListViewModel, ForecastListState> {
        override fun create(state: ForecastListState): ForecastListViewModel
    }

    companion object :
        MavericksViewModelFactory<ForecastListViewModel, ForecastListState> by hiltMavericksViewModelFactory()

    private fun mapPlaceToForecastListItem(data: Place?): List<ForecastListItem> {

        val items = ArrayList<ForecastListItem>()

        data?.let {
            it.list?.let { conditions ->
                for (condition in conditions) {
                    items.add(ForecastListItem(condition))
                }
            }
        }

        return items
    }

    fun loadWeatherData(refreshTasks: Boolean) {

        if (refreshTasks) {
            repo.refreshFromRemote()
        }

        repo.loadWeatherData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { mapPlaceToForecastListItem(it) }
            .execute {
                copy(forecast = it)
            }.disposeOnClear()
    }

    /**
     * Helper to map an [Observable] to an [Async] property on the state object.
     */
    @InternalMavericksApi
    fun <T> Observable<T>.execute(
        stateReducer: ForecastListState.(Async<T>) -> ForecastListState
    ) = execute({ it }, null, stateReducer)

    /**
     * Execute an [Observable] and wrap its progression with [Async] property reduced to the global state.
     *
     * @param mapper A map converting the Observable type to the desired Async type.
     * @param successMetaData A map that provides metadata to set on the Success result.
     *                        It allows data about the original Observable to be kept and accessed later. For example,
     *                        your mapper could map a network request to just the data your UI needs, but your base layers could
     *                        keep metadata about the request, like timing, for logging.
     * @param stateReducer A reducer that is applied to the current state and should return the
     *                     new state. Because the state is the receiver and it likely a data
     *                     class, an implementation may look like: `{ copy(response = it) }`.
     *
     *  @see Success.metadata
     */
    @InternalMavericksApi
    fun <T, V> Observable<T>.execute(
        mapper: (T) -> V,
        successMetaData: ((T) -> Any)? = null,
        stateReducer: ForecastListState.(Async<V>) -> ForecastListState
    ): Disposable {
        val blockExecutions = config.onExecute(this@ForecastListViewModel)
        if (blockExecutions != MavericksViewModelConfig.BlockExecutions.No) {
            if (blockExecutions == MavericksViewModelConfig.BlockExecutions.WithLoading) {
                setState { stateReducer(Loading()) }
            }
            return disposed()
        }

        // Intentionally didn't use RxJava's startWith operator. When withState is called right after execute then the loading reducer won't be enqueued yet if startWith is used.
        setState { stateReducer(Loading()) }

        return map<Async<V>> { value ->
            val success = Success(mapper(value))
            success.metadata = successMetaData?.invoke(value)
            success
        }
            .onErrorReturn { e ->
                Fail(e)
            }
            .subscribe { asyncData -> setState { stateReducer(asyncData) } }
            .disposeOnClear()
    }

    private fun Disposable.disposeOnClear(): Disposable {
        disposables.add(this)
        return this
    }
}