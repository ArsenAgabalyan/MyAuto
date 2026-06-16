package com.example.myauto.controller;

import com.example.myauto.service.ListingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ListingService listingService;

    public AdminController(ListingService listingService) {
        this.listingService = listingService;
    }

    // Страница модерации
    @GetMapping("/moderation")
    public String moderationPage(Model model) {
        model.addAttribute("pendingCars", listingService.getPendingListings());
        return "admin/moderation";
    }

    // Кнопка: Одобрить
    @PostMapping("/listings/{id}/approve")
    public String approve(@PathVariable("id") Long id) {
        listingService.approveListing(id);
        return "redirect:/admin/moderation";
    }

    // Кнопка: Удалить/Отклонить
    @PostMapping("/listings/{id}/reject")
    public String reject(@PathVariable("id") Long id) {
        listingService.deleteListing(id);
        return "redirect:/admin/moderation";
    }
}