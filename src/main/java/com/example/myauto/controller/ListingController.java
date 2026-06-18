package com.example.myauto.controller;

import com.example.myauto.entity.Listing;
import com.example.myauto.entity.Role;
import com.example.myauto.entity.User;
import com.example.myauto.repository.UserRepository;
import com.example.myauto.repository.ListingRepository;
import com.example.myauto.service.ListingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/listings")
public class ListingController {

    private final ListingService listingService;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    public ListingController(ListingService listingService, UserRepository userRepository, ListingRepository listingRepository) {
        this.listingService = listingService;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
    }

    @GetMapping("/add")
    public String addListingPage(Model model) {
        model.addAttribute("listing", new Listing());
        return "listing/add";
    }

    @PostMapping("/add")
    public String saveListing(@ModelAttribute("listing") Listing listing,
                              @RequestParam("imageFiles") MultipartFile[] files,
                              Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        listing.setUser(currentUser);

        handleFiles(listing, files);

        listingService.saveListing(listing);
        return "redirect:/?success";
    }

    @GetMapping("/edit/{id}")
    public String editListingPage(@PathVariable Long id, Model model, Principal principal) {
        Listing listing = listingRepository.findById(id).orElseThrow();
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();

        // Проверка доступа: владелец или админ
        if (!listing.getUser().getUsername().equals(principal.getName()) && currentUser.getRole() != Role.ROLE_ADMIN) {
            return "redirect:/";
        }

        model.addAttribute("listing", listing);
        model.addAttribute("isAdmin", currentUser.getRole() == Role.ROLE_ADMIN);
        return "listing/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateListing(@PathVariable Long id, @ModelAttribute("listing") Listing listing, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        boolean isAdmin = (currentUser.getRole() == Role.ROLE_ADMIN);

        listingService.updateListing(id, listing, isAdmin);

        return isAdmin ? "redirect:/admin/moderation" : "redirect:/profile?updated";
    }

    @GetMapping("/{id}")
    public String viewListingDetail(@PathVariable("id") Long id, Model model) {
        Listing car = listingRepository.findById(id).orElseThrow();
        model.addAttribute("car", car);
        return "listing/detail";
    }

    private void handleFiles(Listing listing, MultipartFile[] files) {
        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        new File(uploadDir).mkdirs();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                    Files.write(Paths.get(uploadDir + uniqueFileName), file.getBytes());
                    listing.getImages().add("/uploads/" + uniqueFileName);
                } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }
}