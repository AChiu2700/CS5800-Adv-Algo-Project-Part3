package com.notes.sort;

import com.notes.model.Note;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SortPreferenceTest {

  private static Note n(String title, String content, String created, String updated) {
    return new Note(
        java.util.UUID.randomUUID().toString(),
        title, content,
        Instant.parse(created), Instant.parse(updated),
        null
    );
  }

  @Test
  @DisplayName("Constructor sets initial sort order; getter returns it")
  void ctor_and_getter() {
    var pref = new SortPreference(SortOrder.LastModified);
    assertEquals(SortOrder.LastModified, pref.getSortOrder());
  }

  @Test
  @DisplayName("setSortOrder changes the order")
  void setSortOrder_changes() {
    var pref = new SortPreference(SortOrder.LastModified);
    pref.setSortOrder(SortOrder.TitleAZ);
    assertEquals(SortOrder.TitleAZ, pref.getSortOrder());
  }

  @Test
  @DisplayName("TitleAZ sorts A→Z using natural String order")
  void titleAZ_sorts() {
    var pref = new SortPreference(SortOrder.TitleAZ);
    var out = pref.apply(List.of(
      n("b","", "2025-10-01T00:00:00Z","2025-10-01T01:00:00Z"),
      n("A","", "2025-10-01T00:00:00Z","2025-10-01T01:00:00Z"),
      n("c","", "2025-10-01T00:00:00Z","2025-10-01T01:00:00Z")
    ));
    assertEquals(List.of("A","b","c"), out.stream().map(Note::getTitle).toList());
  }

  @ParameterizedTest(name = "CreatedDate sorts newest first (#{index})")
  @CsvSource({"2025-10-01T00:00:00Z, 2025-10-02T00:00:00Z, 2025-09-30T00:00:00Z"})
  void createdDate_newest_first(String d1, String d2, String d3) {
    var pref = new SortPreference(SortOrder.CreatedDate);
    var out = pref.apply(List.of(
      n("a","", d1, d1),
      n("b","", d2, d2),
      n("c","", d3, d3)
    ));
    assertEquals(List.of("b","a","c"), out.stream().map(Note::getTitle).toList());
  }

  @Test
  @DisplayName("apply on empty list returns empty list")
  void apply_empty_is_ok() {
    var pref = new SortPreference(SortOrder.TitleAZ);
    assertTrue(pref.apply(List.of()).isEmpty());
  }
}
