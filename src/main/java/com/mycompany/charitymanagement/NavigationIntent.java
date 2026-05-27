package com.mycompany.charitymanagement;

final class NavigationIntent {

    private static ContentFocus contentFocus;
    private static OperationFocus operationFocus;

    private NavigationIntent() {
    }

    static void focusContentComments(String campaignId) {
        contentFocus = new ContentFocus(safe(campaignId));
    }

    static ContentFocus consumeContentFocus() {
        ContentFocus focus = contentFocus;
        contentFocus = null;
        return focus;
    }

    static void focusOperations(String type, String campaignId, String status, String query) {
        operationFocus = new OperationFocus(safe(type), safe(campaignId), safe(status), safe(query));
    }

    static OperationFocus consumeOperationFocus() {
        OperationFocus focus = operationFocus;
        operationFocus = null;
        return focus;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    static final class ContentFocus {

        private final String campaignId;

        ContentFocus(String campaignId) {
            this.campaignId = campaignId;
        }

        String campaignId() {
            return campaignId;
        }
    }

    static final class OperationFocus {

        private final String type;
        private final String campaignId;
        private final String status;
        private final String query;

        OperationFocus(String type, String campaignId, String status, String query) {
            this.type = type;
            this.campaignId = campaignId;
            this.status = status;
            this.query = query;
        }

        String type() {
            return type;
        }

        String campaignId() {
            return campaignId;
        }

        String status() {
            return status;
        }

        String query() {
            return query;
        }
    }
}
