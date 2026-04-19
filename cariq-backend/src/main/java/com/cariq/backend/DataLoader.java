package com.cariq.backend;

import com.cariq.backend.model.Car;
import com.cariq.backend.repository.CarRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader {

    private final CarRepository carRepository;

    @PostConstruct
    public void load() {
        if (carRepository.count() > 0) return;

        carRepository.saveAll(List.of(

            // 1. Maruti Swift
            car("Maruti", "Swift", "ZXi",
                665000.0, "Petrol", "Manual", 23.76, 5, 3,
                "city",
                "Excellent fuel efficiency, peppy engine, easy to drive in traffic",
                "Small boot space, no rear AC vents, basic safety features"),

            // 2. Hyundai Creta
            car("Hyundai", "Creta", "SX(O)",
                1850000.0, "Petrol", "Automatic", 17.4, 5, 5,
                "city/family",
                "Premium cabin, ADAS suite, panoramic sunroof, strong brand resale",
                "Pricey top trim, turbo variants have a stiff ride"),

            // 3. Tata Nexon
            car("Tata", "Nexon", "Creative+",
                1050000.0, "Petrol", "Manual", 17.01, 5, 5,
                "city/family",
                "5-star NCAP safety, spacious for class, feature-rich infotainment",
                "Petrol NVH on highways, average after-sales in smaller towns"),

            // 4. Tata Nexon EV
            car("Tata", "Nexon EV", "Empowered+",
                1995000.0, "Electric", "Automatic", 489.0, 5, 5,
                "city/family",
                "489 km ARAI range, 7.2 kW AC charging, zero running cost per km",
                "Charging infra still developing, higher upfront cost"),

            // 5. Honda City
            car("Honda", "City", "ZX CVT",
                1660000.0, "Petrol", "Automatic", 18.4, 5, 3,
                "city/highway",
                "Refined CVT, spacious sedan, solid build quality",
                "No sunroof, rear disc brakes only on top trim"),

            // 6. Maruti Ertiga
            car("Maruti", "Ertiga", "VXi CNG",
                1099000.0, "CNG", "Manual", 26.11, 7, 3,
                "family",
                "Very low running cost on CNG, 7 seats, strong resale value",
                "CNG boot space loss, underpowered feel on highways"),

            // 7. Toyota Innova Crysta
            car("Toyota", "Innova Crysta", "GX Petrol MT",
                1977000.0, "Petrol", "Manual", 10.0, 7, 4,
                "family/highway",
                "Legendary reliability, commanding presence, best-in-class comfort",
                "Dated platform, low fuel efficiency, no ADAS"),

            // 8. Kia Seltos
            car("Kia", "Seltos", "HTX+",
                1689000.0, "Petrol", "Manual", 16.5, 5, 3,
                "city/family",
                "Feature-loaded, premium interiors, strong engine options",
                "Inconsistent after-sales network in tier-2 cities"),

            // 9. Mahindra Thar
            car("Mahindra", "Thar", "LX Hard Top Diesel AT",
                1699000.0, "Diesel", "Automatic", 15.2, 4, 4,
                "highway",
                "True 4x4 off-road capability, iconic styling, strong community",
                "Only 4 seats, rear entry difficult, high NVH at highway speeds"),

            // 10. Maruti Baleno
            car("Maruti", "Baleno", "Alpha",
                865000.0, "Petrol", "Automatic", 22.35, 5, 2,
                "city",
                "Spacious premium hatchback, Arkamys sound system, Suzuki Connect",
                "Only 2-star GNCAP, no diesel option, average highway composure"),

            // 11. Hyundai i20
            car("Hyundai", "i20", "Asta(O) Turbo DCT",
                1090000.0, "Petrol", "Automatic", 20.35, 5, 3,
                "city",
                "Punchy 1.0T engine, wireless Android Auto/CarPlay, sunroof",
                "Turbo DCT jerky in slow city traffic, slightly expensive"),

            // 12. Skoda Slavia
            car("Skoda", "Slavia", "Style 1.5 TSI AT",
                1799000.0, "Petrol", "Automatic", 18.67, 5, 5,
                "highway/city",
                "5-star NCAP, powerful 1.5 TSI engine, European build quality",
                "Pricey, limited dealer network, smaller boot than segment"),

            // 13. VW Taigun
            car("Volkswagen", "Taigun", "GT Plus 1.5 TSI DSG",
                1949000.0, "Petrol", "Automatic", 17.84, 5, 5,
                "highway/city",
                "5-star NCAP, 150 PS DSG, solid European build, fun to drive",
                "Premium pricing, limited service network in smaller cities"),

            // 14. MG Hector
            car("MG", "Hector", "Select Pro Turbo MT",
                1699000.0, "Petrol", "Manual", 15.8, 5, 4,
                "family/highway",
                "Large 14-inch touchscreen, ADAS features, spacious cabin",
                "Turbo engine feels laggy low down, average ride quality"),

            // 15. Tata Punch
            car("Tata", "Punch", "Creative AMT",
                1010000.0, "Petrol", "Automatic", 20.09, 5, 5,
                "city",
                "5-star GNCAP safest in class, high ground clearance, funky styling",
                "Not a true SUV, limited highway refinement, cramped rear seat"),

            // 16. Renault Triber
            car("Renault", "Triber", "RXZ AMT",
                899000.0, "Petrol", "Automatic", 19.0, 7, 4,
                "city/family",
                "7 seats under 9 lakh, modular seating, light and easy to drive",
                "Underpowered 1.0 naturally aspirated engine, narrow at high speed"),

            // 17. Maruti Vitara Brezza
            car("Maruti", "Vitara Brezza", "ZXi+ AT",
                1395000.0, "Petrol", "Automatic", 19.8, 5, 3,
                "city/family",
                "Strong hybrid mild option, segment-best mileage, Suzuki reliability",
                "No diesel option, average safety rating, basic rear suspension"),

            // 18. Honda Elevate
            car("Honda", "Elevate", "ZX CVT",
                1999000.0, "Petrol", "Automatic", 15.36, 5, 3,
                "city/highway",
                "Refined CVT, premium cabin quality, Honda brand reliability",
                "No sunroof on lower trims, expensive for segment, no ADAS"),

            // 19. Hyundai Venue
            car("Hyundai", "Venue", "SX(O) Turbo DCT",
                1299000.0, "Petrol", "Automatic", 18.15, 5, 3,
                "city",
                "Connected car tech, compact footprint for city parking, sunroof",
                "Rear seat cramped for adults, turbo DCT jerky at crawl"),

            // 20. Maruti Fronx
            car("Maruti", "Fronx", "Alpha Turbo AT",
                1099000.0, "Petrol", "Automatic", 21.79, 5, 3,
                "city/highway",
                "Coupe-SUV styling, Boosterjet turbo engine, connected car tech",
                "No diesel, average rear space, mild-hybrid only on MT"),

            // 21. Toyota Fortuner
            car("Toyota", "Fortuner", "Legender 4x2 AT",
                4675000.0, "Diesel", "Automatic", 10.0, 7, 3,
                "highway/family",
                "Bulletproof reliability, strong resale, commanding road presence",
                "Very poor city fuel efficiency, stiff ride unladen, dated cabin tech"),

            // 22. Mahindra Scorpio-N
            car("Mahindra", "Scorpio", "Z8 L Diesel MT",
                2399000.0, "Diesel", "Manual", 15.0, 7, 3,
                "highway/family",
                "Powerful 2.2 mHawk diesel, 7 seats, rugged build, strong resale",
                "Floaty ride on bad roads, tall step-in height, mediocre NVH"),

            // 23. Mahindra Bolero
            car("Mahindra", "Bolero", "B6 Opt",
                938000.0, "Diesel", "Manual", 16.0, 7, 3,
                "family/highway",
                "Tough and reliable workhorse, strong rural road capability",
                "Very basic interiors, no modern safety or tech features, dated design"),

            // 24. Maruti Ignis
            car("Maruti", "Ignis", "Alpha MT",
                799000.0, "Petrol", "Manual", 20.89, 5, 3,
                "city",
                "Compact city car, quirky styling, easy to park, light controls",
                "No diesel, minimal highway ability, very small segment"),

            // 25. Audi Q7
            car("Audi", "Q7", "55 TFSI Technology",
                8990000.0, "Petrol", "Automatic", 11.24, 7, 5,
                "highway/family",
                "Quattro AWD, air suspension, premium cabin with MMI, ADAS",
                "High maintenance cost, poor ground clearance for Indian potholes")
        ));
    }

    private Car car(String make, String model, String variant,
                    Double price, String fuelType, String transmission,
                    Double mileage, Integer seating, Integer safetyRating,
                    String useCases, String pros, String cons) {
        Car c = new Car();
        c.setMake(make);
        c.setModel(model);
        c.setVariant(variant);
        c.setPrice(price);
        c.setFuelType(fuelType);
        c.setTransmission(transmission);
        c.setMileage(mileage);
        c.setSeating(seating);
        c.setSafetyRating(safetyRating);
        c.setUseCases(useCases);
        c.setPros(pros);
        c.setCons(cons);
        return c;
    }
}