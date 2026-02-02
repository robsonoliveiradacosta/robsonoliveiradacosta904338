package com.quarkus.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "album_images")
public class AlbumImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Column(nullable = false, length = 255)
    private String bucket;

    @Column(nullable = false, length = 255)
    private String hash;

    @Column(name = "content_type", nullable = false, length = 255)
    private String contentType;

    @Column(nullable = false)
    private Integer size;

    public AlbumImage() {
    }

    public AlbumImage(Album album, String bucket, String hash, String contentType, Integer size) {
        this.album = album;
        this.bucket = bucket;
        this.hash = hash;
        this.contentType = contentType;
        this.size = size;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
