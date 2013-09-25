package fr.ungeek.Suggestions;

import com.google.common.base.Joiner;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "suggestion")
class Suggestion {

    @Id
    @Column(unique = true)
    private int id;
    private String playername;
    private int created_at;
    private String title;
    @Lob
    private String contenu;

    public Suggestion(String playername, int created_at, String title, List<String> contenu) {
        this.playername = playername;
        this.created_at = created_at;
        this.title = title;
        this.contenu = Joiner.on("\n").join(contenu);
    }

    public Suggestion(String playername, int created_at, String title, String contenu) {
        this.playername = playername;
        this.created_at = created_at;
        this.title = title;
        this.contenu = contenu;
    }

    public Suggestion() {
    }

    public String getPlayername() {
        return playername;
    }

    public void setPlayername(final String paramplayername) {
        playername = paramplayername;
    }

    public int getCreated_at() {
        return created_at;
    }

    public void setCreated_at(int created_at) {
        this.created_at = created_at;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
