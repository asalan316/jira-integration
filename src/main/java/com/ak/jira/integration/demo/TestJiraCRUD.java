package com.ak.jira.integration.demo;


import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.*;

@Component
@Log4j2
public class TestJiraCRUD {

    /* Define your JIRA configutation */
    private static final String JIRA_URL = "http://10.48.112.33:9082";
    private static final String USERNAME = "arsalan.khan";
    private static final String PASSWORD = "153Iuh8tAeRrTXOkgfgK";

    // define project key
    private static final String PROJECT_KEY = "ATP";

    private static final String VALUE_KEY = "value";
    private JiraRestClient restClient;


    @EventListener
    public void onHandle(ApplicationStartedEvent event) {

        String issueKey = "ATP-8";
        // connect with Jira server
        connectWithJiraRestClient(JIRA_URL, USERNAME, PASSWORD);

        // get issue with Issue Key or create issue if not found
        Issue issue = getIssue(issueKey);
        if (issue == null) {
            issueKey = createIssue(PROJECT_KEY, 10002L, "Open Customer Account");
            issue = getIssue(issueKey);
        }

        // update fields in the issue
        Issue updatedIssue = updateIssueFields(issue);

        // move status from In progress to Done
        transitionIssueToDone(updatedIssue);

        // add comments
        addComment(updatedIssue);

    }

    private Issue updateIssueFields(Issue issue) {
        // set meta data for type of account field
        List<String> typeOfAccount = Arrays.asList("non-resident", "USD");
        List<ComplexIssueInputFieldValue> typeOfAccountValues = buildAndGetMultiSelectValues(typeOfAccount);

        // set meta data for documents submitted field
        List<String> documentsSubmitted = Arrays.asList("Identity Card", "Utility Bills", "Witness Affidavit");
        List<ComplexIssueInputFieldValue> documentsSubmittedValues = buildAndGetMultiSelectValues(documentsSubmitted);

        // update issue with new field values
        final IssueInputBuilder issueInputBuilder = new IssueInputBuilder()
                // set account status field
                .setFieldValue(getIssueFieldIdByName(issue, "Account Status"), ComplexIssueInputFieldValue.with(VALUE_KEY, "Active"))
                // set document submitted field
                .setFieldValue(getIssueFieldIdByName(issue, "Documents submitted"), documentsSubmittedValues)
                // set type of account field
                .setFieldValue(getIssueFieldIdByName(issue, "Type of Account"), typeOfAccountValues);
        getIssueClient().updateIssue(issue.getKey(), issueInputBuilder.build()).claim();

        log.info("updated Issue -> " + issue.getKey());

        return getIssue(issue.getKey());
    }

    private void transitionIssueToDone(Issue updatedIssue) {
        final Issue issue = getIssue(updatedIssue.getKey());
        final Iterable<Transition> transitions = getIssueClient().getTransitions(issue).claim();
        Transition transitionFound = getTransitionByName(transitions, "Done");

        getIssueClient().transition(issue, new TransitionInput(transitionFound.getId())).claim();
        final Issue changedIssue = getIssue(updatedIssue.getKey());
        log.info("issue status -> " + changedIssue.getStatus().getName());
    }

    private void addComment(Issue updatedIssue) {
        getIssueClient().addComment(updatedIssue.getCommentsUri(), Comment.valueOf("all documents are received. Account opened"));
        log.info("added comment to " + updatedIssue.getKey());
    }

    private IssueRestClient getIssueClient() {
        return restClient.getIssueClient();
    }

    private void connectWithJiraRestClient(String jiraUrl, String username, String password) {
        this.restClient = new AsynchronousJiraRestClientFactory()
                .createWithBasicHttpAuthentication(URI.create(jiraUrl), username, password);
    }

    private List<ComplexIssueInputFieldValue> buildAndGetMultiSelectValues(List<String> allowedValues) {

        List<ComplexIssueInputFieldValue> fieldList = new ArrayList<>();
        for (String value : allowedValues) {

            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put(VALUE_KEY, value);
            ComplexIssueInputFieldValue fieldValue = new ComplexIssueInputFieldValue(valuesMap);
            fieldList.add(fieldValue);
        }
        return fieldList;
    }

    private String getIssueFieldIdByName(Issue issue, String fieldName) {
        return issue.getFieldByName(fieldName).getId();
    }

    private String createIssue(String projectKey, Long issueType, String issueSummary) {
        IssueRestClient issueClient = getIssueClient();
        IssueInput newIssue = new IssueInputBuilder(
                projectKey, issueType, issueSummary).build();
        String issueKey = issueClient.createIssue(newIssue).claim().getKey();
        log.info("created issue -> " + issueKey);
        return issueKey;
    }

    private Issue getIssue(String issueKey) {

        try {
            return restClient.getIssueClient()
                    .getIssue(issueKey)
                    .claim();
        } catch (Exception e) {
            log.warn("issue with issueKey " + issueKey + " not found ");
        }
        return null;
    }

    private static Transition getTransitionByName(Iterable<Transition> transitions, String transitionName) {
        Transition transitionFound = null;
        for (Transition transition : transitions) {
            if (transition.getName().equals(transitionName)) {
                transitionFound = transition;
                break;
            }
        }
        return transitionFound;
    }
}
