package xyz.poeschl.slowwave.filter

interface BaseFilter<T> {
  fun applyFilter(input: T): T
}
