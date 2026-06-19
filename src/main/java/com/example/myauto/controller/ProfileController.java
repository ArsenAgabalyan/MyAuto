package com.example.myauto.controller;

import com.example.myauto.entity.Listing;
import com.example.myauto.entity.Role;
import com.example.myauto.entity.User;
import com.example.myauto.repository.ListingRepository;
import com.example.myauto.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public ProfileController(ListingRepository listingRepository, UserRepository userRepository) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String profilePage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        // Берем машины пользователя напрямую из базы данных
        model.addAttribute("myCars", listingRepository.findAllByUserOrderByCreatedAtDesc(user));
        return "profile";
    }

    @PostMapping("/listings/{id}/delete")
    public String deleteMyListing(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        // Удалить может либо владелец машины, либо администратор
        if (currentUser.getRole() == Role.ROLE_ADMIN || listing.getUser().getUsername().equals(principal.getName())) {
            listingRepository.delete(listing);
        }

        return "redirect:/profile?deleted";
    }
}