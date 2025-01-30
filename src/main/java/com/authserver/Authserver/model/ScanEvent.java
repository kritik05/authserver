package com.authserver.Authserver.model;


import java.util.List;

public class ScanEvent {
    private String repo;
    private String owner;
    private List<ScanType> types;

    public ScanEvent() {
    }

    public ScanEvent(String repo, String owner, List<ScanType> types) {
        this.repo = repo;
        this.owner = owner;
        this.types = types;
    }

    public String getRepo() {
        return repo;
    }
    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<ScanType> getTypes() {
        return types;
    }
    public void setTypes(List<ScanType> types) {
        this.types = types;
    }

    @Override
    public String toString() {
        return "ScanEvent{" +
                "repo='" + repo + '\'' +
                ", owner='" + owner + '\'' +
                ", types=" + types +
                '}';
    }
}
