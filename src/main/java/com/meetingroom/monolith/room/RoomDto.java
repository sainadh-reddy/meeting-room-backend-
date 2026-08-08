package com.meetingroom.monolith.room;

public class RoomDto {
    private Long id;
    private String name;
    private Integer floor;
    private Integer capacity;
    private String facilities;
    private String status;

    public RoomDto() {}

    public RoomDto(Long id, String name, Integer floor, Integer capacity, String facilities, String status) {
        this.id = id;
        this.name = name;
        this.floor = floor;
        this.capacity = capacity;
        this.facilities = facilities;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
