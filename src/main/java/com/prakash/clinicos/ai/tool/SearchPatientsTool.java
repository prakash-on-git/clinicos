package com.prakash.clinicos.ai.tool;

import com.prakash.clinicos.patient.service.PatientService;
import com.prakash.clinicos.security.UserPrincipal;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/** Finds a patient's ID from a name/phone/email fragment — the usual first step before booking or history lookup. */
@Component
public class SearchPatientsTool implements ClinicalTool {

    private final PatientService patientService;
    private final ObjectMapper objectMapper;

    public SearchPatientsTool(PatientService patientService, ObjectMapper objectMapper) {
        this.patientService = patientService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "search_patients";
    }

    @Override
    public String description() {
        return "Search for patients registered at this clinic by name, phone, or email. "
                + "Returns each match's patientId, which other tools need.";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        return JsonSchema.object(
                JsonSchema.properties("query", JsonSchema.string("Name, phone, or email fragment to search for")),
                List.of("query"));
    }

    @Override
    public Object execute(Long clinicId, UserPrincipal principal, String argumentsJson) {
        JsonNode args = objectMapper.readTree(argumentsJson);
        String query = args.path("query").asString("");

        List<PatientMatch> matches = patientService
                .searchPatients(clinicId, query, true, PageRequest.of(0, 10))
                .map(p -> new PatientMatch(p.getId(), p.getFullName(), p.getPhone(), p.getEmail()))
                .getContent();

        return matches.isEmpty() ? Map.of("message", "No patients matched \"" + query + "\"") : matches;
    }

    private record PatientMatch(Long patientId, String fullName, String phone, String email) {}
}
