package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.ApartmentRootSeedDto;
import softuni.exam.models.entity.Apartment;
import softuni.exam.models.entity.ApartmentType;
import softuni.exam.repository.ApartmentRepository;
import softuni.exam.service.ApartmentService;
import softuni.exam.service.TownService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParser;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

@Service
public class ApartmentServiceImpl implements ApartmentService {
    private static final String APARTMENTS_FILE_PATH = "src/main/resources/files/xml/apartments.xml";
    private final ApartmentRepository apartmentRepository;
    private final TownService townService;
    private final XmlParser xmlParser;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;

    public ApartmentServiceImpl(ApartmentRepository apartmentRepository, TownService townService, XmlParser xmlParser, ModelMapper modelMapper, ValidationUtil validationUtil) {
        this.apartmentRepository = apartmentRepository;
        this.townService = townService;
        this.xmlParser = xmlParser;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
    }


    @Override
    public boolean areImported() {
        return apartmentRepository.count() > 0;
    }

    @Override
    public String readApartmentsFromFile() throws IOException {
        return Files.readString(Path.of(APARTMENTS_FILE_PATH));
    }

    @Override
    public String importApartments() throws IOException, JAXBException {
        StringBuilder sb = new StringBuilder();

        xmlParser
                .fromFile(APARTMENTS_FILE_PATH, ApartmentRootSeedDto.class)
                .getApartments()
                .stream()
                .filter(apartmentSeedDto -> {
                    boolean isValid = validationUtil.isValid(apartmentSeedDto);
                    if (apartmentRepository.existsByArea(apartmentSeedDto.getArea())
                            && townService.findByName(apartmentSeedDto.getTown()) != null) {
                        sb.append("Invalid apartment");
                        sb.append(System.lineSeparator());
                        return false;
                    }
                    sb
                            .append(isValid
                                    ? String.format("Successfully imported apartment %s - %.2f",
                                    apartmentSeedDto.getApartmentType(), apartmentSeedDto.getArea()
                            )
                                    : "Invalid apartment")
                            .append(System.lineSeparator());
                    return isValid;
                })
                .map(apartmentSeedDto -> {
                    Apartment apartment = modelMapper.map(apartmentSeedDto, Apartment.class);
                    apartment.setApartmentType(ApartmentType.valueOf(apartmentSeedDto.getApartmentType().toUpperCase(Locale.ROOT)));
                    apartment.setTown(townService.findByName(apartmentSeedDto.getTown()));
                    return apartment;
                })
                .forEach(apartmentRepository::save);

        return sb.toString();
    }

    @Override
    public Apartment findById(Long id) {
        return apartmentRepository.findById(id).orElse(null);
    }
}
