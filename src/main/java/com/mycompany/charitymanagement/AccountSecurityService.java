package com.mycompany.charitymanagement;

final class AccountSecurityService {

    private AccountSecurityService() {
    }

    static String resetPassword(String username, String newPassword, String confirmPassword) {
        UserAccount account = AppData.findAccount(username);
        if (account == null) {
            return "Không tìm thấy tài khoản.";
        }
        String validationError = validateNewPassword(newPassword, confirmPassword);
        if (validationError != null) {
            return validationError;
        }
        account.setPassword(newPassword);
        DatabaseRepository.updateAccountPassword(account);
        BusinessService.audit(account.getUsername(), "Đặt lại mật khẩu", "Người dùng đặt lại mật khẩu từ màn hình đăng nhập");
        return null;
    }

    static String changePassword(UserAccount account, String currentPassword, String newPassword, String confirmPassword) {
        if (account == null) {
            return "Phiên đăng nhập không hợp lệ.";
        }
        if (currentPassword == null || !account.getPassword().equals(currentPassword)) {
            return "Mật khẩu hiện tại không đúng.";
        }
        String validationError = validateNewPassword(newPassword, confirmPassword);
        if (validationError != null) {
            return validationError;
        }
        if (account.getPassword().equals(newPassword)) {
            return "Mật khẩu mới phải khác mật khẩu hiện tại.";
        }
        account.setPassword(newPassword);
        DatabaseRepository.updateAccountPassword(account);
        BusinessService.audit(account.getUsername(), "Đổi mật khẩu", "Người dùng đổi mật khẩu trong hồ sơ");
        return null;
    }

    private static String validateNewPassword(String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.trim().length() < 3) {
            return "Mật khẩu mới cần có ít nhất 3 ký tự.";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "Mật khẩu xác nhận không khớp.";
        }
        return null;
    }
}
