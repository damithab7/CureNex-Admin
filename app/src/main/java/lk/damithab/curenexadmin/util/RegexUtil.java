package lk.damithab.curenexadmin.util;

public class RegexUtil {

        public static String generateCode() {
            int r = (int) (Math.random() * 1000000);
            return String.format("%06d", r);
        }

        public static boolean isEmailValid(String email) {
            return email.matches("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
        }

        public static boolean isPasswordValid(String password) {
            return password.matches("^.*(?=.{6,})(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$");
        }

        public static boolean isMobileValid(String mobile) {
            return mobile.matches("^(0{1})(7{1})([0|1|2|4|5|6|7|8]{1})([0-9]{7})");
        }

        public static boolean isCharacterValid(String text){
            return text.matches("^[\\p{L}\\p{N}\\p{P}\\p{Zs}]+$");
        }

        public static boolean isValidationCodeValid(String code) {
            return code.matches("^\\d{4,6}$");
        }

        public static boolean isInteger(String value) {
            return value.matches("^\\d+$");
        }

        public static boolean isDouble(String text) {
            return text.matches("^\\d+(\\.\\d{2})?$");
        }

}
