package com.example.myauto.service;

import com.example.myauto.entity.Listing;
import com.example.myauto.entity.ListingStatus;
import com.example.myauto.entity.User;
import com.example.myauto.repository.ListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListingService {

    private final ListingRepository listingRepository;

    public ListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public List<Listing> getApprovedListings() {
        return listingRepository.findAllByStatus(ListingStatus.APPROVED);
    }

    public List<Listing> getPendingListings() {
        return listingRepository.findAllByStatus(ListingStatus.PENDING);
    }

    public List<Listing> getListingsByUser(User user) {
        return listingRepository.findAllByUserOrderByCreatedAtDesc(user);
    }

    public void saveListing(Listing listing) {
        listingRepository.save(listing);
    }

    public void approveListing(Long id) {
        Listing listing = listingRepository.findById(id).orElseThrow();
        listing.setStatus(ListingStatus.APPROVED);
        listingRepository.save(listing);
    }

    public void deleteListing(Long id) {
        listingRepository.deleteById(id);
    }

    public void deleteUserListing(Long id, String username, boolean isAdmin) {
        Listing listing = listingRepository.findById(id).orElseThrow();
        // Удалить может либо владелец, либо админ
        if (isAdmin || listing.getUser().getUsername().equals(username)) {
            listingRepository.delete(listing);
        }
    }

    @Transactional
    public void updateListing(Long id, Listing updatedData, boolean isAdmin) {
        Listing listing = listingRepository.findById(id).orElseThrow();

        listing.setTitle(updatedData.getTitle());
        listing.setCarModel(updatedData.getCarModel());
        listing.setYear(updatedData.getYear());
        listing.setPrice(updatedData.getPrice());
        listing.setContactPhone(updatedData.getContactPhone());
        listing.setDescription(updatedData.getDescription());

        // Если правит не админ — отправляем на повторную модерацию
        if (!isAdmin) {
            listing.setStatus(ListingStatus.PENDING);
        } else {
            listing.setStatus(ListingStatus.APPROVED);
        }

        listingRepository.save(listing);
    }
}