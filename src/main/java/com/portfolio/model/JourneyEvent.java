/**
 * Creado por Bernard Orozco
 * Individual visitor journey event
 */
package com.portfolio.model;

import java.util.Map;

public class JourneyEvent {
    private String type; // route, project_view, project_click, project_hover
    private long ts;
    private Map<String, Object> data;
    
    public JourneyEvent() {}
    
    public JourneyEvent(String type, long ts, Map<String, Object> data) {
        this.type = type;
        this.ts = ts;
        this.data = data;
    }
    
    // Getters and setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public long getTs() {
        return ts;
    }
    
    public void setTs(long ts) {
        this.ts = ts;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "JourneyEvent{" +
                "type='" + type + '\'' +
                ", ts=" + ts +
                ", data=" + data +
                '}';
    }
}