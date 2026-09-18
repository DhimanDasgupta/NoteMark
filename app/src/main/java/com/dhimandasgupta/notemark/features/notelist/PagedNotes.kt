package com.dhimandasgupta.notemark.features.notelist

import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.database.NoteEntity
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Presents the repository's `Flow<PagingData<NoteEntity>>` as plain lists, so the note list state
 * machine can keep a `List<NoteEntity>` in [NoteListState.NoteListStateWithNotes] instead of
 * handing a `PagingData` to the UI.
 *
 * Paging fetches the next page only when something reads an item near the end of what is already
 * loaded — a RecyclerView adapter or `LazyPagingItems` does that as the user scrolls. Nothing reads
 * items that way here, so [loadMore] is what advances the window; without it the pager would settle
 * after its initial load and never append.
 */
@Inject
@SingleIn(AppScope::class)
class PagedNotes(private val noteMarkRepository: NoteMarkRepository) {
  private val presenter = NoteSnapshotPresenter()

  /**
   * Every note paged in so far, re-emitted on each load, drop and refresh. Cold: collecting it
   * starts the pager, and the pager stops with the collector.
   */
  val snapshots: Flow<List<NoteEntity>> = channelFlow {
    launch {
      presenter.snapshots.collect { snapshot -> send(element = snapshot) }
    }
    noteMarkRepository.getPagedNotes().collectLatest { pagingData ->
      presenter.collectFrom(pagingData = pagingData)
    }
  }

  /** Asks Paging for the next page. Safe to call when everything is already loaded. */
  suspend fun loadMore() = presenter.loadMore()
}

@SingleIn(AppScope::class)
private class NoteSnapshotPresenter :
  PagingDataPresenter<NoteEntity>(mainContext = Dispatchers.Main) {
  val snapshots = MutableStateFlow<List<NoteEntity>>(value = emptyList())

  /** How many notes the screen has asked for. 0 means "whatever the initial load brings". */
  private var desiredCount = 0

  /** Size of the window when the last page was requested, so the same window is not asked twice. */
  private var requestedAtSize = -1

  override suspend fun presentPagingDataEvent(event: PagingDataEvent<NoteEntity>) {
    // A refresh rebuilds the window from the initial page, so a request made against the previous
    // window no longer counts against the new one.
    if (event is PagingDataEvent.Refresh) requestedAtSize = -1

    val items = snapshot().items
    snapshots.value = items

    // Every write to the notes table invalidates the paging source, and Paging then refreshes back
    // to the initial page alone. A scrolling list would re-read its visible items and pull the rest
    // back in on its own; nothing here does, so climb back to what had already been paged in.
    if (items.size < desiredCount) requestNextPage()
  }

  suspend fun loadMore() =
    withContext(context = Dispatchers.Main) {
      desiredCount = size + 1
      requestNextPage()
    }

  /**
   * Reads the last loaded note, which is what sends Paging the access hint that triggers an append.
   * The [requestedAtSize] guard keeps a window that cannot grow any further — the end of the table
   * — from being asked over and over.
   */
  private fun requestNextPage() {
    val loaded = size
    if (loaded == 0 || loaded == requestedAtSize) return
    requestedAtSize = loaded
    get(index = loaded - 1)
  }
}
