package com.example.myauto.controller;

import com.example.myauto.entity.Listing;
import com.example.myauto.entity.ListingStatus;
import com.example.myauto.repository.ListingRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ListingRepository listingRepository;

    public AdminController(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @GetMapping("/moderation")
    public String moderationPage(Model model) {
        // Берем данные напрямую из БД
        model.addAttribute("pendingCars", listingRepository.findAllByStatus(ListingStatus.PENDING));
        model.addAttribute("approvedCars", listingRepository.findAllByStatus(ListingStatus.APPROVED));
        return "admin/moderation";
    }

    @PostMapping("/listings/{id}/approve")
    public String approve(@PathVariable("id") Long id) {
        Listing listing = listingRepository.findById(id).orElseThrow();
        listing.setStatus(ListingStatus.APPROVED);
        listingRepository.save(listing);
        return "redirect:/admin/moderation";
    }

    @PostMapping("/listings/{id}/reject")
    public String reject(@PathVariable("id") Long id) {
        listingRepository.deleteById(id);
        return "redirect:/admin/moderation";
    }
}