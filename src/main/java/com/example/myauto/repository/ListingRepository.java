package com.example.myauto.repository;

import com.example.myauto.entity.Listing;
import com.example.myauto.entity.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findAllByStatus(ListingStatus status);
}