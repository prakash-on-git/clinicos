package com.prakash.clinicos.medical.dto.request;

/**
 * SOAP clinical note. All four sections are optional — doctors fill what is relevant.
 * Sending null for a section leaves the existing value unchanged (update semantics).
 * Sending an empty string ("") clears a section.
 */
public class SaveClinicalNoteRequest {

    /** Subjective — patient's reported symptoms / chief complaint. */
    private String subjective;

    /** Objective — examination findings, vitals summary, test results. */
    private String objective;

    /** Assessment — diagnosis or differential. */
    private String assessment;

    /** Plan — treatment, referrals, follow-up instructions. */
    private String plan;

    public String getSubjective()  { return subjective; }
    public String getObjective()   { return objective; }
    public String getAssessment()  { return assessment; }
    public String getPlan()        { return plan; }
}
