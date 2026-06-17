package com.example.myauto.security; // Убедитесь, что package совпадает с вашим

import com.example.myauto.entity.*;
import com.example.myauto.repository.ListingRepository;
import com.example.myauto.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    public DataInitializer(UserRepository userRepository, ListingRepository listingRepository) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
    }

    @Override
    public void run(String... args) {
        // 1. Создаём администратора, если его нет
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin");
            admin.setRole(Role.ROLE_ADMIN);
            userRepository.save(admin);
            System.out.println("✅ Администратор создан — логин: admin, пароль: admin");
        }

        // 2. Очищаем базу и добавляем 10 машин с ЛОКАЛЬНЫМИ картинками
        if (true) {
            System.out.println("🔄 Очистка старых данных...");
            listingRepository.deleteAll();

            User admin = userRepository.findByUsername("admin").orElseThrow();

            // В конце каждой строки теперь прописан локальный путь: /uploads/1.jpg и т.д.
            String[][] cars = {
                    {"Toyota Camry 2020 — отличное состояние", "Camry V70", "2020", "18500", "+374 91 111 001", "Идеальное состояние, не бита, не крашена.", "/uploads/1.jpg"},
                    {"Skoda Octavia — практичный и вместительный", "Octavia A8", "2022", "19900", "+374 91 111 002", "Бензин 1.5 TSI, АКПП. Как новый.", "/uploads/2.jpg"},
                    {"Nissan Qashqai — городской кроссовер", "Qashqai J12", "2021", "20500", "+374 91 111 003", "Гибрид e-Power, передний привод. Пробег 29 000 км.", "/uploads/3.jpg"},
                    {"Chevrolet Tahoe — американский флагман", "Tahoe GMT1XX", "2018", "39000", "+374 91 111 004", "Бензин 5.3 V8, автомат, полный привод. 7 мест.", "/uploads/4.jpg"},
                    {"BMW X5 M-Sport", "X5 G05", "2019", "45000", "+374 91 111 005", "3.0 дизель, максимальная комплектация, панорамная крыша.", "/uploads/5.jpg"},
                    {"Mercedes-Benz E-Class 220d", "E-Class W213", "2021", "42000", "+374 91 111 006", "Дизель, полный привод, AMG пакет.", "/uploads/6.jpg"},
                    {"Audi A6 Quattro", "A6 C8", "2020", "38000", "+374 91 111 007", "Полный привод, виртуальная приборная панель, матричные фары.", "/uploads/7.jpg"},
                    {"Hyundai Tucson", "Tucson NX4", "2022", "26000", "+374 91 111 008", "Официальный дилер, на гарантии, 1 владелец.", "/uploads/8.jpg"},
                    {"Kia Sportage X-Line", "Sportage NQ5", "2023", "28000", "+374 91 111 009", "Новый автомобиль, без пробега. Самая полная комплектация X-Line.", "/uploads/9.jpg"},
                    {"Honda Civic Sport", "Civic X", "2019", "16500", "+374 91 111 010", "1.5 турбо, спортивная подвеска, отличная динамика.", "/uploads/10.jpg"}
            };

            for (String[] data : cars) {
                Listing listing = new Listing();
                listing.setTitle(data[0]);
                listing.setCarModel(data[1]);
                listing.setYear(Integer.parseInt(data[2]));
                listing.setPrice(Double.parseDouble(data[3]));
                listing.setContactPhone(data[4]);
                listing.setDescription(data[5]);

                listing.setUser(admin);
                listing.setStatus(ListingStatus.APPROVED);

                // Добавляем локальный путь к картинке
                listing.getImages().add(data[6]);

                listingRepository.save(listing);
            }
            System.out.println("✅ 10 новых объявлений с ЛОКАЛЬНЫМИ картинками успешно загружены!");
        }
    }
}