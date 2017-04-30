package com.theironyard.entities;

import javax.persistence.*;

@Entity
@Table(name="photos")
public class Photo {

    @Id
    @GeneratedValue
    int id;

    @Column(nullable = false)
    String filename;

   @Column
    int showTime;

   @Column
   boolean makePublic;

    @ManyToOne
    User sender;

    @ManyToOne
    User receiver;

    public Photo(String filename, User sender, User receiver, int showTime, boolean makePublic) {
        this.filename = filename;
        this.sender = sender;
        this.receiver = receiver;
        this.showTime = showTime;
        this.makePublic = makePublic;
    }

    public boolean isMakePublic() {
        return makePublic;
    }

    public void setMakePublic(boolean makePublic) {
        this.makePublic = makePublic;
    }

    public Photo() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public int getShowTime() {
        return showTime;
    }

    public void setShowTime(int showTime) {
        this.showTime = showTime;
    }
}
