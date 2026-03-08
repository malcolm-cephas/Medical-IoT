package com.malcolm.mcpserver.mcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DoctorMcpTools {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DoctorMcpTools.class);

    private final com.malcolm.mcpserver.repository.UserRepository userRepository;
    private final com.malcolm.mcpserver.repository.DoctorAvailabilityRepository availabilityRepository;

    public DoctorMcpTools(com.malcolm.mcpserver.repository.UserRepository userRepository,
            com.malcolm.mcpserver.repository.DoctorAvailabilityRepository availabilityRepository) {
        this.userRepository = userRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @Tool(description = "Get a list of all available doctors")
    public List<String> getAvailableDoctors() {
        logger.info("[EVAL] Step: TOOL_CALL | Tool: getAvailableDoctors");
        List<String> usernames = userRepository.findByRole("DOCTOR").stream()
                .map(com.malcolm.mcpserver.model.User::getUsername)
                .collect(java.util.stream.Collectors.toList());
        logger.info("[EVAL] Result: SUCCESS | Tool: getAvailableDoctors | Count: {}", usernames.size());
        return usernames;
    }

    @Tool(description = "Get the specific availability slots for a given doctor")
    public String getDoctorAvailability(String doctorUsername) {
        logger.info("[EVAL] Step: TOOL_CALL | Tool: getDoctorAvailability | Input: {}", doctorUsername);

        return userRepository.findByUsername(doctorUsername)
                .map(doctor -> {
                    List<com.malcolm.mcpserver.model.DoctorAvailability> slots = availabilityRepository
                            .findByDoctorId(doctor.getId());
                    if (slots.isEmpty()) {
                        return doctor.getFullName() + " (" + doctor.getDepartment()
                                + ") has no availability slots configured.";
                    }

                    StringBuilder sb = new StringBuilder(
                            doctor.getFullName() + " (" + doctor.getDepartment() + ") availability:\n");
                    for (com.malcolm.mcpserver.model.DoctorAvailability slot : slots) {
                        sb.append("- ").append(slot.getDayOfWeek())
                                .append(": ").append(slot.getStartTime())
                                .append(" - ").append(slot.getEndTime()).append("\n");
                    }
                    return sb.toString().trim();
                })
                .orElse("No information found for doctor: " + doctorUsername);
    }
}
