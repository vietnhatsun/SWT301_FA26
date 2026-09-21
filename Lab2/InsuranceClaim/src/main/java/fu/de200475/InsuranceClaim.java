package fu.de200475;

public class InsuranceClaim {

    // 1. Tính tiền bồi thường cơ bản: claimAmount - deductible
    public double calculateClaim(double claimAmount, double deductible) {
        if (claimAmount < 0 || deductible < 0) {
            throw new IllegalArgumentException("Claim amount and deductible must be non-negative");
        }
        if (claimAmount <= deductible) {
            return 0.0;
        }
        return claimAmount - deductible;
    }

    // 2. Kiểm tra điều kiện hợp lệ (tuổi >= 18 và số tiền bồi thường > 0)
    public boolean isEligible(int age, double claimAmount) {
        if (age < 0 || claimAmount < 0) {
            throw new IllegalArgumentException("Age and claim amount must be non-negative");
        }
        return age >= 18 && claimAmount > 0;
    }

    // 3. Tính tiền bồi thường có tỷ lệ khấu trừ copay (%): (claimAmount - deductible) * (100 - copayRate) / 100
    public double calculateClaimWithCopay(double claimAmount, double deductible, double copayRate) {
        if (copayRate < 0 || copayRate > 100) {
            throw new IllegalArgumentException("Copay rate must be between 0 and 100");
        }
        double claim = calculateClaim(claimAmount, deductible);
        return claim * (100 - copayRate) / 100.0;
    }
}
