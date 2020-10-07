/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.service.impl;

import cc.altius.FASP.model.CustomUserDetails;
import cc.altius.FASP.service.JiraServiceDeskApiService;
import cc.altius.FASP.service.UserService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author altius
 */
@Service
public class JiraServiceDeskApiServiceImpl implements JiraServiceDeskApiService {

    @Value("${jira.apiUrl}")
    private String JIRA_API_URL;
    @Value("${jira.serviceDeskApiUrl}")
    private String JIRA_SERVICE_DESK_API_URL;
    @Value("${jira.apiUsername}")
    private String JIRA_API_USERNAME;
    @Value("${jira.apiToken}")
    private String JIRA_API_TOKEN;

    @Autowired
    private UserService userService;    

    @Override
    public ResponseEntity addIssue(String jsonData, CustomUserDetails curUser) {

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response;
        JsonObject jsonObject, fieldsObject, reporterObject;        

        HttpHeaders headers = getCommonHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        jsonObject = JsonParser.parseString​(jsonData).getAsJsonObject();
        fieldsObject = jsonObject.getAsJsonObject("fields");
        
        reporterObject = fieldsObject.getAsJsonObject("reporter");
        reporterObject.addProperty("id", getUserJiraAccountId(curUser));                

        HttpEntity<String> entity = new HttpEntity<String>(jsonObject.toString(), headers);

        response = restTemplate.exchange(
                JIRA_API_URL + "/issue", HttpMethod.POST, entity, String.class);

        return response;
    }

    @Override
    public ResponseEntity addIssueAttachment(MultipartFile file, String issueId) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = null;
        try {

            HttpHeaders headers = getCommonHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);            
            headers.add("X-Atlassian-Token", "no-check");

            MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
            ContentDisposition contentDisposition = ContentDisposition
                    .builder("form-data")
                    .name("file")
                    .filename(file.getOriginalFilename())
                    .build();
            fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            HttpEntity<byte[]> fileEntity = new HttpEntity<>(file.getBytes(), fileMap);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileEntity);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            response = restTemplate.exchange(
                    JIRA_API_URL + "/issue/" + issueId + "/attachments", HttpMethod.POST, requestEntity, String.class);

            return response;
        } catch (IOException ex) {
            Logger.getLogger(JiraServiceDeskApiServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return response;
        }
    }    
    
    private String getUserJiraAccountId(CustomUserDetails curUser) {

        String jiraAccountId = "";
        jiraAccountId = this.userService.getUserJiraAccountId(curUser.getUserId());

        if (jiraAccountId != null && !jiraAccountId.equals("")) {
            return jiraAccountId;
        } else {
            return this.addJiraCustomer(curUser);
        }
    }
    
    private String addJiraCustomer(CustomUserDetails curUser) {
        JSONObject obj = new JSONObject();
        String accountId = "";
        obj.put("email", curUser.getEmailId());
        obj.put("displayName", curUser.getUsername());        
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response;

        HttpHeaders headers = getCommonHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> entity = new HttpEntity<String>(obj.toJSONString(), headers);

        response = restTemplate.exchange(
                JIRA_SERVICE_DESK_API_URL + "/customer", HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {

            JsonObject jsonObject = JsonParser.parseString​(response.getBody()).getAsJsonObject();
            JsonElement element = jsonObject.get("accountId");
            accountId = element.getAsString();
            this.userService.addUserJiraAccountId(curUser.getUserId(), accountId);
            return accountId;

        } else {
            return "";
        }
    }

    private HttpHeaders getCommonHeaders() {

        String authStr = JIRA_API_USERNAME + ":" + JIRA_API_TOKEN;
        String base64Creds = "Basic " + Base64.getEncoder().encodeToString(authStr.getBytes());        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", base64Creds);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));        
        return headers;
        
    }        
}