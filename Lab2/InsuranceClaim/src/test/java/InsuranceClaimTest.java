import fu.de200475.InsuranceClaim;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class InsuranceClaimTest {

    private InsuranceClaim insuranceClaim;

    @BeforeEach
    void setUp() {
        insuranceClaim = new InsuranceClaim();
    }

    @Test
    @DisplayName("calculateClaim: 1000 - 200 trả về 800")
    void calculateClaim_NormalCase_ReturnsDifference() {
        // Arrange
        double claimAmount = 1000;
        double deductible = 200;
        double expected = 800.0;

        // Act
        double actual = insuranceClaim.calculateClaim(claimAmount, deductible);

        // Assert
        assertEquals(expected, actual, 0.001);
    }

    @Test
    @DisplayName("calculateClaim: claim <= deductible trả về 0")
    void calculateClaim_ClaimLessThanDeductible_ReturnsZero() {
        // Arrange
        double claimAmount = 150;
        double deductible = 200;
        double expected = 0.0;

        // Act
        double actual = insuranceClaim.calculateClaim(claimAmount, deductible);

        // Assert
        assertEquals(expected, actual, 0.001);
    }

    @Test
    @DisplayName("calculateClaim: số âm ném IllegalArgumentException")
    void calculateClaim_NegativeInput_ThrowsException() {
        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> insuranceClaim.calculateClaim(-100, 50)
        );
        assertEquals("Claim amount and deductible must be non-negative", ex.getMessage());
    }

    @Test
    @DisplayName("isEligible: tuổi >= 18 và claim > 0 trả về true")
    void isEligible_ValidAgeAndClaim_ReturnsTrue() {
        // Act & Assert
        assertTrue(insuranceClaim.isEligible(20, 500));
        assertFalse(insuranceClaim.isEligible(16, 500));
    }

    @Test
    @DisplayName("calculateClaimWithCopay: tính đúng khi có copay %")
    void calculateClaimWithCopay_ValidInput_ReturnsCorrectAmount() {
        // claim 1000, deductible 200, copay 20% => (1000 - 200) * 80% = 640
        double actual = insuranceClaim.calculateClaimWithCopay(1000, 200, 20);
        assertEquals(640.0, actual, 0.001);
    }

    @ParameterizedTest(name = "Test {index} => claim: {0}, deductible: {1} => kết quả: {2}")
    @CsvSource({
            "1000, 200, 800.0",
            "500, 100, 400.0",
            "300, 300, 0.0",
            "100, 200, 0.0"
    })
    @DisplayName("calculateClaim: kiểm thử với nhiều bộ dữ liệu")
    void calculateClaim_Parameterized(double claimAmount, double deductible, double expected) {
        double actual = insuranceClaim.calculateClaim(claimAmount, deductible);
        assertEquals(expected, actual, 0.001);
    }
}
