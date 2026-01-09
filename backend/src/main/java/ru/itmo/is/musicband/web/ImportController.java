package ru.itmo.is.musicband.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.is.musicband.dto.ImportOperationDto;
import ru.itmo.is.musicband.dto.ImportResultDto;
import ru.itmo.is.musicband.repo.ImportOperationRepository;
import ru.itmo.is.musicband.service.ImportService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;
    private final ImportOperationRepository importOpRepo;

    @PostMapping
    public ImportResultDto upload(@RequestParam("file") MultipartFile file, Principal principal) throws Exception {
        String user = principal.getName();
        return importService.importCsv(file, user);
    }

    @GetMapping("/history")
    public List<ImportOperationDto> history(
            Principal principal,
            Authentication auth,
            @RequestParam(name = "all", defaultValue = "false") boolean all
    ) {
        String user = principal.getName();

        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        var ops = (all && isAdmin) ? importOpRepo.findAll() : importOpRepo.findByUserName(user);

        return ops.stream()
                .map(o -> new ImportOperationDto(
                        o.getId(),
                        o.getUserName(),
                        o.getStatus(),
                        o.getAddedCount(),
                        o.getErrorMessage(),
                        o.getCreatedAt(),
                        o.getUpdatedAt()
                ))
                .toList();
    }
}
