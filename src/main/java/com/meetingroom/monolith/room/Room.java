package com.meetingroom.monolith.room;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String location = "Hyd"; // Hyderabad
    private String campus;          // "Campus ATC" or "Capital ATC"
    private String block;           // "A Block", "B Block", "N/A"
    private String floor;           // e.g. "Ground Floor", "Level 1", "3rd Floor"
    private Integer capacity;
    @Column(name = "vc_facility")
    private String vcFacility;      // "VC" or "Non-VC"
    private String zone;            // "Zone 1", "Zone 2", "Zone 3", "GCC", etc.
    private String remarks;         // "Inside Zone", "Corridor", etc.
    private String facilities;      // Comma separated e.g. "Projector, Whiteboard"
    private String status = "Active"; // "Active" or "Inactive"

    public Room() {
    }

    public Room(Long id, String name, String location, String campus, String block, String floor, 
                Integer capacity, String vcFacility, String zone, String remarks, String facilities, String status) {
        this.id = id;
        this.name = name;
        this.location = location != null ? location : "Hyd";
        this.campus = campus;
        this.block = block;
        this.floor = floor;
        this.capacity = capacity;
        this.vcFacility = vcFacility;
        this.zone = zone;
        this.remarks = remarks;
        this.facilities = facilities;
        this.status = status != null ? status : "Active";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }

    public String getBlock() { return block; }
    public void setBlock(String block) { this.block = block; }

    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getVcFacility() { return vcFacility; }
    public void setVcFacility(String vcFacility) { this.vcFacility = vcFacility; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

