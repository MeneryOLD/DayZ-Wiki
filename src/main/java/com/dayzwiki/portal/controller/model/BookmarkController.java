package com.dayzwiki.portal.controller.model;

import com.dayzwiki.portal.model.Bookmark;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.item.BookmarkRepository;
import com.dayzwiki.portal.repository.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookmarks")
public class BookmarkController {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Bookmark>> getBookmarksByUserId(Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
        if (optionalUser.isPresent()) {
            List<Bookmark> bookmarks = bookmarkRepository.findAllByUserId(optionalUser.get().getId());
            return new ResponseEntity<>(bookmarks, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<String> createBookmark(@RequestBody Bookmark bookmark, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (bookmarkRepository.countAllByUserId(user.getId()) >= 10) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User cannot have more than 10 bookmarks");
        }

        if (bookmarkRepository.existsByUserAndUrl(user, bookmark.getUrl())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Bookmark already exists");
        }

        bookmark.setUser(user);
        bookmarkRepository.save(bookmark);

        return ResponseEntity.ok("Bookmark added successfully");
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<String> removeBookmark(@PathVariable Long id) {
        bookmarkRepository.deleteById(id);
        return ResponseEntity.ok("Bookmark removed successfully");
    }

}
