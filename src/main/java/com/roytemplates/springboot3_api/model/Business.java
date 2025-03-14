package com.roytemplates.springboot3_api.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;


// Represents a business entity 
// @Data for automatic getters, setters, equals, hashCode, and toString methods
// @Document specifies the MongoDB collection name
@Data
@Document(collection = "businesses")
public class Business {

    @Id
    private String id;

    @Indexed
    @NotBlank(message = "Name is required")
    private String name;

    private String adminID;

    private String logoImage;

    private String description;

    private String wallpaperImage;

    private String website;

    private String email;

    private String instaLink;

    private String fbLink;

    private String twitterLink;

    private String address;

    private String contactInfo;

    private String brandColorRGB;

    @Indexed
    private boolean deleted = false;

}