package com.example.skripsi.models.constant;

public class MessageConstants {
    public static class Auth {
        public static final String EMAIL_ALREADY_USED = "Email already used!";
        public static final String INVALID_EMAIL_OR_PASSWORD = "Invalid email or password";
        public static final String INVALID_STUDENT_ID_OR_LECTURE_ID = "Invalid Student ID or Lecture ID!";
        public static final String MISSING_REFRESH_TOKEN = "Missing refresh token";
    }

    public static class Validation {
        public static final String REGION_NOT_FOUND = "Region not found";
        public static final String MAJOR_NOT_FOUND = "Major not found";
        public static final String MAJOR_DOES_NOT_BELONG_TO_REGION = "Major does not belong to the selected region";
        public static final String INVALID_DOCUMENT_TYPE = "Invalid document type";
        public static final String INVALID_TYPE_COMPANIES_OR_JOBS = "Invalid type. Allowed values: companies, jobs";
        public static final String ENTITY_AND_ID_REQUIRED = "entity and id are required";
        public static final String UNKNOWN_ENTITY_TYPE = "Unknown entity type: ";
    }

    public static class NotFound {
        public static final String REQUEST_DOCUMENT_NOT_FOUND = "Request document not found";
        public static final String COMPANY_REQUEST_NOT_FOUND = "Company request not found";
        public static final String NOTIFICATION_NOT_FOUND = "Notification not found for company request";
        public static final String USER_PROFILE_NOT_FOUND = "User profile not found";
        public static final String ACCESS_DENIED_OWN_REQUESTS = "Access denied: you can only view your own company requests";
        public static final String ACCESS_DENIED_OWN_AUDIT_LOGS = "Access denied: you can only view audit logs for your own requests";
        public static final String ACCESS_DENIED_NOT_FOUND = "Not Found";
        public static final String ACCESS_DENIED = "Access Denied";
    }

    public static class Success {
        public static final String REGISTER_SUCCESS = "Register success";
        public static final String SUCCESSFULLY_CREATED_ACCOUNT = "Successfully Created Account";
        public static final String LOGIN_SUCCESS = "Login success";
        public static final String NEW_ACCESS_TOKEN_CREATED = "New access token created";
        public static final String LOGOUT_SUCCESS = "Logout success";
        public static final String REVIEW_SUBMITTED_SUCCESSFULLY = "Review submitted successfully";
        public static final String SUCCESSFULLY_SEARCH_COMPANIES = "Successfully search companies";
        public static final String SUCCESSFULLY_GET_COMPANIES = "Successfully Get Companies data";
        public static final String SUCCESSFULLY_SUBMIT_COMPANY_REQUEST = "Successfully submit company request";
        public static final String SUCCESSFULLY_GET_COMPANY_REQUESTS = "Successfully get company requests";
        public static final String SUCCESSFULLY_REVIEW_COMPANY_REQUEST = "Successfully review company request";
        public static final String COMPANY_REQUEST_DETAIL = "Company request detail";
        public static final String SUCCESSFULLY_GET_TOP_10_COMPANIES = "Successfully get top 10 companies by rating";
        public static final String SUCCESSFULLY_GET_COMPANY_PROFILE = "Successfully get company profile";
        public static final String SUCCESSFULLY_GET_ALL_USER = "Successfully Get All User";
        public static final String SUCCESSFULLY_GET_PROFILE = "Successfully get profile";
        public static final String EMAIL_IS_REQUIRED = "Email is required";
        public static final String EMAIL_ALREADY_USED_CONFLICT = "Email already used!";
        public static final String EMAIL_IS_AVAILABLE = "Email is available";
        public static final String CERTIFICATE_REQUEST_SUBMITTED = "Certificate request submitted successfully";
        public static final String CERTIFICATE_REQUEST_REVIEWED = "Certificate request reviewed successfully";
        public static final String CERTIFICATE_REQUEST_DETAIL = "Certificate request detail";
    }

    public static class Certificate {
        public static final String CERTIFICATE_REQUEST_ALREADY_FINALIZED = "Certificate request has already been ";
        public static final String CANNOT_BE_CHANGED = " and cannot be changed";
    }

    public static class Company {
        public static final String COMPANY_REQUEST_ALREADY_FINALIZED = "Company request has already been ";
        public static final String AND_CANNOT_BE_CHANGED = " and cannot be changed";
    }

    public static class Async {
        public static final String ASYNC_OPERATION_FAILED = "Async operation failed: ";
    }

    public static class Error {
        public static final String INTERNAL_SERVER_ERROR = "An unexpected error occurred. Please try again later.";
    }
}
