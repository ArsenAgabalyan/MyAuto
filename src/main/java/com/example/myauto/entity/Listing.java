package com.example.myauto.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    private Double price;
    private String carModel;
    private Integer year;
    private String contactPhone; // Новое поле для связи

    private boolean notBeaten = true;
    private boolean notPainted;
    private boolean isOwner;
    private boolean serviceBook;

    // New Listing fields requested by user
    private Integer mileage;
    private boolean gasEquipment = false;
    private String steeringWheel = "LEFT"; // "LEFT", "RIGHT"
    private boolean pricing = true;
    private boolean exchange = false;

    private boolean oneOwner;
    private boolean originalMileage;
    private boolean garageStorage;
    private boolean keysSet;
    private boolean onWarranty;
    private boolean noAccidents;
    private boolean dealerServiced;
    private boolean customsCleared;
    private boolean negotiable;
    private boolean noExchange;
    private boolean urgentSale;

    @Enumerated(EnumType.STRING)
    private ListingStatus status = ListingStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "listing_images", joinColumns = @JoinColumn(name = "listing_id"))
    @Column(name = "image_path")
    private List<String> images = new ArrayList<>();

    // Геттеры и Сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public ListingStatus getStatus() { return status; }
    public void setStatus(ListingStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public boolean isNotBeaten() { return notBeaten; }
    public void setNotBeaten(boolean notBeaten) { this.notBeaten = notBeaten; }

    public boolean isNotPainted() { return notPainted; }
    public void setNotPainted(boolean notPainted) { this.notPainted = notPainted; }

    public boolean isOwner() { return isOwner; }
    public void setOwner(boolean isOwner) { this.isOwner = isOwner; }

    public boolean isServiceBook() { return serviceBook; }
    public void setServiceBook(boolean serviceBook) { this.serviceBook = serviceBook; }

    // New getters and setters
    public Integer getMileage() { return mileage; }
    public void setMileage(Integer mileage) { this.mileage = mileage; }

    public boolean isGasEquipment() { return gasEquipment; }
    public void setGasEquipment(boolean gasEquipment) { this.gasEquipment = gasEquipment; }

    public String getSteeringWheel() { return steeringWheel; }
    public void setSteeringWheel(String steeringWheel) { this.steeringWheel = steeringWheel; }

    public boolean isPricing() { return pricing; }
    public void setPricing(boolean pricing) { this.pricing = pricing; }

    public boolean isExchange() { return exchange; }
    public void setExchange(boolean exchange) { this.exchange = exchange; }

    public boolean isOneOwner() { return oneOwner; }
    public void setOneOwner(boolean oneOwner) { this.oneOwner = oneOwner; }

    public boolean isOriginalMileage() { return originalMileage; }
    public void setOriginalMileage(boolean originalMileage) { this.originalMileage = originalMileage; }

    public boolean isGarageStorage() { return garageStorage; }
    public void setGarageStorage(boolean garageStorage) { this.garageStorage = garageStorage; }

    public boolean isKeysSet() { return keysSet; }
    public void setKeysSet(boolean keysSet) { this.keysSet = keysSet; }

    public boolean isOnWarranty() { return onWarranty; }
    public void setOnWarranty(boolean onWarranty) { this.onWarranty = onWarranty; }

    public boolean isNoAccidents() { return noAccidents; }
    public void setNoAccidents(boolean noAccidents) { this.noAccidents = noAccidents; }

    public boolean isDealerServiced() { return dealerServiced; }
    public void setDealerServiced(boolean dealerServiced) { this.dealerServiced = dealerServiced; }

    public boolean isCustomsCleared() { return customsCleared; }
    public void setCustomsCleared(boolean customsCleared) { this.customsCleared = customsCleared; }

    public boolean isNegotiable() { return negotiable; }
    public void setNegotiable(boolean negotiable) { this.negotiable = negotiable; }

    public boolean isNoExchange() { return noExchange; }
    public void setNoExchange(boolean noExchange) { this.noExchange = noExchange; }

    public boolean isUrgentSale() { return urgentSale; }
    public void setUrgentSale(boolean urgentSale) { this.urgentSale = urgentSale; }
}