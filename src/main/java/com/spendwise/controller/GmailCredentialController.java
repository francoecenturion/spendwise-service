package com.spendwise.controller;

import com.spendwise.dto.GmailCredentialDTO;
import com.spendwise.service.interfaces.IGmailCredentialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gmail")
public class GmailCredentialController {

    private static final Logger log = LoggerFactory.getLogger(GmailCredentialController.class);

    private final IGmailCredentialService gmailCredentialService;

    @Autowired
    public GmailCredentialController(IGmailCredentialService gmailCredentialService) {
        this.gmailCredentialService = gmailCredentialService;
    }

    @PostMapping("/credential")
    public ResponseEntity<GmailCredentialDTO> save(@RequestBody GmailCredentialDTO dto) {
        GmailCredentialDTO result = gmailCredentialService.save(dto);
        log.debug("POST /gmail/credential - Gmail credential saved");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/credential")
    public ResponseEntity<Void> delete() {
        gmailCredentialService.delete();
        log.debug("DELETE /gmail/credential - Gmail credential deleted");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status")
    public ResponseEntity<GmailCredentialDTO> getStatus() {
        GmailCredentialDTO status = gmailCredentialService.getStatus();
        log.debug("GET /gmail/status");
        return ResponseEntity.ok(status);
    }

}
