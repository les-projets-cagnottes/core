package fr.lesprojetscagnottes.core.project.model;

public enum ProjectStatus {
    DRAFT("DRAFT"),
    IDEA("IDEA"),
    IN_PROGRESS("IN_PROGRESS"),
    FINISHED("FINISHED");

    public final String label;

    ProjectStatus(String label) {
        this.label = label;
    }
}
