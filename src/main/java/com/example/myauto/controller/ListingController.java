package com.example.myauto.controller;

import com.example.myauto.entity.Listing;
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

        String username = principal.getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow();
        listing.setUser(currentUser);

        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        File folder = new File(uploadDir);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                    Path path = Paths.get(uploadDir + uniqueFileName);

                    Files.write(path, file.getBytes());

                    listing.getImages().add("/uploads/" + uniqueFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        listingService.saveListing(listing);
        return "redirect:/?success";
    }

    // НОВЫЙ МЕТОД: Открывает карточку детального просмотра автомобиля по ID
    @GetMapping("/{id}")
    public String viewListingDetail(@PathVariable("id") Long id, Model model) {
        Listing car = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено: " + id));
        model.addAttribute("car", car);
        return "listing/detail";
    }
}