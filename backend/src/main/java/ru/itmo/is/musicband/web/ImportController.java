package ru.itmo.is.musicband.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.is.musicband.dto.ImportOperationDto;
import ru.itmo.is.musicband.dto.ImportResultDto;
import ru.itmo.is.musicband.repo.ImportOperationRepository;
import ru.itmo.is.musicband.service.ImportService;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {
    private final ImportService importService;
    private final ImportOperationRepository importOpRepo;

    @PostMapping
    public ImportResultDto upload(@RequestParam("file") MultipartFile file, 
                                   @RequestParam(name = "user", required = false) String userName,
                                   Principal principal) throws Exception {
        String user = getUserName(userName, principal);
        return importService.importCsv(file, user);
    }

    @GetMapping("/history")
    public List<ImportOperationDto> history(@RequestParam(name = "user", required = false) String userName,
                                             @RequestParam(name = "all", defaultValue = "false") boolean all,
                                             Principal principal) {
        String user = getUserName(userName, principal);
        boolean isAdmin = isAdmin(user);
        
        log.info("History: userName param={}, resolved user={}, isAdmin={}, all={}", userName, user, isAdmin, all);
        
        if (all && !isAdmin) {
            throw new IllegalStateException("Access denied: admin privileges required");
        }
        
        var allOps = importOpRepo.findAll();
        log.info("Total operations in DB: {}", allOps.size());
        for (var op : allOps) {
            log.info("  DB: id={}, userName='{}'", op.getId(), op.getUserName());
        }
        
        var ops = (all && isAdmin) ? allOps : allOps.stream()
            .filter(op -> {
                boolean matches = user != null && user.equals(op.getUserName());
                if (!matches) {
                    log.debug("Filtered out: id={}, userName='{}' != '{}'", op.getId(), op.getUserName(), user);
                }
                return matches;
            })
            .toList();
        
        log.info("Filtered operations: {}", ops.size());
        return ops.stream().map(o -> new ImportOperationDto(
                o.getId(),
                o.getUserName(),
                o.getStatus(),
                o.getAddedCount(),
                o.getErrorMessage(),
                o.getCreatedAt(),
                o.getUpdatedAt()
        )).toList();
    }
    
    private String getUserName(String userName, Principal principal) {
        if (userName != null && !userName.isBlank()) {
            return userName;
        }
        if (principal != null) {
            return principal.getName();
        }
        return "anonymous";
    }
    
    private boolean isAdmin(String userName) {
        if (userName == null || userName.isBlank()) {
            return false;
        }
        String name = userName.trim();
        return name.equals("admin") || name.startsWith("admin_") || name.equalsIgnoreCase("administrator");
    }
}

