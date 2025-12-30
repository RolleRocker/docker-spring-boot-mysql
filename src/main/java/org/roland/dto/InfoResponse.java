package org.roland.dto;

public record InfoResponse(String app, String version, String timestamp, long totalMessages) {
}
