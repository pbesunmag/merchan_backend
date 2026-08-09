package avengersshop.merchan_backend.controllers;

import avengersshop.merchan_backend.dto.request.CrearTerminalDTO;
import avengersshop.merchan_backend.dto.response.TerminalDTO;
import avengersshop.merchan_backend.services.TerminalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terminales")
@CrossOrigin(origins = "*")
public class TerminalController {
    private final TerminalService terminalService;

    public TerminalController(TerminalService terminalService) {
        this.terminalService = terminalService;
    }

    //Obtenemos todas las terminales
    @GetMapping
    public ResponseEntity<List<TerminalDTO>> listarTerminales() {
        return ResponseEntity.status(HttpStatus.OK).body(terminalService.listarTerminales());
    }

    //Creamos nueva terminal
    @PostMapping
    //@Valid activa las validaciones de Jakarta que están en la clase CrearTerminalDTO
    public ResponseEntity<TerminalDTO> crearTerminal(@Valid @RequestBody CrearTerminalDTO terminalDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(terminalService.crearTerminal(terminalDTO));
    }
}
