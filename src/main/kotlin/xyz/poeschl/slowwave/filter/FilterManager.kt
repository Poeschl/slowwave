package xyz.poeschl.slowwave.filter

import xyz.poeschl.slowwave.FilterException

class FilterManager<T> {

  private val registeredFilter = mutableListOf<BaseFilter<T>>()

  fun addFilter(filter: BaseFilter<T>) {
    registeredFilter.add(filter)
  }

  fun clearFilter() {
    registeredFilter.clear()
  }

  @Throws(FilterException::class)
  fun applyAllFilter(request: T): T {
    var currentList = request
    registeredFilter.forEach { currentList = it.applyFilter(currentList) }
    return currentList
  }
}
