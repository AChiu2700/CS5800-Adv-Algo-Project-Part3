package com.notes.app;

import com.notes.repo.NoteRepository;
import com.notes.repo.Trash;
import com.notes.search.SearchIndex;
import com.notes.sort.SortOrder;
import com.notes.sort.SortPreference;
import com.notes.storage.InMemoryLocalStorage;
import com.notes.util.Clock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppControllerTest {

  private static Clock fixed(String iso){ return () -> Instant.parse(iso); }

  @Test
  @DisplayName("newNote creates and returns an empty note")
  void newNote_creates() {
    var repo  = new NoteRepository(new InMemoryLocalStorage(), fixed("2025-10-01T00:00:00Z"));
    var trash = new Trash(30, fixed("2025-10-01T00:00:00Z"));
    var index = new SearchIndex();
    var pref  = new SortPreference(SortOrder.LastModified);

    var app = new AppController(repo, trash, index, pref);

    var n = app.newNote();
    assertNotNull(n.getId());
    assertEquals("", repo.getNoteById(n.getId()).getTitle());
  }

  @Test
  @DisplayName("editNote on unknown id throws meaningful exception")
  void edit_unknown_throws() {
    var repo  = new NoteRepository(new InMemoryLocalStorage(), fixed("2025-10-01T00:00:00Z"));
    var app   = new AppController(
        repo,
        new Trash(30, fixed("2025-10-01T00:00:00Z")),
        new SearchIndex(),
        new SortPreference(SortOrder.LastModified)
    );

    var ex = assertThrows(IllegalArgumentException.class,
      () -> app.editNote("missing-id", "T","C"));
    assertTrue(ex.getMessage().contains("Note not found"));
  }

  @Test
  @DisplayName("deleteNote moves to trash and schedules auto purge")
  void delete_moves_and_schedules() {
    var clock = fixed("2025-10-01T00:00:00Z");
    var repo  = spy(new NoteRepository(new InMemoryLocalStorage(), clock));
    var trash = spy(new Trash(30, clock));
    var app   = new AppController(repo, trash, new SearchIndex(), new SortPreference(SortOrder.LastModified));

    var n = app.newNote();

    app.deleteNote(n.getId());

    verify(repo).moveToTrash(n.getId());
    verify(trash).autoPurge();
    assertNotNull(repo.getNoteById(n.getId()).getDeletedAt());
  }

  @Test
  @DisplayName("setSortOrder affects getListOfNotes ordering (TitleAZ)")
  void setSortOrder_affects_getListOfNotes() {
    var clock = fixed("2025-10-01T00:00:00Z");
    var repo  = new NoteRepository(new InMemoryLocalStorage(), clock);
    var app   = new AppController(
        repo,
        new Trash(30, clock),
        new SearchIndex(),
        new SortPreference(SortOrder.LastModified)
    );

    repo.createNote("b","..");
    repo.createNote("A","..");

    app.setSortOrder(SortOrder.TitleAZ);
    var list = app.getListOfNotes();

    assertEquals("A", list.get(0).getTitle());
  }

  @Test
  @DisplayName("restoreNote clears deletedAt and returns note to list")
  void restoreNote_clears_deletedAt() {
    var clock = fixed("2025-10-01T00:00:00Z");
    var repo  = new NoteRepository(new InMemoryLocalStorage(), clock);
    var trash = new Trash(30, clock);
    var app   = new AppController(repo, trash, new SearchIndex(), new SortPreference(SortOrder.LastModified));

    var n = repo.createNote("R","..");
    app.deleteNote(n.getId());
    assertNotNull(repo.getNoteById(n.getId()).getDeletedAt());

    app.restoreNote(n.getId());
    assertNull(repo.getNoteById(n.getId()).getDeletedAt());
    assertTrue(app.getListOfNotes().stream().anyMatch(x -> x.getId().equals(n.getId())));
  }
}
