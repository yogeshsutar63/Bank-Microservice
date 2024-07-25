package com.eazybytes.accounts.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Schema(
        name = "Cards",
        description = "Schema to hold Card information"
)
@Data
public class CardsDto {

    @NotEmpty(message = "Mobile number should not be empty or null")
    @Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number should be 10 digits")
    @Schema(description = "Mobile Number of Customer", example = "1234567890")
    private String mobileNumber;

    @NotEmpty(message = "Card Number of the Customer cannot be null or Empty")
    @Pattern(regexp = "(^$|[0-9]{12})",message = "Card Number should be 12 digits only")
    @Schema(description = "Card Number of the Customer", example = "123412341234")
    private String cardNumber;

    @NotEmpty(message = "Card Type cannot be Empty")
    @Schema(description = "Type of the Card",example = "Credit Card")
    private String cardType;

    @Positive(message = "Total Limit of the card should be greater than zero")
    @Schema(description = "Total limit available against the Card",example = "1000000")
    private int totalLimit;

    @PositiveOrZero(message = "Total amount should be zero or greater than zero")
    @Schema(description = "Amount used against the issued credit limit",example = "1000")
    private int amountUsed;

    @PositiveOrZero(message = "Available amount should be zero or Greater than Zero")
    @Schema(description = "Total amount available against a card", example = "900000")
    private int availableAmount;

}
