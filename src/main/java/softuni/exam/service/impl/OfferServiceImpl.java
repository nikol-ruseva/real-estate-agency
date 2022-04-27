package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.OfferExportDto;
import softuni.exam.models.dto.OfferRootSeedDto;
import softuni.exam.models.entity.Offer;
import softuni.exam.repository.OfferRepository;
import softuni.exam.service.AgentService;
import softuni.exam.service.ApartmentService;
import softuni.exam.service.OfferService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParser;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class OfferServiceImpl implements OfferService {
    private static final String OFFERS_FILE_PATH = "src/main/resources/files/xml/offers.xml";
    private final OfferRepository offerRepository;
    private final AgentService agentService;
    private final ApartmentService apartmentService;
    private final XmlParser xmlParser;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;

    public OfferServiceImpl(OfferRepository offerRepository, AgentService agentService, ApartmentService apartmentService, XmlParser xmlParser, ModelMapper modelMapper, ValidationUtil validationUtil) {
        this.offerRepository = offerRepository;
        this.agentService = agentService;
        this.apartmentService = apartmentService;
        this.xmlParser = xmlParser;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
    }

    @Override
    public boolean areImported() {
        return offerRepository.count() > 0;
    }

    @Override
    public String readOffersFileContent() throws IOException {
        return Files.readString(Path.of(OFFERS_FILE_PATH));
    }

    @Override
    public String importOffers() throws IOException, JAXBException {
        StringBuilder sb = new StringBuilder();

        xmlParser
                .fromFile(OFFERS_FILE_PATH, OfferRootSeedDto.class)
                .getOffers()
                .stream()
                .filter(offerSeedDto -> {
                    boolean isValid = validationUtil.isValid(offerSeedDto);
                    if (!agentService.existsByFirstName(offerSeedDto.getAgent().getName())) {
                        sb.append("Invalid offer");
                        sb.append(System.lineSeparator());
                        return false;
                    }
                    sb
                            .append(isValid
                                    ? String.format("Successfully imported offer %.2f",
                                    offerSeedDto.getPrice()
                            )
                                    : "Invalid offer")
                            .append(System.lineSeparator());
                    return isValid;
                })
                .map(offerSeedDto -> {
                   Offer offer = modelMapper.map(offerSeedDto, Offer.class);
                   offer.setAgent(agentService.findByFirstName(offerSeedDto.getAgent().getName()));
                   offer.setApartment(apartmentService.findById(offerSeedDto.getApartment().getId()));
                  return offer;
                })
                .forEach(offerRepository::save);

        return sb.toString();
    }

    @Override
    public String exportOffers() {
        StringBuilder sb = new StringBuilder();



        offerRepository.findByApartmentTypeOrderByAreaDescPriceAsc()
                .stream()
                .map(offer -> modelMapper.map(offer, OfferExportDto.class))
                .forEach(offer -> {
                    sb.append(String.format("""
                            Agent %s %s with offer №%d
                            -Apartment area: %.2f
                            --Town: %s
                            ---Price: %.2f$
                            """,
                            offer.getAgent().getFirstName(), offer.getAgent().getLastName(), offer.getId(),
                            offer.getApartment().getArea(), offer.getApartment().getTownName(),
                            offer.getPrice()));
                });

        return sb.toString();
    }
}
