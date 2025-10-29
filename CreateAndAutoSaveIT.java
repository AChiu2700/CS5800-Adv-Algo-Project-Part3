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

class CreateAndAutosaveIT {
  private static Clock fixed(String iso){ return () -> Instant.parse(iso); }

  @Test
  void create_and_edit_persists_note() {
    var clock = fixed("2025-10-01T00:00:00Z");
    var repo  = new NoteRepository(new InMemoryLocalStorage(), clock);
    var app   = new AppController(
        repo,
        new Trash(30, clock),
        new SearchIndex(),
        new SortPreference(SortOrder.LastModified)
    );

    var n = app.newNote();
    app.editNote(n.getId(), "Title", "Body v1");

    var saved = repo.getNoteById(n.getId());
    assertEquals("Title", saved.getTitle());
    assertEquals("Body v1", saved.getContent());
    assertNotNull(saved.getCreatedAt());
    assertNotNull(saved.getUpdatedAt());
  }
}
