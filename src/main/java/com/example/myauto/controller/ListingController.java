package com.example.myauto.controller;

import com.example.myauto.entity.Listing;
import com.example.myauto.entity.ListingStatus;
import com.example.myauto.entity.Role;
import com.example.myauto.entity.User;
import com.example.myauto.repository.UserRepository;
import com.example.myauto.repository.ListingRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/listings")
public class ListingController {

    // Заметь: ListingService больше нет, работаем напрямую с базой
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    public ListingController(UserRepository userRepository, ListingRepository listingRepository) {
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

        // Сохраняем напрямую в репозиторий
        listingRepository.save(listing);
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
    public String updateListing(@PathVariable Long id, @ModelAttribute("listing") Listing updatedData, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        Listing listing = listingRepository.findById(id).orElseThrow();

        // Проверка прав: обновить может только владелец или администратор
        if (!listing.getUser().getUsername().equals(principal.getName()) && currentUser.getRole() != Role.ROLE_ADMIN) {
            return "redirect:/";
        }

        boolean isAdmin = (currentUser.getRole() == Role.ROLE_ADMIN);

        listing.setTitle(updatedData.getTitle());
        listing.setCarModel(updatedData.getCarModel());
        listing.setYear(updatedData.getYear());
        listing.setPrice(updatedData.getPrice());
        listing.setContactPhone(updatedData.getContactPhone());
        listing.setDescription(updatedData.getDescription());

        // Если правит не админ — отправляем на модерацию
        if (!isAdmin) {
            listing.setStatus(ListingStatus.PENDING);
        } else {
            listing.setStatus(ListingStatus.APPROVED);
        }

        // Сохраняем изменения напрямую
        listingRepository.save(listing);

        return isAdmin ? "redirect:/admin/moderation" : "redirect:/profile?updated";
    }

    @GetMapping("/{id}")
    public String viewListingDetail(@PathVariable("id") Long id, Model model) {
        Listing car = listingRepository.findById(id).orElseThrow();
        model.addAttribute("car", car);
        return "listing/detail";
    }

    // Я добавил сюда методы удаления и одобрения, которые были в сервисе,
    // чтобы они не потерялись. Позже мы привяжем их к кнопкам админа/профиля.
    @PostMapping("/delete/{id}")
    public String deleteListing(@PathVariable Long id, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        Listing listing = listingRepository.findById(id).orElseThrow();

        if (currentUser.getRole() == Role.ROLE_ADMIN || listing.getUser().getUsername().equals(principal.getName())) {
            listingRepository.delete(listing);
        }
        return "redirect:/profile?deleted";
    }

    @PostMapping("/approve/{id}")
    public String approveListing(@PathVariable Long id, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            Listing listing = listingRepository.findById(id).orElseThrow();
            listing.setStatus(ListingStatus.APPROVED);
            listingRepository.save(listing);
        }
        return "redirect:/admin/moderation";
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