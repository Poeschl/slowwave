package xyz.poeschl.slowwave.filter

class FilterManager<T> {

  private val registeredFilter = mutableListOf<BaseFilter<T>>()

  fun addFilter(filter: BaseFilter<T>) {
    registeredFilter.add(filter)
  }

  fun clearFilter() {
    registeredFilter.clear()
  }

  fun applyAllFilter(request: T): T {
    var currentList = request
    registeredFilter.forEach { currentList = it.applyFilter(currentList) }
    return currentList
  }
}
