package softuni.exam.models.dto;

import java.math.BigDecimal;

public class OfferExportDto {
    private Long id;
    private AgentFullNameDto agent;
    private ApartmentAreaAndTownNameDto apartment;
    private BigDecimal price;

    public OfferExportDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AgentFullNameDto getAgent() {
        return agent;
    }

    public void setAgent(AgentFullNameDto agent) {
        this.agent = agent;
    }

    public ApartmentAreaAndTownNameDto getApartment() {
        return apartment;
    }

    public void setApartment(ApartmentAreaAndTownNameDto apartment) {
        this.apartment = apartment;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
