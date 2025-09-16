package com.servicerequest.emailbot.service.servicerequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicerequest.emailbot.service.auth.AuthService;
import com.servicerequest.emailbot.repository.DbService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class ServiceRequest {
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final Dotenv dotenv;

    @Autowired
    private AuthService authService;

    @Autowired
    private DbService dbService;

    private static final Map<String, Integer> PRIORITY_MAP = Map.of(
            "Low", 1,
            "Medium", 2,
            "High", 3
    );

    public ServiceRequest() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.dotenv = Dotenv.configure().ignoreIfMissing().load();
    }

    public String createServiceRequest(String category, String typeValue, String department, String subject,
                                       String description, String priority, String emailId) throws Exception {
        String apiUrl = dotenv.get("SERVICE_REQUEST_API");
        if (apiUrl == null) {
            throw new RuntimeException("SERVICE_REQUEST_API environment variable not set");
        }

        // ✅ Step 1: Get user session (same as Python get_service_tracker_session)
        Map<String, Object> session = authService.getServiceTrackerSession(emailId);
        if (session == null || !session.containsKey("cookie_token")) {
            throw new RuntimeException("❌ Authentication failed. Could not get user session.");
        }

        String cookieToken = (String) session.get("cookie_token");
        String personId = (String) session.get("personID");
        String userName = (String) session.get("userName");
        String fullName = (String) session.get("fullName");
        String unitNumber = (String) session.get("unitNumber");
        String unitName = (String) session.get("orgUnit");

        // ✅ Step 2: Build draft SR payload
        Integer categoryCode = dbService.getCategoryCode(category);
        if (categoryCode == null) categoryCode = 61;

        Integer typeCode = dbService.getTypeCode(typeValue, categoryCode);
        if (typeCode == null) typeCode = 108;

        Integer priorityId = PRIORITY_MAP.getOrDefault(priority, 2);
        Integer adminGroupId = 11;
        long currentTime = System.currentTimeMillis();

        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> serviceRequest = new HashMap<>();

        serviceRequest.put("serviceRequestId", null);
        serviceRequest.put("statusCode", 1);

        Map<String, Object> serviceRequestStatus = new HashMap<>();
        serviceRequestStatus.put("statusCode", 1);
        serviceRequestStatus.put("description", "Draft");
        serviceRequestStatus.put("updateTimestamp", currentTime);
        serviceRequestStatus.put("updateUser", userName);
        serviceRequest.put("serviceRequestStatus", serviceRequestStatus);

        serviceRequest.put("typeCode", String.valueOf(typeCode));
        Map<String, Object> serviceRequestType = new HashMap<>();
        serviceRequestType.put("typeCode", String.valueOf(typeCode));
        serviceRequestType.put("description", typeValue);
        serviceRequestType.put("categoryCode", String.valueOf(categoryCode));
        serviceRequestType.put("adminGroupId", adminGroupId);
        serviceRequestType.put("isActive", true);
        serviceRequest.put("serviceRequestType", serviceRequestType);

        serviceRequest.put("categoryCode", String.valueOf(categoryCode));
        Map<String, Object> serviceRequestCategoryData = new HashMap<>();
        serviceRequestCategoryData.put("categoryCode", String.valueOf(categoryCode));
        serviceRequestCategoryData.put("description", category);
        serviceRequestCategoryData.put("isActive", true);
        serviceRequest.put("serviceRequestCategoryData", serviceRequestCategoryData);

        serviceRequest.put("subject", subject);
        serviceRequest.put("description", "<p>" + description + "</p>");
        serviceRequest.put("reporterPersonId", personId);
        serviceRequest.put("unitNumber", unitNumber);

        Map<String, Object> unit = new HashMap<>();
        unit.put("unitNumber", unitNumber);
        unit.put("unitName", department != null ? department : unitName);
        unit.put("isActive", true);
        serviceRequest.put("unit", unit);

        serviceRequest.put("createUser", userName);
        serviceRequest.put("updateUser", userName);
        serviceRequest.put("priorityId", priorityId);

        Map<String, Object> serviceRequestPriority = new HashMap<>();
        serviceRequestPriority.put("priorityId", priorityId);
        serviceRequestPriority.put("description", priority);
        serviceRequestPriority.put("isActive", true);
        serviceRequest.put("serviceRequestPriority", serviceRequestPriority);

        serviceRequest.put("createUserFullName", fullName);
        serviceRequest.put("updateUserFullName", fullName);
        serviceRequest.put("reporterPersonName", fullName);

        payload.put("serviceRequest", serviceRequest);
        payload.put("serviceRequestHistory", null);

        String jsonBody = mapper.writeValueAsString(payload);
        System.out.println("📤 Draft SR Payload: " + jsonBody);

        // ✅ Step 3: Create Draft SR
        HttpRequest draftRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Cookie", "Cookie_Token=" + cookieToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> draftResponse = client.send(draftRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("📥 Draft SR Response: " + draftResponse.body());

        if (draftResponse.statusCode() != 200) {
            throw new RuntimeException("❌ Failed to create draft SR. HTTP " + draftResponse.statusCode());
        }

        JsonNode draftJson = mapper.readTree(draftResponse.body());
        String srId = draftJson.path("serviceRequest").path("serviceRequestId").asText(null);

        if (srId == null) {
            throw new RuntimeException("❌ No serviceRequestId found in draft response");
        }
        System.out.println("✅ Draft SR created: " + srId);

        // ✅ Step 4: Submit SR
        boolean submitted = submitServiceRequest(srId, cookieToken);
        if (submitted) {
            System.out.println("🎉 Service request " + srId + " submitted successfully.");
        } else {
            System.err.println("⚠️ Service request created but submission failed: " + srId);
        }

        return srId;
    }

    private boolean submitServiceRequest(String serviceRequestId, String cookieToken) throws Exception {
        String submitApi = dotenv.get("SUBMIT_SR_API");
        if (submitApi == null) {
            throw new RuntimeException("SUBMIT_SR_API environment variable not set");
        }

        Map<String, Object> formJson = new HashMap<>();
        formJson.put("serviceRequestId", serviceRequestId);
        formJson.put("serviceRequestComment", null);
        formJson.put("newAttachments", new Object[0]);
        formJson.put("serviceRequestStatus", 1);

        String boundary = "----Boundary" + System.currentTimeMillis();
        StringBuilder formData = new StringBuilder();

        formData.append("--").append(boundary).append("\r\n");
        formData.append("Content-Disposition: form-data; name=\"formDataJson\"\r\n\r\n");
        formData.append(mapper.writeValueAsString(formJson)).append("\r\n");
        formData.append("--").append(boundary).append("--\r\n");

        HttpRequest submitRequest = HttpRequest.newBuilder()
                .uri(URI.create(submitApi))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Cookie", "Cookie_Token=" + cookieToken)
                .POST(HttpRequest.BodyPublishers.ofString(formData.toString()))
                .build();

        HttpResponse<String> response = client.send(submitRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("📥 Submit SR Response: " + response.body());

        return response.statusCode() == 200;
    }
}
