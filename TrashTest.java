package com.notes.repo;

import com.notes.model.Note;
import com.notes.util.Clock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TrashTest {

  private static Clock fixed(String iso) { return () -> Instant.parse(iso); }
  private static Instant ISO(String s) { return Instant.parse(s); }

  @Test
  @DisplayName("add includes only notes that are already marked deleted")
  void add_includes_only_deleted() {
    var trash = new Trash(30, fixed("2025-10-01T00:00:00Z"));

    var deleted = new Note("A", "B", ISO("2025-09-01T00:00:00Z"));
    deleted.markDelete(ISO("2025-09-15T00:00:00Z"));

    var notDeleted = new Note("B", "C", ISO("2025-09-01T00:00:00Z"));
    // notDeleted has deletedAt == null

    trash.add(deleted);
    trash.add(notDeleted);

    assertEquals(1, trash.listDeletedNotes().size());
    assertEquals("A", trash.listDeletedNotes().get(0).getTitle());
  }

  @Test
  @DisplayName("remove returns true when a deleted note is removed; false otherwise")
  void remove_returns_boolean() {
    var trash = new Trash(30, fixed("2025-10-01T00:00:00Z"));

    var n = new Note("ToRemove", "x", ISO("2025-09-20T00:00:00Z"));
    n.markDelete(ISO("2025-09-21T00:00:00Z"));

    trash.add(n);
    assertEquals(1, trash.listDeletedNotes().size());

    assertTrue(trash.remove(n));
    assertEquals(0, trash.listDeletedNotes().size());

    // Removing again should return false
    assertFalse(trash.remove(n));
  }

  @Test
  @DisplayName("purgeExpired(now) removes notes where (now - deletedAt) >= retentionDays")
  void purgeExpired_removes_by_retention() {
    var now   = ISO("2025-10-01T00:00:00Z");
    var trash = new Trash(30, () -> now);

    // deleted 63, 31, 30 days ago → all should be purged by purgeExpired(now)
    var d63 = new Note("63d", "B", ISO("2025-07-30T00:00:00Z"));
    d63.markDelete(ISO("2025-07-30T00:00:00Z"));
    var d31 = new Note("31d", "B", ISO("2025-08-31T00:00:00Z"));
    d31.markDelete(ISO("2025-08-31T00:00:00Z"));
    var d30 = new Note("30d", "B", ISO("2025-09-01T00:00:00Z"));
    d30.markDelete(ISO("2025-09-01T00:00:00Z"));

    trash.add(d63);
    trash.add(d31);
    trash.add(d30);

    trash.purgeExpired(now);

    assertEquals(0, trash.listDeletedNotes().size(), "All ≥30d old should be purged by purgeExpired(now)");
  }

  @Test
  @DisplayName("autoPurge() uses now-30d, so with retention=30 it effectively purges notes deleted ≥60 days ago")
  void autoPurge_effectively_removes_60d_plus() {
    var now   = ISO("2025-10-01T00:00:00Z");
    var trash = new Trash(30, () -> now);

    // For autoPurge: purgeExpired(now - 30d)
    // Removal condition becomes (now-30d - deletedAt) >= 30  ⇒ deletedAt ≤ now-60d

    var d61 = new Note("61d", "B", ISO("2025-08-01T00:00:00Z")); // 61 days ago
    d61.markDelete(ISO("2025-08-01T00:00:00Z"));

    var d59 = new Note("59d", "B", ISO("2025-08-03T00:00:00Z")); // 59 days ago
    d59.markDelete(ISO("2025-08-03T00:00:00Z"));

    var d31 = new Note("31d", "B", ISO("2025-08-31T00:00:00Z")); // 31 days ago
    d31.markDelete(ISO("2025-08-31T00:00:00Z"));

    trash.add(d61);
    trash.add(d59);
    trash.add(d31);

    trash.autoPurge();

    // Expect only the ≥60d old note to be removed under current autoPurge behavior
    var remainingTitles = trash.listDeletedNotes().stream().map(Note::getTitle).toList();
    assertFalse(remainingTitles.contains("61d"), "61d-old note should be purged");
    assertTrue(remainingTitles.contains("59d"),  "59d-old note should remain");
    assertTrue(remainingTitles.contains("31d"),  "31d-old note should remain");
  }

  @Test
  @DisplayName("getters expose retentionDays and clock")
  void getters_work() {
    var clock = fixed("2025-10-01T00:00:00Z");
    var trash = new Trash(45, clock);
    assertEquals(45, trash.getRetentionDays());
    assertEquals(clock, trash.getClock());
  }
}
