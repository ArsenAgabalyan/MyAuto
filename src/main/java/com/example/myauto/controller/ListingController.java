package com.example.myauto.controller;

import com.example.myauto.entity.Listing;
import com.example.myauto.entity.User;
import com.example.myauto.repository.UserRepository;
import com.example.myauto.service.ListingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/listings")
public class ListingController {

    private final ListingService listingService;
    private final UserRepository userRepository;

    public ListingController(ListingService listingService, UserRepository userRepository) {
        this.listingService = listingService;
        this.userRepository = userRepository;
    }

    // Открыть страницу добавления авто
    @GetMapping("/add")
    public String addListingPage(Model model) {
        model.addAttribute("listing", new Listing());
        return "listing/add";
    }

    // Сохранить авто в БД
    @PostMapping("/add")
    public String saveListing(@ModelAttribute("listing") Listing listing, Principal principal) {
        // Находим текущего авторизованного пользователя по его логину
        String username = principal.getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow();

        // Привязываем автора к объявлению
        listing.setUser(currentUser);

        // Сохраняем в БД (статус автоматически будет PENDING)
        listingService.saveListing(listing);

        return "redirect:/?success"; // Возвращаем на главную
    }
}