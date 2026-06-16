package com.example.myauto.service;

import com.example.myauto.entity.Listing;
import com.example.myauto.entity.ListingStatus;
import com.example.myauto.repository.ListingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListingService {

    private final ListingRepository listingRepository;

    public ListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    // Показать на главной только одобренные
    public List<Listing> getApprovedListings() {
        return listingRepository.findAllByStatus(ListingStatus.APPROVED);
    }

    // Показать админу только те, что ждут проверки
    public List<Listing> getPendingListings() {
        return listingRepository.findAllByStatus(ListingStatus.PENDING);
    }

    // Сохранить объявление от пользователя
    public void saveListing(Listing listing) {
        listingRepository.save(listing);
    }

    // Одобрить объявление
    public void approveListing(Long id) {
        Listing listing = listingRepository.findById(id).orElseThrow();
        listing.setStatus(ListingStatus.APPROVED);
        listingRepository.save(listing);
    }

    // Удалить/Отклонить объявление
    public void deleteListing(Long id) {
        listingRepository.deleteById(id);
    }
}