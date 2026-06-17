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

        // 2. ИСПРАВЛЕНО: Добавляем машины ТОЛЬКО если база данных пуста
        if (listingRepository.count() == 0) {
            System.out.println("🔄 База данных пуста. Заполнение тестовыми данными...");

            User admin = userRepository.findByUsername("admin").orElseThrow();

            String[][] cars = {
                    // Оригинальные 10 машин
                    {"Toyota Camry 2020 — отличное состояние", "Camry V70", "2020", "18500", "+374 91 111 001", "Идеальное состояние, не бита, не крашена.", "/uploads/1.jpg"},
                    {"Skoda Octavia — практичный и вместительный", "Octavia A8", "2022", "19900", "+374 91 111 002", "Бензин 1.5 TSI, АКПП. Как новый.", "/uploads/2.jpg"},
                    {"Nissan Qashqai — городской кроссовер", "Qashqai J12", "2021", "20500", "+374 91 111 003", "Гибрид e-Power, передний привод. Пробег 29 000 км.", "/uploads/3.jpg"},
                    {"Chevrolet Tahoe — американский флагман", "Tahoe GMT1XX", "2018", "39000", "+374 91 111 004", "Бензин 5.3 V8, автомат, полный привод. 7 мест.", "/uploads/4.jpg"},
                    {"BMW X5 M-Sport", "X5 G05", "2019", "45000", "+374 91 111 005", "3.0 дизель, максимальная комплектация, панорамная крыша.", "/uploads/5.jpg"},
                    {"Mercedes-Benz E-Class 220d", "E-Class W213", "2021", "42000", "+374 91 111 006", "Дизель, полный привод, AMG пакет.", "/uploads/6.jpg"},
                    {"Audi A6 Quattro", "A6 C8", "2020", "38000", "+374 91 111 007", "Полный привод, виртуальная приборная панель, матричные фары.", "/uploads/7.jpg"},
                    {"Hyundai Tucson", "Tucson NX4", "2022", "26000", "+374 91 111 008", "Официальный дилер, на гарантии, 1 владелец.", "/uploads/8.jpg"},
                    {"Kia Sportage X-Line", "Sportage NQ5", "2023", "28000", "+374 91 111 009", "Новый автомобиль, без пробега. Самая полная комплектация X-Line.", "/uploads/9.jpg"},
                    {"Honda Civic Sport", "Civic X", "2019", "16500", "+374 91 111 010", "1.5 турбо, спортивная подвеска, отличная динамика.", "/uploads/10.jpg"},

                    // Дополнительные 20 машин
                    {"Ford Mustang GT — Американская мускулатура", "Mustang VI", "2017", "24500", "+374 91 111 011", "5.0 V8 Coyote, легендарный звук, задний привод, механика.", "/uploads/11.jpg"},
                    {"Volkswagen Golf GTI — Заряженный хэтчбек", "Golf VII GTI", "2016", "15800", "+374 91 111 012", "2.0 TSI, DSG, отличное состояние, обслужен до мелочей.", "/uploads/12.jpg"},
                    {"Porsche Cayenne S — Спорт и роскошь", "Cayenne 958.2", "2015", "33000", "+374 91 111 013", "3.6 Битурбо, пневмоподвеска, премиальная акустика Bose.", "/uploads/13.jpg"},
                    {"Tesla Model 3 Long Range", "Model 3", "2021", "29500", "+374 91 111 014", "Электро, полный привод, запас хода 550 км. Автопилот включен.", "/uploads/14.jpg"},
                    {"Subaru Forester — Для любых дорог", "Forester SK", "2019", "19200", "+374 91 111 015", "2.5 атмосферный, симметричный полный привод, система EyeSight.", "/uploads/15.jpg"},
                    {"Lexus RX 450h — Надежный гибрид", "RX 4 IV", "2018", "31500", "+374 91 111 016", "3.5 Гибрид, максимальная комплектация Luxury, один владелец.", "/uploads/16.jpg"},
                    {"Mazda CX-5 Supreme", "CX-5 KF", "2020", "22000", "+374 91 111 017", "2.5 SkyActiv-G, проекция на лобовое стекло, кожаный салон.", "/uploads/17.jpg"},
                    {"Jeep Grand Cherokee Overland", "Grand Cherokee WK2", "2014", "17500", "+374 91 111 018", "3.6 бензин, честный полный привод, отличная проходимость.", "/uploads/18.jpg"},
                    {"Land Rover Range Rover Sport", "Range Rover Sport L494", "2016", "28500", "+374 91 111 019", "3.0 Дизель, обслужен у дилера, богатая комплектация.", "/uploads/19.jpg"},
                    {"Volvo XC90 Inscription", "XC90 II", "2017", "27000", "+374 91 111 020", "2.0 D5 Дизель, 7 мест, эталон безопасности, шведское качество.", "/uploads/20.jpg"},
                    {"BMW 3 series — Стильная тройка", "3 Series G20", "2020", "26500", "+374 91 111 021", "2.0 бензин, задний привод, M-пакет, лазерные фары.", "/uploads/21.jpg"},
                    {"Toyota Land Cruiser Prado", "Prado 150", "2012", "23000", "+374 91 111 022", "3.0 Дизель, легендарная надежность, рама, блокировки.", "/uploads/22.jpg"},
                    {"Mercedes-Benz C-Class Coupe", "C-Class C205", "2018", "21800", "+374 91 111 023", "1.6 турбо, AMG Line, панорама, привлекает взгляды.", "/uploads/23.jpg"},
                    {"Mitsubishi Outlander", "Outlander III", "2015", "12500", "+374 91 111 024", "2.4 бензин, полный привод, надежный семейный кроссовер.", "/uploads/24.jpg"},
                    {"Renault Megane — Экономичный дизель", "Megane IV", "2018", "11200", "+374 91 111 025", "1.5 dCi, расход 4.5л/100км, ухоженный салон, механическая КПП.", "/uploads/25.jpg"},
                    {"Peugeot 3008 GT-Line", "3008 II", "2019", "18300", "+374 91 111 026", "1.6 дизель, футуристичный i-Cockpit салон, панорамная крыша.", "/uploads/26.jpg"},
                    {"Suzuki Jimny — Маленький танк", "Jimny IV", "2021", "21000", "+374 91 111 027", "1.5 бензин, полноценный внедорожник, подключаемый полный привод.", "/uploads/27.jpg"},
                    {"Audi A4 Allroad Quattro", "A4 B9 Allroad", "2017", "20500", "+374 91 111 028", "2.0 TFSI, повышенный клиренс, идеальный универсал для семьи.", "/uploads/28.jpg"},
                    {"BMW 5 series (Классика E39)", "5 series E39", "2001", "6500", "+374 91 111 029", "2.5 бензин, легендарный кузов, для ценителей марки.", "/uploads/29.jpg"},
                    {"Mercedes-Benz S-Class (W140) — Кабан", "S-Class W140", "1997", "9500", "+374 91 111 030", "Ретро-классика. S500 V8, коллекционное состояние, двойные стекла.", "/uploads/30.jpg"}
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

                listing.getImages().add(data[6]);

                listingRepository.save(listing);
            }
            System.out.println("✅ 30 новых объявлений успешно загружены!");
        } else {
            System.out.println("ℹ️ База данных уже содержит объявления. Пропуск инициализации.");
        }
    }
}