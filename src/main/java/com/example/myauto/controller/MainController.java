package com.example.myauto.controller;

import com.example.myauto.entity.Listing;
import com.example.myauto.entity.User;
import com.example.myauto.service.ListingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    private final ListingService listingService;

    public MainController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("cars", listingService.getApprovedListings());
        // Добавляем пустые объекты для работы форм в модальных окнах
        model.addAttribute("listing", new Listing());
        model.addAttribute("user", new User());
        return "index";
    }
}