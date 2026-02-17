package com.readytoplanbe.myapp.service.dto;

import java.io.Serializable;

public class CountByDateDTO implements Serializable { // Implement Serializable as often done in JHipster DTOs
    private static final long serialVersionUID = 1L; // For Serializable

    private String date; // Formatted date string (e.g., "2025-07-04")
    private long count;

    public CountByDateDTO(String date, long count) {
        this.date = date;
        this.count = count;
    }

    // --- Getters and Setters ---
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountByDateDTO that = (CountByDateDTO) o;
        return count == that.count && date.equals(that.date);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(date, count);
    }

    @Override
    public String toString() {
        return "CountByDateDTO{" +
            "date='" + date + '\'' +
            ", count=" + count +
            '}';
    }
}
