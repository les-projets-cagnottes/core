package fr.lesprojetscagnottes.core.vote.model;

public enum VoteType {
    NONE("NONE"),
    UP("UP"),
    DOWN("DOWN");

    public final String label;

    VoteType(String label) {
        this.label = label;
    }
}
