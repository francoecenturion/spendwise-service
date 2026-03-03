package com.spendwise.client.dolarApiHistorical;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflection;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@RegisterReflection
public class DolarApiHistoricalDTO {

    @JsonProperty("casa")
    private String type;

    @JsonProperty("compra")
    private BigDecimal buyingPrice;

    @JsonProperty("venta")
    private BigDecimal sellingPrice;

    @JsonProperty("fechaActualizacion")
    private LocalDate updateDate;
}
