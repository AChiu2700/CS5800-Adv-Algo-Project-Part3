package com.notes.it;

import com.notes.model.Note;
import com.notes.repo.NoteRepository;
import com.notes.repo.Trash;
import com.notes.search.SearchIndex;
import com.notes.sort.SortOrder;
import com.notes.sort.SortPreference;
import com.notes.storage.InMemoryLocalStorage;
import com.notes.util.Clock;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchAndSortIT {
  private static Clock fixed(String iso){ return () -> Instant.parse(iso); }

  @Test
  void repo_to_index_to_sort_flow() {
    var clock = fixed("2025-10-01T00:00:00Z");
    var repo  = new NoteRepository(new InMemoryLocalStorage(), clock);
    var index = new SearchIndex();
    var pref  = new SortPreference(SortOrder.LastModified);

    // Seed notes
    repo.createNote("Project Alpha","…");
    repo.createNote("Groceries","projector bulb");
    repo.createNote("Zebra","…");

    // Build index from repository data
    index.index(repo.listNotes());

    // Search
    List<Note> matches = index.search("project");
    assertEquals(2, matches.size());

    // Sort the matches by Title A→Z
    pref.setSortOrder(SortOrder.TitleAZ);
    List<Note> sortedMatches = pref.apply(matches);

    assertEquals("Groceries", sortedMatches.get(0).getTitle());
    assertEquals("Project Alpha", sortedMatches.get(1).getTitle());
  }
}
