package com.example.myauto.controller;

import com.example.myauto.entity.Listing;
import com.example.myauto.entity.ListingStatus;
import com.example.myauto.entity.User;
import com.example.myauto.repository.ListingRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    private final ListingRepository listingRepository;

    public MainController(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @GetMapping("/")
    public String homePage(Model model) {
        // Выводим только одобренные машины напрямую из базы
        model.addAttribute("cars", listingRepository.findAllByStatus(ListingStatus.APPROVED));

        // Добавляем пустые объекты для работы форм в модальных окнах
        model.addAttribute("listing", new Listing());
        model.addAttribute("user", new User());
        return "index";
    }
}