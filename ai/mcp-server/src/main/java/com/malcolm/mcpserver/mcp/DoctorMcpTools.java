package com.malcolm.mcpserver.mcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DoctorMcpTools {

    @Tool(description = "Get a list of all available doctors")
    public List<String> getAvailableDoctors() {
        // In a real scenario, this would autowire a DoctorRepository and call findAll()
        return List.of("dr_smith", "dr_jones", "dr_williams");
    }

    @Tool(description = "Get the specific availability slots for a given doctor")
    public String getDoctorAvailability(String doctorUsername) {
        String doctor = doctorUsername.toLowerCase();
        return switch (doctor) {
            case "dr_smith" -> "Available Monday and Wednesday morning.";
            case "dr_jones" -> "Available Tuesday and Thursday afternoon.";
            case "dr_williams" -> "Available Friday all day.";
            default -> "No availability information found for doctor: " + doctor;
        };
    }
}
