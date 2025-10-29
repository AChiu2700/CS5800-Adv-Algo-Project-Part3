package com.notes.it;

import com.notes.app.AppController;
import com.notes.repo.NoteRepository;
import com.notes.repo.Trash;
import com.notes.search.SearchIndex;
import com.notes.sort.SortOrder;
import com.notes.sort.SortPreference;
import com.notes.storage.InMemoryLocalStorage;
import com.notes.util.Clock;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DeleteRestoreIT {
  private static Clock fixed(String iso){ return () -> Instant.parse(iso); }

  @Test
  void delete_then_restore_flow() {
    var clock = fixed("2025-10-01T00:00:00Z");
    var repo  = new NoteRepository(new InMemoryLocalStorage(), clock);
    var app   = new AppController(
        repo,
        new Trash(30, clock),
        new SearchIndex(),
        new SortPreference(SortOrder.LastModified)
    );

    var n = repo.createNote("KeepMe","…");

    app.deleteNote(n.getId());
    assertNotNull(repo.getNoteById(n.getId()).getDeletedAt(), "should be in trash");

    app.restoreNote(n.getId());
    assertNull(repo.getNoteById(n.getId()).getDeletedAt(), "should be restored");
    assertTrue(app.getListOfNotes().stream().anyMatch(x -> x.getId().equals(n.getId())));
  }
}
