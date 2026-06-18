package com.example.myauto.controller;

import com.example.myauto.entity.User;
import com.example.myauto.repository.UserRepository;
import com.example.myauto.service.ListingService;
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

    private final ListingService listingService;
    private final UserRepository userRepository;

    public ProfileController(ListingService listingService, UserRepository userRepository) {
        this.listingService = listingService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String profilePage(Model model, Principal principal) {
        if (principal == null) return "redirect:/auth/login";

        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        model.addAttribute("myCars", listingService.getListingsByUser(user));
        return "profile";
    }

    @PostMapping("/listings/{id}/delete")
    public String deleteMyListing(@PathVariable Long id, Principal principal) {
        if (principal == null) return "redirect:/auth/login";

        // ТЕПЕРЬ ПЕРЕДАЕМ 3 ПАРАМЕТРА:
        // 1. id объявления
        // 2. имя пользователя
        // 3. false (так как обычный пользователь не админ)
        listingService.deleteUserListing(id, principal.getName(), false);

        return "redirect:/profile?deleted";
    }
}