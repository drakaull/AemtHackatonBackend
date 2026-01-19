package org.helha.aemthackatonbackend.domain;

import java.util.List;

public class Folder {
    private long id;
    private String name;
    private Folder parent;
    private List<Folder> children;
    private List<Note> notes;
}
