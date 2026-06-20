package com.example.myauto.controller;

import com.example.myauto.entity.Listing;
import com.example.myauto.entity.ListingStatus;
import com.example.myauto.repository.ChatMessageRepository;
import com.example.myauto.repository.ListingRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ListingRepository listingRepository;
    private final ChatMessageRepository chatMessageRepository;

    public AdminController(ListingRepository listingRepository, ChatMessageRepository chatMessageRepository) {
        this.listingRepository = listingRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @GetMapping("/moderation")
    public String moderationPage(Model model) {
        // Получаем все объявления, отсортированные по ID в обратном порядке (сначала новые)
        model.addAttribute("allCars", listingRepository.findAllByOrderByIdDesc());
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
        Listing listing = listingRepository.findById(id).orElseThrow();
        listing.setStatus(ListingStatus.REJECTED);
        listingRepository.save(listing);
        return "redirect:/admin/moderation";
    }

    @PostMapping("/listings/{id}/delete")
    @Transactional
    public String delete(@PathVariable("id") Long id) {
        chatMessageRepository.deleteByListingId(id);
        listingRepository.deleteById(id);
        return "redirect:/admin/moderation";
    }

    @PostMapping("/listings/bulk-approve")
    @Transactional
    public String bulkApprove(@RequestParam("ids") String idsStr) {
        List<Long> ids = parseIds(idsStr);
        if (!ids.isEmpty()) {
            List<Listing> listings = listingRepository.findAllById(ids);
            for (Listing listing : listings) {
                listing.setStatus(ListingStatus.APPROVED);
            }
            listingRepository.saveAll(listings);
        }
        return "redirect:/admin/moderation";
    }

    @PostMapping("/listings/bulk-delete")
    @Transactional
    public String bulkDelete(@RequestParam("ids") String idsStr) {
        List<Long> ids = parseIds(idsStr);
        if (!ids.isEmpty()) {
            chatMessageRepository.deleteByListingIdIn(ids);
            listingRepository.deleteAllById(ids);
        }
        return "redirect:/admin/moderation";
    }

    @GetMapping("/listings/{id}/preview")
    public String previewFragment(@PathVariable("id") Long id, Model model) {
        Listing listing = listingRepository.findById(id).orElseThrow();
        model.addAttribute("car", listing);
        return "admin/moderation :: listingPreview";
    }

    private List<Long> parseIds(String idsStr) {
        if (idsStr == null || idsStr.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(idsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}