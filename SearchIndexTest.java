package com.notes.search;

import com.notes.model.Note;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchIndexTest {

  private static Note n(String t, String c){ return new Note(t, c, Instant.parse("2025-10-01T00:00:00Z")); }

  @Test
  @DisplayName("index stores a snapshot for future searches")
  void index_stores_snapshot() {
    var idx = new SearchIndex();
    var notes = List.of(n("A","x"), n("B","y"));
    idx.index(notes);
    assertEquals(1, idx.search("a").size()); // only "A" matches
  }

  @Test
  @DisplayName("search matches title OR content, case-insensitive, partial")
  void search_title_or_content_case_insensitive() {
    var idx = new SearchIndex();
    idx.index(List.of(
      n("Project Plan", "alpha"),
      n("Groceries", "buy projector bulb"),
      n("Other", "…")
    ));
    var hits = idx.search("project");
    assertEquals(2, hits.size(), "should match title 'Project' and content 'projector'");
  }

  @Test
  @DisplayName("search blank returns empty list")
  void search_blank_returns_empty() {
    var idx = new SearchIndex();
    idx.index(List.of(n("A","B")));
    assertTrue(idx.search("   ").isEmpty());
    assertTrue(idx.search(null).isEmpty());
  }
}
