package com.notes.repo;

import com.notes.model.Note;
import com.notes.storage.InMemoryLocalStorage;
import com.notes.util.Clock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NoteRepositoryTest {

  private static Clock fixed(String iso) { return () -> Instant.parse(iso); }

  @Test
  @DisplayName("createNote persists with createdAt/updatedAt and appears in listNotes")
  void create_and_list() {
    var repo = new NoteRepository(new InMemoryLocalStorage(), fixed("2025-10-01T00:00:00Z"));
    var n = repo.createNote("A","Content");

    assertNotNull(n.getId());
    var list = repo.listNotes();
    assertEquals(1, list.size());
    assertEquals("A", list.get(0).getTitle());
    assertEquals(Instant.parse("2025-10-01T00:00:00Z"), list.get(0).getCreatedAt());
  }

  @Test
  @DisplayName("save updates title/content and bumps updatedAt")
  void save_updates_and_bumps_updatedAt() {
    var repo = new NoteRepository(new InMemoryLocalStorage(), fixed("2025-10-01T00:00:00Z"));
    var n = repo.createNote("A","C1");
    var before = repo.getNoteById(n.getId()).getUpdatedAt();

    // emulate edits: pass a Note instance with new title/content
    var edited = new Note(n.getId(), "A2", "C2", n.getCreatedAt(), before, null);
    var saved = repo.save(edited);

    assertEquals("A2", saved.getTitle());
    assertEquals("C2", saved.getContent());
    assertTrue(saved.getUpdatedAt().isAfter(before), "updatedAt should be bumped");
  }

  @Test
  @DisplayName("listNotes excludes deleted notes")
  void list_excludes_deleted() {
    var repo = new NoteRepository(new InMemoryLocalStorage(), fixed("2025-10-01T00:00:00Z"));
    var a = repo.createNote("A","..");
    var b = repo.createNote("B","..");

    repo.moveToTrash(b.getId());

    var list = repo.listNotes();
    assertEquals(1, list.size());
    assertEquals(a.getId(), list.get(0).getId());
  }

  @Test
  @DisplayName("restoreFromTrash clears deletedAt and returns to list")
  void restoreFromTrash_returns_to_list() {
    var repo = new NoteRepository(new InMemoryLocalStorage(), fixed("2025-10-01T00:00:00Z"));
    var n = repo.createNote("R","..");

    repo.moveToTrash(n.getId());
    assertNotNull(repo.getNoteById(n.getId()).getDeletedAt());

    repo.restoreFromTrash(n.getId());
    assertNull(repo.getNoteById(n.getId()).getDeletedAt());
    assertTrue(repo.listNotes().stream().anyMatch(x -> x.getId().equals(n.getId())));
  }

  @Test
  @DisplayName("purgeDeletedNotes removes note permanently")
  void purgeDeletedNotes_removes_perm() {
    var repo = new NoteRepository(new InMemoryLocalStorage(), fixed("2025-10-01T00:00:00Z"));
    var n = repo.createNote("X","..");

    repo.purgeDeletedNotes(n.getId());

    assertNull(repo.getNoteById(n.getId()));
  }
}
