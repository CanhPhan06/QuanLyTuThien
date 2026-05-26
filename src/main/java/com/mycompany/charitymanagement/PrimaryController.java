package com.mycompany.charitymanagement;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class PrimaryController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMessage;

    @FXML
    private void handleLogin() throws IOException {
        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        String password = txtPassword.getText() == null ? "" : txtPassword.getText();
        UserAccount account = AppData.authenticate(username, password);

        if (account == null) {
            lblMessage.setText("Sai tài khoản/mật khẩu");
            return;
        }

        lblMessage.setText("");
        UserSession.setCurrentUser(account);

        if (account.isAdmin()) {
            App.setRootToMainLayout(NavigationService.VIEW_DASHBOARD);
        } else if (account.isVolunteer()) {
            NavigationService.navigateTo(NavigationService.VIEW_VOLUNTEER);
        } else if (account.isSponsor()) {
            NavigationService.navigateTo(NavigationService.VIEW_SPONSORPORTAL);
        } else {
            lblMessage.setText("Vai trò tài khoản chưa được hỗ trợ");
        }
    }
}
