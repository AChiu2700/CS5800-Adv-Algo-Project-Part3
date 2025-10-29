package com.notes.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class NoteTest {

  private static Instant ISO(String s) { return Instant.parse(s); }

  @Test
  @DisplayName("markDelete sets deletedAt; clearDelete unsets it")
  void mark_and_clear_deleted() {
    var n = new Note("T","C", ISO("2025-10-01T00:00:00Z"));

    n.markDelete(ISO("2025-10-02T00:00:00Z"));
    assertEquals(ISO("2025-10-02T00:00:00Z"), n.getDeletedAt());

    n.clearDelete();
    assertNull(n.getDeletedAt());
  }

  @Test
  @DisplayName("updateTitle updates title and touches updatedAt")
  void updateTitle_updates_updatedAt() {
    var n = new Note("Old","C", ISO("2025-10-01T00:00:00Z"));
    n.updateTitle("New");
    assertEquals("New", n.getTitle());
    assertNotNull(n.getUpdatedAt());
  }

  @Test
  @DisplayName("updateContent updates content and touches updatedAt")
  void updateContent_updates_updatedAt() {
    var n = new Note("T","Old", ISO("2025-10-01T00:00:00Z"));
    n.updateContent("New");
    assertEquals("New", n.getContent());
    assertNotNull(n.getUpdatedAt());
  }
}
