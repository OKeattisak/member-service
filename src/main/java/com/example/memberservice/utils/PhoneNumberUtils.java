package com.example.memberservice.utils;

public class PhoneNumberUtils {
    public static String normalize(String rawPhoneNumber) {
        if (rawPhoneNumber == null || rawPhoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }

        String digitsOnly = rawPhoneNumber.replaceAll("[^0-9+]", "");

        if (digitsOnly.startsWith("0")) {
            return "+66" + digitsOnly.substring(1);
        }

        if (digitsOnly.startsWith("+66")) {
            return digitsOnly;
        }

        if (digitsOnly.startsWith("66")) {
            return "+" + digitsOnly;
        }

        throw new IllegalArgumentException("Invalid Thai phone number format");
    }

    public static boolean isValid(String normalizedPhone) {
        return normalizedPhone != null && normalizedPhone.matches("^\\+66[0-9]{9}$");
    }

    public static String toLocalFormat(String normalizedPhone) {
        if (isValid(normalizedPhone)) {
            return "0" + normalizedPhone.substring(3);
        }
        throw new IllegalArgumentException("Invalid normalized Thai phone number");
    }
}
